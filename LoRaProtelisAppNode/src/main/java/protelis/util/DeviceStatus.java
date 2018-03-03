package protelis.util;

/**
 * The class which represent the transmission status of 
 * a protelis node. 
 * */
public enum DeviceStatus {
    
    /**
     * NOT_JOINED means that the node is not joined 
     * in accordance with this java application and it's 
     * different from the LoRa join. This means that the 
     * protelis application is doing a cycle and the @ProtelisLoRaNodeThread is never been started.
     * */
    NOT_JOINED,
    /**
     * NOT_JOINED means that the node is joined in 
     * accordance with this java application and it's 
     * different from the LoRa join. This means that 
     * the  @ProtelisLoRaNodeThread has started and the 
     * backend application has stored his DevEUI in 
     * protelis neighborState field and the node is not 
     * sharing its protelis state at the moment. The next 
     * time that it will transmit, it will start a 
     * sharing procedure of his state.
     * */
    JOINED,
    /**
     * TRANSMITTING means that the node is 
     * currently sharing its state.
     * */
    TRANSMITTING,
    /**
     * RECEIVING means that the node is 
     * currently receiving the neighborState.
     * */
    RECEIVING;
}
