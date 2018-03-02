package shareneighborstate;

public enum ShareNeighborStatus {
    
    FREE("Free"), PREPARETRANSMISSION("PrepareTransmission"), TRANSMITTING("Transmitting");
    
    protected String value;
    
    private ShareNeighborStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
}
