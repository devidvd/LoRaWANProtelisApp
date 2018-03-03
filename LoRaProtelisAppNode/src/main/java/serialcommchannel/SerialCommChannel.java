package serialcommchannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;


public class SerialCommChannel implements CommChannel, SerialPortEventListener {

        private final SerialPort serialPort;
        private final BufferedReader input;//InputStream of the serial port
        private final OutputStream output;//OutputStream of the serial port
        private final BlockingQueue<String> queue;//queque which stores messages from the serial port

        public SerialCommChannel(String port, int rate) throws Exception {
                queue = new ArrayBlockingQueue<String>(100);
                //get the port that must be open
                final CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);
                //open serial port
                serialPort = (SerialPort) portId.open(this.getClass().getName(), 2000);
                //set port parameters
                serialPort.setSerialPortParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, 
                        SerialPort.PARITY_NONE);
                //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                //open the streams
                input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                output = serialPort.getOutputStream();

                //add event listeners
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
        }

        @Override
        public void sendMsg(String msg) {
                final char[] array = msg.toCharArray();
                final byte[] bytes = new byte[array.length];
                for (int i = 0; i < array.length; i++){
                        bytes[i] = (byte) array[i];
                }
                try {
                        output.write(bytes);
                        output.flush();
                } catch(Exception ex){
                        ex.printStackTrace();
                }
        }

        @Override
        /**
         * Just return the message in the queque.
         */
        public String receiveMsg() throws InterruptedException {
                return queue.take();
        }

        @Override
        /**
         * Check if there is a new message in the serial port.
         */
        public boolean isMsgAvailable() {
                return !queue.isEmpty();
        }

        /**
         * This should be called when you stop using the port.
         * This will prevent port locking on platforms like Linux.
         */
        public synchronized void close() {
                if (serialPort != null) {
                        serialPort.removeEventListener();
                        serialPort.close();
                }
        }

        /**
         * Handle an event on the serial port. Read the data and print it in case of exception.
         */
        //@Override
        public synchronized void serialEvent(SerialPortEvent oEvent) {
                if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                        try {
                                //take the message from the serial port...
                                final String msg = input.readLine();
                                //and put it in the message queque!
                                queue.put(msg);
                        } catch (Exception e) {
                                System.err.println(e.toString());
                        }
                }
                //Ignore all the other events
        }

}
