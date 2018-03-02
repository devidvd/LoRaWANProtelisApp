package shareneighborstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.protelis.vm.util.CodePath;

import util.DevEUI;
import util.Pair;

/**
 * This class store the nodes manage the priority of the nodes currently joined.
 * */
public class ShareNeighborStateScheduler {
    
    //map of the devices data: (human) deviceId - devEUI.
    private Map<String, DevEUI> id_eui_map;
    //protelis map (deviceUID = devEUI).
    private Map<DevEUI, Map<CodePath, Object>> protelis_map;
    //this list contains nodes EUI that have already the neighborstate
    private List<String> deviceIdSharePriority;
    //this list contains deviceID that are just joined and they have to receive the neighborstate
    private List<String> devicesIdJustJoined;
    
    /**
     * @param devicesID_ports ...
     * */
    public ShareNeighborStateScheduler(List<Pair<String, List<String>>> devicesID_ports) {
        this.deviceIdSharePriority = new ArrayList<>();
        this.devicesIdJustJoined = new ArrayList<>();
        this.id_eui_map = new HashMap<String, DevEUI>();
        this.protelis_map = new HashMap<DevEUI, Map<CodePath, Object>>();
        for(Pair<String, List<String>> deviceId : devicesID_ports) {
            this.id_eui_map.put(deviceId.getX(), null);
        }
    }
    
    /**
     * @param deviceId ...
     * 
     * Method to call when a node joins the system.
     * It requires synchronized to avoid problems if two nodes send join request simultaneously.
     * 
     * @throws Exception In case the deviceId isn't registered in system.
     * */
    public synchronized boolean addNodes(String deviceId, String deviceEUI) throws Exception {
        if(this.id_eui_map.containsKey(deviceId)) {
            if(this.id_eui_map.get(deviceId) == null) {
                this.id_eui_map.replace(deviceId, new DevEUI(deviceEUI));
                this.protelis_map.put(new DevEUI(deviceEUI), new HashMap<>());
                this.devicesIdJustJoined.add(deviceEUI);
                return true;
            }
            return false;
        } else {
            throw new Exception("The device id doesn't exist in system!");
        }
    }
    
    /**
     * Update the protelis state of one device, called by @UplinkMsgHandler 
     * when receives the state of one device. 
     * */
    public synchronized void updateProtelisState(String deviceID, Map<CodePath, Object> state) {
        this.protelis_map.replace(this.id_eui_map.get(deviceID), state);
    }
    
    /**
     * If the scheduler is in FREE Status, downlink transmissions will be scheduled.
     * */
    public synchronized void schedule() {
        if(SchedulerStatus.getInstance().getStatus() == ShareNeighborStatus.FREE) {
            //if there is a node that have just joined the system
            if(!this.devicesIdJustJoined.isEmpty()) {
                String deviceId = this.devicesIdJustJoined.get(0);
                /* TODO qui ci va la procedura per frammentare le informazioni 
                 * dello stato protelis di tutti gli altri devici attivi 
                 */
                int counterCommunicationFrame = 3;//numero di downlink necessario per trasmettere lo stato dei vicini
                try {
                    SchedulerStatus.getInstance().startDownlinkCommunication(deviceId, counterCommunicationFrame);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                //change the wait queque of the device
                this.deviceIdSharePriority.add(this.devicesIdJustJoined.remove(0));
            }
            //schedule downlinks following the standard queque
            else {
                String deviceId = this.deviceIdSharePriority.get(0);
                /* TODO qui ci va la procedura per frammentare le informazioni 
                 * dello stato protelis di tutti gli altri devici attivi 
                 */
                int counterCommunicationFrame = 3;//numero di downlink necessario per trasmettere lo stato dei vicini
                try {
                    SchedulerStatus.getInstance().startDownlinkCommunication(deviceId, counterCommunicationFrame);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                //put the device at the end of the queque
                this.deviceIdSharePriority.add(this.deviceIdSharePriority.remove(0));
            }
        }
    }
    
}
