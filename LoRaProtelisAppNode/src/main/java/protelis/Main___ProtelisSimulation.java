package protelis;

import java.util.ArrayList;
import java.util.List;

import lora.LoRaNodeImpl;

public class Main___ProtelisSimulation {

    public static void main(final String... args) {
        /*TODO N.B.
         * Gateway ID: 0xAD4BAD4BAD4BAD4B
         * Core Board Ip: 137.204.107.148
         * Network Router Ip: 137.204.107.254
         * Default Mask Ip: 255.255.255.0
         * 
         * LoRa Server Ip: 52.169.76.203
         * */
        //set the frequencies list
        List<Integer> nodeFrequencies = new ArrayList<Integer>(); 
        nodeFrequencies.add(868100000);
        nodeFrequencies.add(868300000); 
        nodeFrequencies.add(868500000);
        nodeFrequencies.add(867100000); 
        nodeFrequencies.add(867300000); 
        nodeFrequencies.add(867500000); 
        nodeFrequencies.add(867700000); 
        nodeFrequencies.add(867900000);
        //init the target server ports
        List<Integer> uplinkTargetServerPorts = new ArrayList<Integer>();
        int minUplinkPort = 1;
        int maxUplinkPort = 10;
        for(int i = minUplinkPort; i < maxUplinkPort; i++) {
            uplinkTargetServerPorts.add(i);
        }
        //init serial port parameter
        String comPort = "COM10";
        int baudRate = 9600;
        //init device keys
        String deviceId = "node3";
        //here you have also to initialize the keys to access the application...
        //keys = ....
        //init transmission paramters
        long transmission_freq = 60000;// 1 min
        int power_index = 1;//max power
        int data_rate = 6;//max data rate
        int cr = 0;//coding rate 4/5
        LoRaNodeImpl node;
        try {
            node = new LoRaNodeImpl(deviceId, comPort, baudRate, false);
            node.setTransmissionParams(data_rate, power_index, transmission_freq, cr, uplinkTargetServerPorts);
            //set abp or otaa params
            //set serial abp or otaa params
            //then the node can be able to transmit
            node.closePort();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
}
