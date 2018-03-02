package app.handlers;

import java.util.function.BiConsumer;

import org.thethingsnetwork.data.common.messages.ActivationMessage;

import shareneighborstate.ShareNeighborStateScheduler;

/**
 * The Handler to manage the LoRaWAN join Over-The-Air-Autentication Event.
 * */
public class OTAADeviceJoinMsgHandler implements BiConsumer<String, ActivationMessage> {

    private final ShareNeighborStateScheduler nodesQueque;
    private final String deviceId;
    
    public OTAADeviceJoinMsgHandler(final String deviceId, final ShareNeighborStateScheduler shareNeighborStateScheduler) {
        this.deviceId = deviceId;
        this.nodesQueque = shareNeighborStateScheduler;
    }
    
    @Override
    public void accept(final String t, final ActivationMessage activationMsg) {
        try {
            this.nodesQueque.addNodes(deviceId, activationMsg.getDevEui());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
