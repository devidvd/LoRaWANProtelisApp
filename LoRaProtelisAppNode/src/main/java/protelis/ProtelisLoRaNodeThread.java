package protelis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.util.CodePath;

import lora.LoRaNode;
import protelis.util.DeviceStatus;
import protelis.util.ObjectInstanceBuilder;
import protelis.util.ObjectPiecesToSend;

/**
 * The class is used to represent and manage a node drived 
 * by a protelis application.
 * */
public class ProtelisLoRaNodeThread extends Thread {

    private Map<DeviceUID, Map<CodePath, Object>> neighborState;
    private Map<DeviceUID, Map<CodePath, Object>> tempNeighborState;
    private Map<CodePath, Object> ownStateToSend;
    private final LoRaNode device;
    private final boolean joinOTAA;
    private DeviceStatus deviceStatus;
    
    public ProtelisLoRaNodeThread(final LoRaNode node, final boolean joinOTAA) {
        this.neighborState = null;
        this.device = node;
        this.joinOTAA = joinOTAA;
        this.deviceStatus = DeviceStatus.NOT_JOINED;
    }
    
    public DeviceStatus getDevice() {
        return this.deviceStatus;
    }
    
    public Map<DeviceUID, Map<CodePath, Object>> getTempNeighborState() {
        return this.tempNeighborState;
    }
    
