package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.protelis.vm.util.CodePath;

import protelis.util.Pair;


public final class Main {
    
    public static void main(final String... args) throws InterruptedException {
        
        Map<CodePath, Object> toSend = new HashMap<>();
        Object o = new Object();
        
        /*
        //initialization of uplink and downlink ports assigned to each device
        List<Pair<String, List<String>>> IdDevicesAndDownPorts = new ArrayList<Pair<String, List<String>>>();
        //first node
        //in the node application, the node will send uplink to ports: 1 - 10
        String deviceIdOne = "node1";
        List<String> downlinkPortsNodeOne = new ArrayList<String>();
        Integer minDownlinkNumberPortNodeOne = 11;
        Integer maxDownlinkNumberPortNodeOne = 20;
        for(Integer i = minDownlinkNumberPortNodeOne; i <= maxDownlinkNumberPortNodeOne; i++) {
            downlinkPortsNodeOne.add(i.toString());
        }
	IdDevicesAndDownPorts.add(new Pair<>(deviceIdOne, downlinkPortsNodeOne));
	//second node
	//....
	//create connection to TheThingsNetwork and run the application
	TTNapp connection = new TTNapp("eu", "ihopethisisthelastapp", "ttn-account-v2.tiHCEaAlJj8O5UMyUPTit9ZvHkvU_dWrUvp-kd_7z20", IdDevicesAndDownPorts);
	connection.connectToTTNServer();
	connection.startSchedule();
	*/
    }
    
}