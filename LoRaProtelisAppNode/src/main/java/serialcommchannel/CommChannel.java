package serialcommchannel;

public interface CommChannel {
        
        /**
         * Send message to the serial port.
         */
        void sendMsg(String msg);
        
        /**
         * Receive message from the serial port.
         */
        String receiveMsg() throws InterruptedException;

        /**
         * Check if a message is available on the serial port.
         */
        boolean isMsgAvailable();

}
