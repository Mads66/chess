package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, ResponseException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case CONNECT -> connect(action.getGameID(), session);
            case LEAVE -> leave(action.getGameID());
            case RESIGN -> resign(action.getGameID());
            case MAKE_MOVE -> makeMove(action.getGameID(), session);
        }
    }

    private void connect(int gameID, Session session) throws IOException {
        connections.add(gameID, session);
        var message = String.format("joined game %s", gameID);
        var notification = new Notification(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME), message);
        connections.broadcast(gameID, notification);
    }

    private void leave(int gameID) throws IOException {
        connections.remove(gameID);
        var message = String.format("player has left game %s", gameID);
        var notification = new Notification(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION), message);
        connections.broadcast(gameID, notification);
    }

    private void resign(int gameID) throws IOException {
        connections.remove(gameID);
        var message = String.format("player has resigned game %s", gameID);
        var notification = new Notification(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION), message);
        connections.broadcast(gameID, notification);
    }

    private void makeMove(int gameID, Session session) throws ResponseException {
        try {
            var message = String.format("making move ing game %s", gameID);
            var notification = new Notification(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION), message);
            connections.broadcast(gameID, notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}
