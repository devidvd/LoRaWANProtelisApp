package app.handlers;

import java.util.function.Consumer;

/**
 * Handler for manage the error during the connection phase with TheThingsNetwork server.
 * */
public class ConnectionErrorHandler implements Consumer<Throwable> {

    public ConnectionErrorHandler() {}

    @Override
    public void accept(Throwable error) {
        System.err.println("error: " + error.getMessage());
        System.exit(1);
    }

}
