package protelis.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for manage the transmission of an object.
 * */
public class ObjectPiecesToSend {

    private final List<String> serializedInstance;
    private int numberMsgSentIndex;
    private final int twoByte = 10;//TODO change also this
    private final int communicationProtocolIndexing = 4;
    
    public ObjectPiecesToSend(final String serializedObject, final int data_rate) {
        this.serializedInstance = new ArrayList<>();
        this.numberMsgSentIndex = 0;
        final int payloadByteSize = (DataRateParams.getMaxPayloadSize(data_rate)) - communicationProtocolIndexing;
        final Integer totalPieces = serializedObject.length() / payloadByteSize;
        final boolean sendVeryLastMsg;
        Integer index;
        String payload;
        if(serializedObject.length() % payloadByteSize > 0) {
            sendVeryLastMsg = true;
        } else {
            sendVeryLastMsg = false;
        }
        for(index = 0; index < totalPieces; index++) {
            payload = serializedObject.substring( (payloadByteSize * index), 
                    ( (payloadByteSize * index) + payloadByteSize));
            createMsgPayload(index, totalPieces, payload);
        }
        if(sendVeryLastMsg) {
            payload = serializedObject.substring(payloadByteSize * index);
            createMsgPayload(index, totalPieces, payload);
        }
    }
    
    public void createMsgPayload(final Integer index, final Integer totalPieces, final String payload) {
        //TODO change these confrontation, confront the byte size instead the int value
        if(index < twoByte && totalPieces < twoByte) {
            this.serializedInstance.add("0" + index.toString() + "0" + totalPieces.toString() + payload);
        } else if(index > twoByte && totalPieces > twoByte){
            this.serializedInstance.add("" + index.toString() + "" + totalPieces.toString() + payload);
        } else if(index < twoByte && totalPieces > twoByte) {
            this.serializedInstance.add("0" + index.toString() + "" + totalPieces.toString() + payload);
        }  else if(index > twoByte && totalPieces < twoByte) {
            this.serializedInstance.add("" + index.toString() + "0" + totalPieces.toString() + payload);
        }
    }
    
    /*
    public void addPiece(final int pieceIndex, final String content) {
        this.serializedInstance.set(pieceIndex, this.serializedInstance.get(pieceIndex).concat(content));
    }
    */
    
    /**
     * Return the next packet content to transmit.
     * 
     * @return null when all the packet are all transmitted.
     * */
    public String getNextPiece() {
        if(allPiecesTransmitted()) {
            return null;
        }
        final String nextPiece = this.serializedInstance.get(this.numberMsgSentIndex);
        this.numberMsgSentIndex++;
        return nextPiece;
    }
    
    public boolean allPiecesTransmitted() {
        if(this.numberMsgSentIndex == this.serializedInstance.size()) {
            return true;
        }
        return false;
    }
    
    public String getPiece(final int pieceIndex, final String content) {
        return this.serializedInstance.get(pieceIndex);
    }
    
}
