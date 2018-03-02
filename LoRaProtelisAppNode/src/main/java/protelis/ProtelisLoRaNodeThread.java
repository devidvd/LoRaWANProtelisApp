package protelis;

import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.util.CodePath;

import lora.LoRaNode;

public class ProtelisLoRaNodeThread extends Thread {

    private Map<DeviceUID, Map<CodePath, Object>> neighborState;
    private Map<DeviceUID, Map<CodePath, Object>> tempNeighborState;
    private Map<CodePath, Object> ownStateToSend;
    private LoRaNode device;
    private boolean sharingStateInProgress;
    private boolean joinOTAA;
    
    public ProtelisLoRaNodeThread(final LoRaNode node, final boolean joinOTAA) {
        this.neighborState = null;
        this.device = node;
        this.joinOTAA = joinOTAA;
    }
    
    public boolean getSharingStateInProgress() {
        return this.sharingStateInProgress;
    }
    
    public void setOwnState(Map<CodePath, Object> ownStateToSend) {
        this.ownStateToSend = ownStateToSend;
    }
    
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        return this.neighborState;
    }
    
    public Map<DeviceUID, Map<CodePath, Object>> getTempNeighborState() {
        return this.tempNeighborState;
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
        while(true) {
            try {
                wait();//wait the notify of the shareState
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
            sendState();
        }
    }
    
    public void sendState() {
        
        
        //ultima operazione da fare
        this.sharingStateInProgress = false;
    }
    
    public void receiveNeighborState() {
        
    }
    
}
