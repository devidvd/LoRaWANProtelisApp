package lora;

import java.util.List;

public interface LoRaNode {

    /**
     * Call to set the OTAA properties.
     * */
    public void setOTAAproperties(final String appKey, final String appEui, final String devEui);
    
    /**
     * Serial initialization of the node keys used in OTAA.
     * 
     * @throws InterruptedException - If happens a serial interruption.
     * */
    public void serialSetDeviceOTAAkeys() throws InterruptedException;
    
    /**
     * Call to set the ABP properties.
     * 
     * */
    public void setABPproperties(final String appSessionKey, final String networkSessionkey, final String devAddr);
    
    /**
     * Serial Initialization of the node keys used in ABP.
     * 
     * @throws InterruptedException - If happens a serial interruption.
     * */
    public void serialSetDeviceABPkeys() throws InterruptedException;
    
    /**
     * Call to set transmission parameters.
     * */
    public void setTransmissionParams(final int loraDataRate, final int powerIndex, 
            final long transmission_freq, int cr, final List<Integer> uplinkTargetServerPorts);
    
    /**
     * Serial initialization of the node transmission parameters: power index, data rate, cr.
     * 
     * @throws InterruptedException - If happens a serial interruption.
     * */
    public void serialSetDeviceTransmissionParams() throws InterruptedException;
    
    /**
     * Serial initialization of the node transmission channels.
     * 
     * @throws InterruptedException - If happens a serial interruption.
     * */
    public void serialSetDeviceChannelsFreq(final List<Integer> frequencies) throws InterruptedException;
    
    /** 
     * Join abp network.
     * 
     * @throws InterruptedException - If happens a serial interruption.
     * */
    public void joinAbp() throws InterruptedException;
    
    /** 
     * Join otaa network.
     * 
     * @throws InterruptedException - If happens a serial interruption.
     * */
    public void joinOtaa() throws InterruptedException;
    
    /**
     * Transmit uplink msg.
     * Return the hexadecimal message receive after transmit the uplink.
     * If the node hasn't receive anything, the string will be initialized with null value.
     * 
     * @throws InterruptedException - If happens a serial interruption.
     */
    public String transmit(final String msgToSend, final boolean confirmedMsg) throws InterruptedException;
    
    /**
     * This method close the serial port. 
     * It must be called before close the application.
     * */
    public void closePort();
    
    public String getDevEUI();
    
    public int getLoraDataRate();
    
    public long getTimeToWaitAfterTransmission();
    
}
