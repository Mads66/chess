package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final GameService gameService;
    private final UserService userService;

    public WebSocketHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }


    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case CONNECT -> connect(action.getGameID(), session, action.getAuthToken());
            case LEAVE -> leave(action.getGameID(), action.getAuthToken());
            case RESIGN -> resign(action.getGameID(), action.getAuthToken());
            case MAKE_MOVE -> makeMove(action.getGameID(), session, action.getAuthToken());
        }
    }

    private void connect(int gameID, Session session, String auth) throws Exception {
        connections.add(gameID, session, auth);
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth)) {
            GameData game = gameService.getGame(gameID);
            String gameBoard = new Gson().toJson(game.game());
            var gameNote = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            var message = String.format("joined game %s", gameID);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            connections.localBroadcast(gameID, gameNote, auth);
            connections.broadcast(gameID, notification, auth);
        }
    }

    private void leave(int gameID, String auth) throws IOException {
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth)) {
            connections.remove(gameID, auth);
            var message = String.format("player has left game %s", gameID);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            connections.broadcast(gameID, notification, auth);
        }
    }

    private void resign(int gameID, String auth) throws IOException {
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth)) {
            connections.remove(gameID, auth);
            var message = String.format("player has resigned game %s", gameID);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            connections.broadcast(gameID, notification, auth);
        }
    }

    private void makeMove(int gameID, Session session, String auth) throws IOException {
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth)) {
            try {
                var message = String.format("making move in game %s", gameID);
                var game = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
                connections.localBroadcast(gameID, game, auth);
                connections.broadcast(gameID, game, auth);
            } catch (Exception ex) {
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                connections.localBroadcast(gameID, notification, auth);
            }
        }
    }

    private boolean assertAuth(int gameID, String auth) throws IOException {
        try {
            if (userService.getAuth(auth) == null){
                var message = "user is not authorized";
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                connections.localBroadcast(gameID, notification, auth);
                return false;
            }
        } catch (Exception ex){
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            connections.localBroadcast(gameID,notification,auth);
        }
        return true;
    }

    private boolean assertGameID (int gameID, String auth) throws IOException {
        try {
            if (gameService.getGame(gameID) == null) {
                var message = String.format("game %s not found", gameID);
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                connections.localBroadcast(gameID, notification, auth);
                return false;
            }
        } catch (Exception ex) {
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            connections.localBroadcast(gameID,notification,auth);
        }
        return true;
    }
}
