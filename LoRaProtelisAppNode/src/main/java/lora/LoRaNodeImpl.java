package lora;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import serialcommchannel.SerialCommChannel;

/**
 * This class represents the LoRa node drived by this application.
 * */
public class LoRaNodeImpl implements LoRaNode {
    
    private final String deviceId;
    //OTAA properties
    private String appKey;
    private String appEui;
    private String devEui;
    //ABP properties
    private String appSessionKey;
    private String networkSessionkey;
    private String devAddr;
    //transmission parameters
    private boolean joinWithOTAA;
    private Integer loraDataRate;
    private Integer powerIndex;
    private long wait_time_after_transmit;
    private String codingRate;
    private List<Integer> uplinkTargetServerPorts;
    private int portListsPosition;
    //manage the serial channel
    private final SerialCommChannel channel;
    
    
    public LoRaNodeImpl(final String deviceId, final String serialPort, final int serialDataRate, final boolean joinWithOTAA) throws Exception {
        this.deviceId = deviceId;
        this.joinWithOTAA = joinWithOTAA;
        this.channel = new SerialCommChannel(serialPort, serialDataRate);
        initSerialPort();
    }
    
    @Override
    public void setOTAAproperties(final String appKey, final String appEui, 
            final String devEui) {
        this.appKey = appKey;
        this.appEui = appEui;
        this.devEui = devEui;
    }
    
    
    @Override
    public void setABPproperties(final String appSessionKey, 
                final String networkSessionkey, final String devAddr) {
        this.appSessionKey = appSessionKey;
        this.networkSessionkey = networkSessionkey;
        this.devAddr = devAddr;
    }
    
    @Override
    public void setTransmissionParams(final int loraDataRate, final int powerIndex, 
                final long transmission_freq, int cr, final List<Integer> uplinkTargetServerPorts) {
        
        this.loraDataRate = loraDataRate;
        this.powerIndex = powerIndex;
        this.wait_time_after_transmit = transmission_freq;
        this.uplinkTargetServerPorts = uplinkTargetServerPorts;
        switch(cr) {
            case 0:
                this.codingRate = "4/5";
                break;
            case 1:
                this.codingRate = "4/6";
                break;
            case 2:
                this.codingRate = "4/7";
                break;
            case 3:
                this.codingRate = "4/8";
                break;
        }
    }
    
