package protelis;

import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;

import lora.LoRaNode;

/**
 * LoRaWAN protelis NetworkManager.
 * */
public class ProtelisLoRaNetworkManagerImpl implements NetworkManager {

    private ProtelisLoRaNodeThread protelisLoRaDevice;
    
    public ProtelisLoRaNetworkManagerImpl(final LoRaNode device, final boolean joinOTAA) {
        this.protelisLoRaDevice = new ProtelisLoRaNodeThread(device, joinOTAA);
    }
    
    /**
     * Start the first initialization of the node in the system.
     * */
    public void startCommunication() {
       this.protelisLoRaDevice.start();
    }
    
    @Override
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        switch(this.protelisLoRaDevice.getDevice()) {
            case NOT_JOINED:
                return this.protelisLoRaDevice.getNeighborState();// = null
            case JOINED:
                return this.protelisLoRaDevice.getNeighborState();
            case TRANSMITTING:
                return this.protelisLoRaDevice.getNeighborState();
            case RECEIVING:
                return this.protelisLoRaDevice.getTempNeighborState();
        }
        try {
            throw new Exception("The device state is not initialized!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    @Override
    public void shareState(Map<CodePath, Object> toSend) {
        switch(this.protelisLoRaDevice.getDevice()) {
            case NOT_JOINED:
                break;
            case JOINED:
                //set the device state and wake up the node thread to send his protelis state
                this.protelisLoRaDevice.setOwnState(toSend);
                this.protelisLoRaDevice.startSendStateProcedure();
            case TRANSMITTING:
                break;
            case RECEIVING:
                break;
        }
    }
    
}
