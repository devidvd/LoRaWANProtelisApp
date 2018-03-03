package protelis.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A class for build the object shared in the system.
 * */
public class ObjectInstanceBuilder {
    
    private List<String> serializedInstance;
    
    public ObjectInstanceBuilder(int totalPieces) {
        this.serializedInstance = new ArrayList<>();
        for(int i = 0; i < totalPieces; i++) {
            this.serializedInstance.add(null);
        }
    }
    
    public void addPiece(int pieceIndex, String content) {
        this.serializedInstance.set(pieceIndex, content);
    }
    
    /**
     * To use in the future retransmission protocol.
     * */
    public List<Integer> getLostPieces() {
        List<Integer> lostPieces = new ArrayList<>();
        int i = 0;
        for(String s : this.serializedInstance) {
            if(s == null){
                lostPieces.add(i);
            }
            i++;
        }
        return lostPieces;
    }
    
    /** 
     * Return the serialized instance as generic @Object. 
     * */
    public Object buildInstance() throws IOException, ClassNotFoundException {
        String instanceByteString = new String("");
        for(String s : this.serializedInstance) {
            instanceByteString.concat(s);
        }
        byte[] javaObjectSerializated = instanceByteString.getBytes();
        byte[] instanceInBase64 = Base64.getDecoder().decode(javaObjectSerializated);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(instanceInBase64));
        Object obj = objectInputStream.readObject();
        objectInputStream.close();
        return obj;
    }
    
}
