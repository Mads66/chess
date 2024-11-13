package client;

import com.sun.nio.sctp.NotificationHandler;
import ui.ChessClient;

public class ClientTests {
    private final ChessClient client;

    public ClientTests() {
        this.client = new ChessClient("http://localhost:8080");
    }
}