    /**
     * Intialize the port before start using it. Sometimes 
     * in the port could be stored a message, so it must 
     * be removed before start the serial communication.
     * 
     * @throws InterruptedException
     */
    private void initSerialPort() throws InterruptedException {
        if(channel.isMsgAvailable()) {
            String emptyMsg = channel.receiveMsg();
            System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                    + ": Remove this message from nodePort: " + emptyMsg);
        }
    }
    
    /**
     * This method manage a simple serial communication with the node. 
     * It must be used ONLY if the node can answer in two ways: "ok" or "invalid_param".
     * 
     * @throws InterruptedException 
     * */
    private void serialSimpleSetLoRaParam(String serialCommand, String value, String ok_msg, String invalid_param_msg) throws InterruptedException {
        String msg;
        boolean wait_msg = true;
        System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                + ": " + serialCommand + " " + value);
        channel.sendMsg(serialCommand + " " + value + "\r\n");
        while(wait_msg) {
            if(channel.isMsgAvailable()) {
                wait_msg = false;
                msg = channel.receiveMsg();
                if(msg.contains("ok")) {
                    System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                            + ": " + ok_msg);
                } else if(msg.contains("invalid_param")) {
                    System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                            + ": " + invalid_param_msg);
                    closePort();
                    System.exit(1);
                }
            }
        }
    }
    
    @Override
    public void serialSetDeviceOTAAkeys() throws InterruptedException {
        //set the appkey
        serialSimpleSetLoRaParam("mac set appkey", this.appKey.toString(), 
                "appKey set successfully", "Error occurs during appKey set... restart the application");
        //set the appeui
        serialSimpleSetLoRaParam("mac set appeui", this.appEui.toString(),
                "appEui set successfully", "Error occurs during appKey set... restart the application");
        //set the devEui
        serialSimpleSetLoRaParam("mac set deveui", this.devEui.toString(), 
                "devEui set successfully", "Error occurs during devEui set... restart the application");
        System.out.println("\n");
    }
    
    @Override
    public void serialSetDeviceABPkeys() throws InterruptedException {
        //set nwkskey
        serialSimpleSetLoRaParam("mac set nwkskey", this.networkSessionkey, 
                "network session key set successfully", "Error occurs during network session key set... restart the application");
        //set appskey
        serialSimpleSetLoRaParam("mac set appskey", this.appSessionKey, 
                "application session key set successfully", "Error occurs during application session key set... restart the application");
        //set devaddr
        serialSimpleSetLoRaParam("mac set devaddr", this.devAddr, 
                "device address set successfully", "Error occurs during device address set... restart the application");
        System.out.println("\n");
    }
    
    @Override
    public void serialSetDeviceTransmissionParams() throws InterruptedException {
        //set data rate
        serialSimpleSetLoRaParam("mac set dr", this.loraDataRate.toString(), 
                "data rate set successfully", "Error occurs during data rate set, maybe is not allowed?... restart the application");
        //set the power index
        serialSimpleSetLoRaParam("mac set pwridx", this.powerIndex.toString(), 
                "power index set successfully", "Error occurs during power index set, maybe is not allowed?... restart the application");
        //set the coding rate
        serialSimpleSetLoRaParam("radio set cr", this.codingRate, 
                "coding rate set successfully", "Error occurs during coding rate set... restart the application");
        System.out.println("\n");
    }
    
    @Override
    public void serialSetDeviceChannelsFreq(final List<Integer> frequencies) throws InterruptedException {
        String msg;
        boolean wait_msg;
        /* 
         * band g1, standard LoRa Channel EU
         * 868100000 = channel 0
         * 868300000 = channel 1
         * 868500000 = channel 2
         * */
        /* Note for microchip Node RN2483 (and maybe even for other models of node): 
         * the order of this serial settings should not be changed. Those commands 
         * must be send in this specific order or node will not respond in the proper way.
         */
        for(int ch = 0; ch < frequencies.size(); ch++) {
            wait_msg = true;
            //set channel frequency
            System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                    + ": mac set ch freq " + ch + " " + frequencies.get(ch));
            channel.sendMsg("mac set ch freq " + ch + " " + frequencies.get(ch) + "\r\n");
            Thread.sleep(500);
            while(wait_msg) {
                if(channel.isMsgAvailable()){
                    wait_msg = false;
                    msg = channel.receiveMsg();
                    System.out.println("direct answer from serial port: " + msg);
                }
            }
            //set channel data rate range
            wait_msg = true;
            System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                    + ": mac set ch drrange " + ch + " 0 7 ");
            channel.sendMsg("mac set ch drrange " + ch + " 0 7 " + "\r\n");
            Thread.sleep(500);
            while(wait_msg) {
                if(channel.isMsgAvailable()){
                    wait_msg = false;
                    msg = channel.receiveMsg();
                    System.out.println("direct answer from serial port: " + msg);
                }
            }
            //set channel duty cycle
            wait_msg = true;
            System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                    + ": mac set ch dcycle " + ch + " 990 ");
            channel.sendMsg("mac set ch dcycle " + ch + " 990 " + "\r\n");
            Thread.sleep(500);
            while(wait_msg) {
                if(channel.isMsgAvailable()){
                    wait_msg = false;
                    msg = channel.receiveMsg();
                    System.out.println("direct answer from serial port: " + msg);
                }
            }
            //set channel status
            wait_msg = true;
            System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                    + ": mac set ch status " + ch + " on");
            channel.sendMsg("mac set ch status " + ch + " on" + "\r\n");
            Thread.sleep(500);
            while(wait_msg) {
                if(channel.isMsgAvailable()){
                    wait_msg = false;
                    msg = channel.receiveMsg();
                    System.out.println("direct answer from serial port: " + msg);
                }
            }
        }
        
    }
    
    @Override
    public void joinAbp() throws InterruptedException {
        this.joinWithOTAA = false;
        System.out.println(Calendar.getInstance().getTimeInMillis() + ": mac join abp");
        channel.sendMsg("mac join abp" + "\r\n");
        /* The answer that follow the command "mac join abp" is too fast 
         * and cause interfearence problem with the serial connection. 
         * Waiting 1 seconds and remove the answer received, will not 
         * cause any problem because the ABP mode has always a positive join answer.*/
        Thread.sleep(1000);
        this.channel.receiveMsg();
    }
    
    @Override
    public void joinOtaa() throws InterruptedException {
        this.joinWithOTAA = true;
        boolean keep_join = true;
        String msg;
        while(keep_join) {
            boolean wait_msg = true;
            boolean ok = false;
            System.out.println(Calendar.getInstance().getTimeInMillis() + ": mac join otaa");
            channel.sendMsg("mac join otaa" + "\r\n");
            //wait the 1° response from node
            while(wait_msg) {
                if(channel.isMsgAvailable()) {
                    msg = channel.receiveMsg();
                    //manage all the responses which can be received
                    if(msg.contains("ok")) {
                        ok = true;
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                + ": ok - It's ok, the node has send the join request.");
                    } else if(msg.contains("invalid_param")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                + ": invalid_param - The node don't know that command");
                        System.out.println("Solution: adjust the code and run the program again.");
                        closePort();
                        System.exit(1);
                    } else if(msg.contains("keys_not_init")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": keys_not_init - the keys are not configured!");
                        System.out.println("Solution: adjust the keys and run the program again.");
                    } else if(msg.contains("no_free_ch")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": no_free_ch - no channel available");
                        System.out.println("Solution: the program will wait 10 minute then will retry to join.");
                        TimeUnit.MINUTES.sleep(10);
                    } else if(msg.contains("silent")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": silent - the device is in silent mode...");
                        System.out.println("Solution: the node needs to be awaken, then restart the application again.");
                        closePort();
                        System.exit(1);
                    } else if(msg.contains("busy")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": busy - the device is busy.");
                        System.out.println("Solution: just wait the device ends the operation in progress, the program will wait 5 minutes then will retry to join");
                        TimeUnit.MINUTES.sleep(5);
                    } else if(msg.contains("mac_paused")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": mac_paused - the mac was paused and not resumed");
                        System.out.println("Solution: resume the mac, then restart the application.");
                        closePort();
                        System.exit(1);
                    }
                    //start wait the 2° response from node
                    if(ok) {
                        while(wait_msg) {
                            if(channel.isMsgAvailable()) {
                                msg = channel.receiveMsg();
                                if(msg.contains("accepted")) {
                                    System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                            + ": it's accepted!!");
                                    wait_msg = false;
                                    keep_join = false;
                                    ok = false;
                                } else if(msg.contains("denied")) {
                                    System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                            + ": it's denied, there's must be something wrong with the keys");
                                    wait_msg = false;
                                }
                            }
                        }//end wait loop of the 2° response
                    }
                }
            }//end wait loop of the 1° respose*/
            Thread.sleep(5000);
            this.channel.receiveMsg();
            keep_join = false;
        }//end of keep join loop
        //wait the transmission time amount, before start transmit
        System.out.println("Wait " + this.wait_time_after_transmit + " milliseconds, before begin to transmit.");
        Thread.sleep(this.wait_time_after_transmit);
    }
    
    @Override
    public String transmit(final String msgToSend, final boolean confirmedMsg) throws InterruptedException {
        String msgReceived = null;
        //convert the payloadCounter into hexdecimal data before send it
        /*String msgToSend = counterPayload.toString();
        byte[] msgToSendBytes = msgToSend.getBytes(StandardCharsets.UTF_8);
        String msgToSendHex = DatatypeConverter.printHexBinary(msgToSendBytes);
        */
        boolean keep_transmit = true;
        //start try to transmit until a transmission has success
        while(keep_transmit) {
            boolean wait_msg = true;
            String msg;
            boolean ok = false;
            //check if the port range is over of index
            if((this.uplinkTargetServerPorts.size() - 1) == portListsPosition) {
                portListsPosition = 0;
            }
            if(confirmedMsg) {
                System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                        + " mac tx cnf " + this.uplinkTargetServerPorts.get(portListsPosition) + " " + msgToSend);
                channel.sendMsg("mac tx cnf " + this.uplinkTargetServerPorts.get(portListsPosition) + " " + msgToSend + "\r\n");
            } else {
                System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                        + " mac tx uncnf " + this.uplinkTargetServerPorts.get(portListsPosition) + " " + msgToSend);
                channel.sendMsg("mac tx uncnf " + this.uplinkTargetServerPorts.get(portListsPosition) + " " + msgToSend + "\r\n");
            }
            this.portListsPosition++;//each transmission will be stored in a different port
            //wait the 1° response from node
            while(wait_msg) {
                if(channel.isMsgAvailable()) {
                    msg = channel.receiveMsg();
                    //manage all the responses which can be received
                    if(msg.contains("ok")) {
                        ok = true;
                        System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                + ": ok - Nothing to do it's ok");
                    } else if(msg.contains("invalid_param")) {
                        System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                + ": invalid_param - The node don't know that command");
                        System.out.println("Solution: adjust the code and run the program again");
                        closePort();
                        System.exit(1);
                    } else if(msg.contains("not_joined")) {
                        System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": not_joined - the node must be joined to the network... start the join procedure");
                        if(this.joinWithOTAA) {
                            joinOtaa();
                        } else {
                            joinAbp();
                        }
                        wait_msg = false;
                    } else if(msg.contains("no_free_ch")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": no_free_ch - no channel available");
                        System.out.println("Solution: the program will wait 10 minute then will retry to transmit.");
                        TimeUnit.MINUTES.sleep(10);
                        wait_msg = false;
                    } else if(msg.contains("silent")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": silent - the device is in silent mode...");
                        System.out.println("Solution: the node needs to be awaken, then restart the application again.");
                        closePort();
                        System.exit(1);
                    } else if(msg.contains("frame_counter_err_rejoin_needed")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": frame counter error - the device has an internal problem.");
                        System.out.println("Solution: the node needs to join again, the application will now start join request.");
                        if(this.joinWithOTAA) {
                            joinOtaa();
                        } else {
                            joinAbp();
                        }
                    } else if(msg.contains("busy")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": busy - the device is busy.");
                        System.out.println("Solution: just wait the device ends the operation in progress, the program will wait 5 minutes then will retry to join");
                        TimeUnit.MINUTES.sleep(5);
                    } else if(msg.contains("mac_paused")) {
                        System.out.println(this.deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": mac_paused - the mac was paused and not resumed");
                        System.out.println("Solution: resume the mac, then restart the application.");
                        closePort();
                        System.exit(1);
                    } else if(msg.contains("invalid_data_len")) {
                        //solution: decrease payload lenght
                        System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis())
                                + ": invalid_data_len - the payload lenght is greater than the maximum lenght allow by the current data rate!");
                        System.out.println("Solution: fix the code and then restart the application.");
                        closePort();
                        System.exit(1);
                    }
                    //start wait the 2° response from node
                    if(ok) {
                        while(wait_msg) {
                            if(channel.isMsgAvailable()) {
                                msg = channel.receiveMsg();
                                if(msg.contains("mac_tx")) {
                                    System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                            + ": has transmit!");
                                    wait_msg = false;
                                    keep_transmit = false;
                                } else if(msg.contains("mac_rx")) {
                                    System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                            + ": has transmit and receive!");
                                    System.out.println("full message receive from serial port: " + msg);
                                    //remove the prefix
                                    int indexToCut = new String("mac_rx ").length();
                                    msg = msg.substring(indexToCut);
                                    int indexOfPayload = (msg.indexOf(" ", 0)) + 1;
                                    String hexMsg = msg.substring(indexOfPayload);
                                    System.out.println("message received in hex format: " + hexMsg.toString());
                                    msgReceived = hexMsg;
                                    wait_msg = false;
                                    keep_transmit = false;
                                } else if(msg.contains("mac_error")) {
                                    System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                            + ": mac error occurs");
                                    wait_msg = false;
                                    keep_transmit = false;
                                } else if(msg.contains("invalid_data_len")) {
                                    System.out.println(deviceId + " " + new Timestamp(Calendar.getInstance().getTimeInMillis()) 
                                            + ": invalid data lenght... maybe the downlink payload lenght isn't allow!");
                                    //these inserts are the right ones
                                    wait_msg = false;
                                    keep_transmit = false;
                                }
                            }
                        }//wait the 2° response
                    }
                }
            }//wait 1° response
        }//keep_trasmit
        return msgReceived;
    }
    
    @Override
    public void closePort() {
        this.channel.close();
    }
    
    @Override
    public String getDevEUI() {
        return this.devEui;
    }
    
}
