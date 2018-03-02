package protelis;

import java.util.Map;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;

import lora.LoRaNode;


public class ProtelisLoRaNetworkManagerImpl implements NetworkManager {

    private ProtelisLoRaNodeThread protelisLoRaDevice;
    
    public ProtelisLoRaNetworkManagerImpl(final LoRaNode device, final boolean joinOTAA) {
        this.protelisLoRaDevice = new ProtelisLoRaNodeThread(device, joinOTAA);
    }
    
    /**
     * Start the first initialization of the node in the system.
     * 
     * */
    public void startCommunication() {
       this.protelisLoRaDevice.start();
    }
    
    @Override
    public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
        if(this.protelisLoRaDevice.getSharingStateInProgress()) {
            return this.protelisLoRaDevice.getTempNeighborState();
        }
        return this.protelisLoRaDevice.getNeighborState();
    }

    @Override
    public void shareState(Map<CodePath, Object> toSend) {
        //if the device does not currently share his protelis state...
        if(!this.protelisLoRaDevice.getSharingStateInProgress()) {
            //...set the device state and wake up the node thread to send his protelis state
            this.protelisLoRaDevice.setOwnState(toSend);
            notify();
        }
    }
    
}
