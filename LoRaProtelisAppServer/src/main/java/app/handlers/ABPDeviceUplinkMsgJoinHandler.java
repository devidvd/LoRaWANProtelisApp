package app.handlers;

import java.io.UnsupportedEncodingException;
import java.util.function.BiConsumer;

import org.thethingsnetwork.data.common.messages.DataMessage;
import org.thethingsnetwork.data.common.messages.UplinkMessage;

import shareneighborstate.ShareNeighborStateScheduler;

/**
 * The Handler to manage the LoRaWAN Application-By-Personalization Event.
 * Lorawan don't need the ABP join, but this protelis applications needs to know
 * every time when a device joins the network.
 * */
public class ABPDeviceUplinkMsgJoinHandler implements BiConsumer<String, DataMessage> {

    private final ShareNeighborStateScheduler shareNeighborStateScheduler;
    private final String deviceId;
    
    public ABPDeviceUplinkMsgJoinHandler(final String deviceId, final ShareNeighborStateScheduler shareNeighborStateScheduler) {
        this.deviceId = deviceId;
        this.shareNeighborStateScheduler = shareNeighborStateScheduler;
    }
    
    @Override
    public void accept(String t, DataMessage dataMsg) {
        String payloadString = "empty_payload";
        final UplinkMessage uplinkMsg = (UplinkMessage) dataMsg;
        try {
            payloadString = new String( (uplinkMsg).getPayloadRaw(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        //syntax for recognize uplink msg of ABP join
        if(payloadString.startsWith("!") && payloadString.endsWith("!")) {
            String devEUI = payloadString.substring(1, payloadString.length() - 1);
            try {
                this.shareNeighborStateScheduler.addNodes(deviceId, devEUI);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

}
