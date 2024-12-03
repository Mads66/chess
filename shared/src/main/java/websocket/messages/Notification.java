package websocket.messages;

import com.google.gson.Gson;

public class Notification extends ServerMessage {
    private ServerMessage messageType;
    private String message;

    public Notification(ServerMessage.ServerMessageType messageType, String message) {
        super(messageType);
        this.message = message;
    }

    public ServerMessage getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return new Gson().toJson(message);
    }
}
