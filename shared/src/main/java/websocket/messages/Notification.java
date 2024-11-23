package websocket.messages;

import com.google.gson.Gson;

public class Notification {
    private ServerMessage messageType;
    private String message;

    public Notification(ServerMessage messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    public ServerMessage getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return new Gson().toJson(message);
    }
}
