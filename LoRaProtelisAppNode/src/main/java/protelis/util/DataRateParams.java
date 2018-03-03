package protelis.util;

/**
 * Enumeration than contains data rate LoRa index, bit/s and max payload size.
 * */
public enum DataRateParams {
    
    ZERO(0, 250, 51), ONE(1, 440, 51), TWO(2, 980, 51), THREE(3, 1760, 121), 
    FOUR(4, 3125, 222), FIVE(5, 5470, 222), SIX(6, 11000, 222), SEVEN(7 ,50000, 222);

    private final int data_rate;
    private final int bit;
    private final int max_payload;
    
    private DataRateParams(int data_rate, int bit, int max_payload){ 
        this.data_rate = data_rate;
        this.bit = bit;
        this.max_payload = max_payload;
    }
    
    public int getBit() {
        return bit; 
    }
    
    public int getDataRate() {
        return this.data_rate;
    }
    
    public int getMaxPayload() {
        return this.max_payload;
    }
    
    public static int getMaxPayloadSize(int data_rate) {
        if(data_rate == ZERO.getDataRate()) {
            return ZERO.getMaxPayload();
        } else if(data_rate == ONE.getDataRate()) {
            return ONE.getMaxPayload();
        } else if(data_rate == TWO.getDataRate()) {
            return TWO.getMaxPayload();
        } else if(data_rate == THREE.getDataRate()) {
            return THREE.getMaxPayload();
        } else if(data_rate == FOUR.getDataRate()) {
            return FOUR.getMaxPayload();
        } else if(data_rate == FIVE.getDataRate()) {
            return FIVE.getMaxPayload();
        } else if(data_rate == SIX.getDataRate()) {
            return SIX.getMaxPayload();
        } else if(data_rate == SEVEN.getDataRate()) {
            return SEVEN.getMaxPayload();
        }
        try {
            throw new Exception("data rate not selected!");
        } catch (Exception e) {
            System.err.println("data rate not selected!");
            e.printStackTrace();
            System.exit(1);
        }
        return -1;
    }
    
}
