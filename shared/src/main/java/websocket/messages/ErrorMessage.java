package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private String error;

    public ErrorMessage(ServerMessageType type, String error) {
        super(type);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}

