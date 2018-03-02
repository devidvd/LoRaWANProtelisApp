package protelis.util;

import org.protelis.lang.datatype.DeviceUID;

/**
 * A class which represent the LoRaWAN parameter: Device Extended-Unique-Id.
 * It implements the protelis DeviceUID interface because both this parameters
 * needs to be unique in the system.
 * */
public class DevEUI implements DeviceUID {
    
    private String devEUI;
    private static final long serialVersionUID = -173772232920291908L;
    
    public DevEUI(String devEUI) {
        this.devEUI = devEUI;
    }
    
    public String getValue() {
        return this.devEUI;
    }
    
}
