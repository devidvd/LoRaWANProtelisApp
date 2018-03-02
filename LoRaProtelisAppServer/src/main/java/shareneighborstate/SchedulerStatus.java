package shareneighborstate;

/**
 * A singleton class for manage the downlinks transmission status.
 * */
public class SchedulerStatus {
    
    private static SchedulerStatus instance = null;
    private String deviceIDinCommunication;
    private int counterCommunicationFrame;
    private ShareNeighborStatus status;
    
    protected SchedulerStatus() {
        this.deviceIDinCommunication = "";
        this.counterCommunicationFrame = -1;
        this.status = ShareNeighborStatus.FREE;
    }
    
    public static SchedulerStatus getInstance() {
        if(instance == null) {
            instance = new SchedulerStatus();
        }
        return instance;
    }
    
    /**
     * Method called by the @ShareNeighborStateScheduler at each round 
     * to determines its behavior.
     * */
    public synchronized ShareNeighborStatus getStatus() {
        return this.status;
    }
    
    /**
     * Method called by the @UplinkMsgHandler when receives the last 
     * confirmation frame.
     * */
    public synchronized void setFreeStatus() {
        this.status = ShareNeighborStatus.FREE;
    }
    
    /**
     * */
    public synchronized void prepareDownlinkTransmission() {
        
    }
    
    /**
     * Method called by the @ShareNeighborStateScheduler to start 
     * deliver the downlinks for the specific node.
     * 
     * @throws Exception 
     * */
    public synchronized void startDownlinkCommunication(String deviceID, int counterCommunicationFrame) throws Exception {
        if(this.counterCommunicationFrame == -1 && this.status == ShareNeighborStatus.FREE) {
            this.status = ShareNeighborStatus.TRANSMITTING;
            this.deviceIDinCommunication = deviceID;
            this.counterCommunicationFrame = counterCommunicationFrame;
        } else {
            if (this.counterCommunicationFrame != -1 && this.status != ShareNeighborStatus.FREE) {
                throw new Exception("Error! The counterCommunicationFrame = " + this.counterCommunicationFrame 
                    + ". Error! The status = " + this.status.getValue());
            } else if(this.counterCommunicationFrame != -1) {
                throw new Exception("Error! The counterCommunicationFrame = " + this.counterCommunicationFrame);
            } else if(this.status != ShareNeighborStatus.FREE) {
                throw new Exception("Error! The status = " + this.status.getValue());
            } else {
                throw new Exception("unknow error");
            }
        }
    }
    
    /**
     * Method called by @UplinkMsgHandler when receive the
     * */
    public synchronized boolean updateCounterFrame(String deviceID) {
        if(deviceID == this.deviceIDinCommunication) {
            this.counterCommunicationFrame--;
            return true;
        }
        return false;
    }
}
