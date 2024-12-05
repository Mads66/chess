package ui.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import ui.ChessBoard;
import ui.ChessClient;
import websocket.commands.GameCommand;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    ChessClient chessClient;
    private final Map<UserGameCommand.CommandType, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();


    public WebsocketFacade(String url, NotificationHandler notificationHandler, ChessClient chessClient) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;
            this.chessClient = chessClient;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    extracted(message, notificationHandler, chessClient);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void extracted(String message, NotificationHandler notificationHandler, ChessClient chessClient) {
        try {
            // Parse the common fields to determine message type
            ServerMessage baseMessage = new Gson().fromJson(message, ServerMessage.class);

            CompletableFuture<String> future = pendingResponses.remove(baseMessage.getServerMessageType());
            if (future != null) {
                future.complete(message);
                return;
            }

            switch (baseMessage.getServerMessageType()) {
                case NOTIFICATION -> {
                    NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);

                    notificationHandler.notify(new Notification(notification.getServerMessageType(), notification.getMessage()));
                }
                case LOAD_GAME -> {
                    LoadGameMessage loadGameMessage = new Gson().fromJson(message, LoadGameMessage.class);
                    GameData gameData = loadGameMessage.getGame();
                    System.out.println("\n");
                    if (chessClient.getMyTeamColor() == ChessGame.TeamColor.WHITE) {
                        ChessBoard.main(gameData.game().getBoard(), null, true);
                    } else{
                        ChessBoard.main(gameData.game().getBoard(), null, false);
                    }
                    chessClient.updateGameData(gameData);
                    notificationHandler.notify(new Notification(loadGameMessage.getServerMessageType(), "New board"));
                }
                case ERROR -> {
                    ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
                    notificationHandler.notify(new Notification(errorMessage.getServerMessageType(), errorMessage.getError()));
                }
                case GAME -> {
                    GameMessage gameMessage = new Gson().fromJson(message, GameMessage.class);
                    System.out.println("\n");
                    if (chessClient.getMyTeamColor() == ChessGame.TeamColor.WHITE) {
                        ChessBoard.main(gameMessage.getGame().getBoard(), gameMessage.getHighlight(), true);
                    } else{
                        ChessBoard.main(gameMessage.getGame().getBoard(), gameMessage.getHighlight(), false);
                    }
                    notificationHandler.notify(new Notification(gameMessage.getServerMessageType(), ""));
                }
                default -> {
                    System.err.println("Unknown message type received: " + baseMessage.getServerMessageType());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Failed to process incoming message: " + message);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void getGame(int gameID, AuthData authData, List<ChessPosition> highlight) throws ResponseException {
        try {
            var action = new GameCommand(UserGameCommand.CommandType.LOAD_GAME, authData.authToken(), gameID, highlight);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void joinGame(JoinGameRequest joinGameRequest, AuthData auth) throws ResponseException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, auth.authToken(), joinGameRequest.gameID());
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException e) {
            throw new ResponseException(500, e.getMessage());
        }

    }

    public void resignGame(int gameID, AuthData auth) throws ResponseException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.RESIGN, auth.authToken(), gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leaveGame(int gameID, AuthData auth) throws ResponseException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, auth.authToken(), gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
            this.session.close();
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(int gameID, AuthData auth, ChessMove move, ChessGame.TeamColor teamColor) throws ResponseException {
        try {
            var action = new MoveCommand(UserGameCommand.CommandType.MAKE_MOVE, auth.authToken(), gameID, move, teamColor);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

}