    public synchronized void setOwnState(Map<CodePath, Object> ownStateToSend) {
        this.ownStateToSend = ownStateToSend;
    }
    
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        return this.neighborState;
    }
    
    /**
     * 
     * */
    public void joinOTAA() {
        boolean keep_join = true;
        while(keep_join) {
            try {
                this.device.joinOtaa();
                keep_join = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Try again to join the application using OTAA.");
            }
        }
    }
    
    /**
     * 
     * */
    public void joinABP() {
        boolean keep_join = true;
        while(keep_join) {
            try {
                this.device.joinAbp();
                /*  
                 * In this protelis application, the DeviceUID match 
                 * the LoRa DevEUI. So after the ABP join, the device 
                 * needs to send his own the DevEUI (usually a node 
                 * that use ABP, doesn't need the devEUI but in this case
                 * is the protelis application that needs it! So the 
                 * ABP node must have his own DevEUI configured.) In 
                 * this way the backend application can build the 
                 * protelis map using the DevEUI as DeviceUID. For 
                 * obviously reasons, this message must be confirmed.
                 * */
                this.device.transmit("!" + this.device.getDevEUI() + "!", true);
                keep_join = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Try again to join the application using ABP.");
            }
        }
    }
    
    /**
     * The loop work of the node. 
     * The node wait until the protelis @NetworkManager wakes 
     * it calling his shareState() method. The node will not 
     * enter in wait mode again until it has complete the 
     * sharing of his state. If the server interrupt the 
     * shareState procedure with the sharing of the neighborState,
     * the shareState will be resumed after the server send all neighborState.
     * */
    public void run() {
        if(this.joinOTAA) {
            joinOTAA();
        } else {
            joinABP();
        }
        this.deviceStatus = DeviceStatus.JOINED;
        sleepForTimeToWaitAfterTransmission();
        while(true) {
            try {
                // wait the call of startSendProcedure()
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
            sendState();
        }
    }
    
    /**
     * Called by the method "shareState(Map<CodePath, Object> toSend)"
     * declared in @NetworkManager and implemented in @ProtelisLoRaNetworkManagerImpl.
     * */
    public synchronized void startSendStateProcedure() {
        notify();
    }
    
    /**
     * Manage the sharing procedure of his own state.
     * */
    public void sendState() {
        //change device state and serialize his state
        this.deviceStatus = DeviceStatus.TRANSMITTING;
        String serializeState;
        try {
            serializeState = serializeSingleState(this.ownStateToSend);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during state conversion, this transmission won't start.");
            return;
        }
        //divide the state
        final ObjectPiecesToSend objPieces = new ObjectPiecesToSend(serializeState, this.device.getLoraDataRate());
        boolean receiveAllPacketConfirmation = false;
        /* For this first implementation, the application will always send
         *  confirmed message, so for now the application doesn't need 
         *  to have a retransmission protocol. Anyway the first 4 byte of 
         *  the payload are reserved for a future retransmission protocol implementation. */
        while(!receiveAllPacketConfirmation) {
            try {
                String msgReceived = this.device.transmit(objPieces.getNextPiece(), true);
                /*
                 * If the node receives a downlink, means that the node needs to 
                 * prepare to receive the neighborState.
                 * */
                if(msgReceived != null) {
                    receiveNeighborState(msgReceived);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("Error on serial port during transmission");
                System.exit(1);
            }
            sleepForTimeToWaitAfterTransmission();
            if(objPieces.allPiecesTransmitted()) {
                receiveAllPacketConfirmation = true;
            }
        }
        this.deviceStatus = DeviceStatus.JOINED;
    }
    
    /**
     * Start managing the receiving of NeighborState procedure.
     * This method should be call when the transmission 
     * of his own state is interrupted by downlink receiving.
     * */
    public void receiveNeighborState(final String firstHexMsgReceived) {
        this.deviceStatus = DeviceStatus.RECEIVING;
        final String firstCharMsgReceived = convertHexStringToCharString(firstHexMsgReceived);
        final String nMaxPacket = firstCharMsgReceived.substring(2, 4);
        this.tempNeighborState = this.neighborState;
        final ObjectInstanceBuilder objBuilder = new ObjectInstanceBuilder(Integer.valueOf(nMaxPacket));
        boolean receiveAllPacket = false;
        String msgReceived = null;
        while(!receiveAllPacket) {
            try {
                msgReceived = this.device.transmit("empty_message", true);
                if(msgReceived != null) {
                    final String charMsgReceived = convertHexStringToCharString(msgReceived);
                    final String currentPacketIndex = charMsgReceived.substring(0, 2);
                    objBuilder.addPiece(Integer.valueOf(currentPacketIndex), charMsgReceived.substring(4));
                    //check if the receive packet is the last one
                    if(Integer.valueOf(currentPacketIndex) == (Integer.valueOf(nMaxPacket) - 1)) {
                        receiveAllPacket = true;
                        try {
                            this.neighborState = (Map<DeviceUID, Map<CodePath, Object>>) objBuilder.buildInstance();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("Error on serial port during transmission");
                System.exit(1);
            }
            sleepForTimeToWaitAfterTransmission();
        }
        this.deviceStatus = DeviceStatus.TRANSMITTING;
    }
    
    public String convertHexStringToCharString(final String hexmsg) {
        final byte[] payloadInByte = DatatypeConverter.parseHexBinary(hexmsg);
        String payloadDecoded = "the_hex_conversion_has_failed";
        try {
            payloadDecoded = new String(payloadInByte, "UTF-8");
            //System.out.println("codified in bytes converted in string = " + msgDecoded);
        } catch (UnsupportedEncodingException e1) {
            System.out.println("Error during hex to string conversion");
            return null;
        }
        return payloadDecoded;
    }
    /** 
     * Convert the a single device state to a Base64 string.
     * This method will be called by the server application
     * before send data. 
     * */
    public String serializeSingleState(final Map<CodePath, Object> instance) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objOutputStream = new ObjectOutputStream( byteArrayOutputStream );
        objOutputStream.writeObject(instance);
        objOutputStream.close();
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()); 
    }
    
    /**
     * This method sleep for the timeToWaitAfterTransmission field
     * inside a @LoRaNodeImpl class.
     * */
    private void sleepForTimeToWaitAfterTransmission() {
        System.out.println("Sleep " + this.device.getTimeToWaitAfterTransmission() + " milliseconds, before begin to transmit.");
        try {
            sleep(this.device.getTimeToWaitAfterTransmission());
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
    
}
