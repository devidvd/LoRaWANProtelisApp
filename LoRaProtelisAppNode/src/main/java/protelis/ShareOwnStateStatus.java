package protelis;


public enum ShareOwnStateStatus {
    
    FREE("Free"), TRANSMITTING("Transmitting");
    
    protected String value;
    
    private ShareOwnStateStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
