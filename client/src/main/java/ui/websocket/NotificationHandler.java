package ui.websocket;

import websocket.messages.Notification;

public interface NotificationHandler {
    void notify(Notification notification);
}
