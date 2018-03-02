package app.handlers;

import java.io.UnsupportedEncodingException;
import java.util.function.BiConsumer;

import org.thethingsnetwork.data.common.messages.DataMessage;
import org.thethingsnetwork.data.common.messages.UplinkMessage;

import shareneighborstate.SchedulerStatus;
import shareneighborstate.ShareNeighborStateScheduler;

/**
 * Handler for receive uplink event.
 * */
public class UplinkMsgHandler implements BiConsumer<String, DataMessage> {
    
    private final ShareNeighborStateScheduler shareNeighborStateScheduler;
    private String deviceID;
    
    public UplinkMsgHandler(final String deviceID, final ShareNeighborStateScheduler shareNeighborStateScheduler) {
        super();
        this.deviceID = deviceID;
        this.shareNeighborStateScheduler = shareNeighborStateScheduler;
    }
    
    @Override
    public void accept(String t, DataMessage u) {
        String payloadString = "empty payload";
        final UplinkMessage uplinkRx = (UplinkMessage) u;
        try {
            payloadString = new String( (uplinkRx).getPayloadRaw(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        //il payloadstring conterrà il proprio sharestate + /n per comunicare al server l'ultimo pezzo di informazione ricevuto
        if(payloadString.endsWith("/9") || payloadString.endsWith("/8") || payloadString.endsWith("/7")
                || payloadString.endsWith("/6") || payloadString.endsWith("/5") || payloadString.endsWith("/4")
                || payloadString.endsWith("/3") || payloadString.endsWith("/2") || payloadString.endsWith("/1")) {
            SchedulerStatus.getInstance().updateCounterFrame(deviceID);
        } //case the uplink has receive the last one data frame
        else if(payloadString.endsWith("/0")) {
            SchedulerStatus.getInstance().updateCounterFrame(deviceID);
            SchedulerStatus.getInstance().setFreeStatus();
        }

        //4 - schedule the downlink to a specific device and into a specific port
        /*try {
            final String downlinkPayload = counterPayload.toString();
            final byte[] downlinkPayloadBytes = downlinkPayload.getBytes();
            final DownlinkMessage downlink = new DownlinkMessage(Integer.parseInt(this.portOfNodeTarget), downlinkPayloadBytes);
            client.send(nodeTarget, downlink);
        } catch (Exception e) {
            System.out.println("Error during the schedule of downlink");
        }*/
        
    }

}
