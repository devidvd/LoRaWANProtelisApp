package app;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.thethingsnetwork.data.common.Connection;
import org.thethingsnetwork.data.mqtt.Client;

import app.handlers.ABPDeviceUplinkMsgJoinHandler;
import app.handlers.ConnectionErrorHandler;
import app.handlers.OTAADeviceJoinMsgHandler;
import app.handlers.UplinkMsgHandler;
import shareneighborstate.ShareNeighborStateScheduler;
import util.Pair;

/**
 * Application.
 * */
public class TTNapp {
    
    //server application metadata
    private final String region;
    private final String appId;
    private final String accessKey;
    //all the devices (human) ids included in the application and their relative downlink ports
    private final List<Pair<String, List<String>>> devicesID_and_ports;
    //scheduler for manage the transmission of the neighborstate to each node
    private final ShareNeighborStateScheduler shareNeighborStateScheduler;
    //the object that incorporates the communication with TheThingsNetwork
    private Client client;
    
    public TTNapp(final String region, final String appId, final String accessKey,
            final List<Pair<String, List<String>>> devicesIDs_ports) {
        this.region = region;
        this.appId = appId;
        this.accessKey = accessKey;
        this.devicesID_and_ports = devicesIDs_ports;
        this.shareNeighborStateScheduler = new ShareNeighborStateScheduler(this.devicesID_and_ports);
        try {
            client = new Client(this.region, this.appId, this.accessKey);
        } catch (URISyntaxException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * This method initializes the devices events listeners, 
     * then connects to TTN server.
     * */
    public void connectToTTNServer() {
        /* Add a listener for the "connected" and "error" events to have the 
         * knowledge about the connection to the TTN application*/
        client.onError(new ConnectionErrorHandler());
        client.onConnected((Connection client) -> System.out.println("connected!"));
        //set listeners for each node
        for(Pair<String, List<String>> nodeID : this.devicesID_and_ports) {
            client.onActivation(new OTAADeviceJoinMsgHandler(nodeID.getX(), this.shareNeighborStateScheduler));
            client.onMessage(nodeID.getX(), new ABPDeviceUplinkMsgJoinHandler(nodeID.getX(), this.shareNeighborStateScheduler));
            client.onMessage(nodeID.getX(), new UplinkMsgHandler(nodeID.getX(), this.shareNeighborStateScheduler));
        }
        //start to connect with TTN server
        try {
            client.start();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void startSchedule() throws InterruptedException {
        while(true) {
            TimeUnit.MINUTES.sleep(3);
            this.shareNeighborStateScheduler.schedule();
        }
    }
    
}
