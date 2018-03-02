package app;

import java.io.*;
import java.util.*;

import org.protelis.vm.util.CodePath;

import gnu.trove.list.TByteList;
import gnu.trove.list.array.TByteArrayList;


public class SerializationSample implements Serializable {
    
    private static final long serialVersionUID = 3474837123047412128L;

    private String aString = "The value of that string";
    private int someInteger = 11111;

    
    public static void main( String [] args ) throws IOException  { 
        
        SerializationSample instance = new SerializationSample();
        /*
         * può il serve ricostruire il codepath con la TByteList
         * */
        
        byte[] values = new String("1234567890 1234567890 1234567890 1234567890 "
                + "1234567890 1234567890 1234567890 1234567890").getBytes();
        TByteList list = new TByteArrayList(values);
        CodePath codePath = new CodePath(list);
        
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("provaDimensioni2.txt")));
        
        oos.writeObject( codePath );
        oos.close();
    }
}