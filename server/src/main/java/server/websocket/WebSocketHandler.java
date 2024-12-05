package server.websocket;

import chess.*;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.GameCommand;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
import java.util.List;

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
            case LEAVE -> leave(action.getGameID(), action.getAuthToken(), session);
            case RESIGN -> resign(action.getGameID(), action.getAuthToken(), session);
            case MAKE_MOVE -> {
                MoveCommand moveCommand = new Gson().fromJson(message, MoveCommand.class);
                makeMove(moveCommand.getGameID(), moveCommand.getAuthToken(), session, moveCommand.getMove());
            }
            case LOAD_GAME -> {
                GameCommand gameCommand = new Gson().fromJson(message, GameCommand.class);
                loadGame(gameCommand.getGameID(), gameCommand.getAuthToken(), session, gameCommand.getHighlight());
            }
        }
    }

    private void connect(int gameID, Session session, String auth) throws Exception {
        connections.add(gameID, session, auth);
        if (assertAuth(gameID, auth, session, false) && assertGameID(gameID, auth, session)) {
            try {
                GameData gameBoard = gameService.getGame(gameID);
                var gameNote = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameBoard);
                String username = userService.getAuth(auth).username();
                var notification = getMessage(gameBoard, username);
                connections.localBroadcast(gameID, gameNote, session);
                connections.broadcast(gameID, notification, auth);
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: could not connect, please try again");
                connections.localBroadcast(gameID, notification, session);
            }
        }
    }

    private static NotificationMessage getMessage(GameData gameBoard, String username) {
        String message;
        if (gameBoard.whiteUsername() != null && gameBoard.whiteUsername().equals(username)) {
            message = String.format("%s joined game as white player", username);
        } else if (gameBoard.blackUsername() != null && gameBoard.blackUsername().equals(username)) {
            message = String.format("%s joined game as black player", username);
        } else {
            message = String.format("%s joined game as an observer", username);
        }
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        return notification;
    }

    private void leave(int gameID, String auth, Session session) throws IOException {
        if (assertAuth(gameID, auth, session,true) && assertGameID(gameID, auth, session)) {
            try {
                AuthData authData = userService.getAuth(auth);
                GameData gameBoard = gameService.getGame(gameID);
                String playerColor = playerColor(gameBoard, authData);
                if (!(playerColor.equals("observer"))) {
                    gameService.leaveGame(playerColor, gameID);
                }
                var message = String.format("%s has left the game", authData.username());
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.broadcast(gameID, notification, auth);
                connections.remove(gameID, auth);
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: could not leave, please try again");
                connections.localBroadcast(gameID, notification, session);
            }
        }
    }

    public String playerColor(GameData gameBoard, AuthData authData) {
        if (gameBoard.whiteUsername() != null && gameBoard.whiteUsername().equals(authData.username())) {
            return "WHITE";
        }
        if (gameBoard.blackUsername() != null && gameBoard.blackUsername().equals(authData.username())) {
            return "BLACK";
        } else {
            return "observer";
        }
    }

    private void resign(int gameID, String auth, Session session) throws Exception {
        if (assertAuth(gameID, auth, session,true) && assertGameID(gameID, auth, session) &&
                assertGameplay(gameID, session)) {
            try {
                AuthData authData = userService.getAuth(auth);
                GameData gameBoard = gameService.getGame(gameID);
                String playerColor = playerColor(gameBoard, authData);
                if (!(playerColor.equals("observer"))) {
                    gameService.resignGame(gameID);
                    var message = String.format("%s has resigned game and game %s is over",
                            authData.username(), gameID);
                    var notification = new NotificationMessage(
                            ServerMessage.ServerMessageType.NOTIFICATION, message);
                    connections.generalBroadcast(gameID, notification);
                } else {
                    var error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                            String.format("Error: %s is not a player in this game", authData.username()));
                    connections.localBroadcast(gameID, error, session);
                }
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: could not resign," +
                        "please try again");
                connections.localBroadcast(gameID, notification, session);
            }
        }
    }

    private void makeMove(int gameID, String auth, Session session, ChessMove move) throws Exception {
        if (assertAuth(gameID, auth, session, true) && assertGameID(gameID, auth, session) &&
                assertChessMove(gameID,auth,session,move) && assertGameplay(gameID, session)) {
            try {
                GameData gameBoard = gameService.getGame(gameID);
                try {
                    gameBoard.game().makeMove(move);
                    gameService.updateGame(gameBoard.game(), gameID);
                } catch (InvalidMoveException e) {
                    var error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                    connections.localBroadcast(gameID,error,session);
                }
                var game = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameBoard);
                String message = String.format("player made move %s", move.getEndPosition());
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.broadcast(gameID, notification, auth);
                connections.generalBroadcast(gameID, game);
                ChessGame.TeamColor color = gameBoard.game().getTeamTurn();
                if (gameBoard.game().isInCheck(color)) {
                    if (gameBoard.game().isInCheckmate(color)) {
                        gameBoard.game().resignGame();
                        gameService.updateGame(gameBoard.game(), gameID);
                        var check = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                                String.format("%s is in checkmate, Game Over!", color.toString()));
                        connections.generalBroadcast(gameID, check);
                    } else {
                        var check = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                                String.format("%s is in check", color.toString()));
                        connections.generalBroadcast(gameID, check);
                    }
                }
                if (gameBoard.game().isInStalemate(color)) {
                    gameBoard.game().resignGame();
                    gameService.updateGame(gameBoard.game(), gameID);
                    var check = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate!");
                    connections.generalBroadcast(gameID, check);
                }
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                        "Error: bad move, please try again");
                connections.localBroadcast(gameID, notification, session);
            }
        }
    }

    private void loadGame(int gameID, String auth, Session session, List<ChessPosition> highlight) throws Exception {
        if (assertAuth(gameID, auth, session,true) && assertGameID(gameID, auth, session)){
            ChessGame game = gameService.getGame(gameID).game();
            GameMessage message = new GameMessage(ServerMessage.ServerMessageType.GAME, game, highlight);
            connections.localBroadcast(gameID,message,session);
        }
    }

    private boolean assertAuth(int gameID, String auth, Session session, boolean gamePlay) throws IOException {
        try {
            AuthData authData = userService.getAuth(auth);
            if (authData == null){
                String message = "Error: user is not authorized";
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, session);
                connections.remove(gameID, auth);
                return false;
            } else if (gamePlay){
                List<Connection> users = connections.getConnections(gameID);
                for (var user : users){
                    if (user.getPlayerAuth().equals(auth)){
                        return true;
                    }
                }
                var message = "Error: player is not authorized";
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, session);
                return false;
            } else {
                return true;
            }
        } catch (Exception ex){
            var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, ex.getMessage());
            connections.localBroadcast(gameID,notification,session);
        }
        return false;
    }

    private boolean assertGameID (int gameID, String auth, Session session) throws IOException {
        try {
            if (gameService.getGame(gameID) == null) {
                var message = String.format("Error: game %s not found", gameID);
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, session);
                return false;
            }else {
                return true;
            }
        } catch (Exception ex) {
            var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, ex.getMessage());
            connections.localBroadcast(gameID,notification,session);
        }
        return false;
    }

    private boolean assertChessMove(int gameID, String auth, Session session, ChessMove move) throws IOException {
        try {
            GameData game = gameService.getGame(gameID);
            AuthData authData = userService.getAuth(auth);
            if (game == null) {
                var message = String.format("Error : game %s not found", gameID);
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, session);
                return false;
            }
            if (game.game().getGameOver()){
                var message = String.format("Error: game %s is over", gameID);
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, session);
                return false;
            }

            ChessGame chessGame = game.game();
            ChessPiece piece = chessGame.getBoard().getPiece(move.getStartPosition());
            ChessGame.TeamColor teamColor;
            try {
                teamColor = piece.getTeamColor();
            } catch (Exception ex){
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR,
                        "Error: There is not a piece there");
                connections.localBroadcast(gameID, notification, session);
                return false;
            }
            if (teamColor == ChessGame.TeamColor.BLACK && !game.blackUsername().equals(authData.username())){
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR,
                        "Error: That is not your piece to move");
                connections.localBroadcast(gameID, notification, session);
                return false;
            } else if (teamColor == ChessGame.TeamColor.WHITE && !game.whiteUsername().equals(authData.username())){
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR,
                        "Error: That is not your piece to move");
                connections.localBroadcast(gameID, notification, session);
                return false;
            }
            if(chessGame.isInCheckmate(piece.getTeamColor())){
                GameData gameData = gameService.getGame(gameID);
                var notification = new LoadGameMessage(ErrorMessage.ServerMessageType.LOAD_GAME, gameData);
                connections.generalBroadcast(gameID, notification);
                return false;
            }
            if(chessGame.getTeamTurn() != piece.getTeamColor()){
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR,
                        "Error: It is not your turn");
                connections.localBroadcast(gameID, notification, session);
                return false;
            }
            var validMoves = chessGame.validMoves(move.getStartPosition());
            if (validMoves.contains(move)){
                return true;
            }
        } catch (Exception ex) {
            var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, "Error: invalid move");
            connections.localBroadcast(gameID, notification, session);
        }
        var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR,
                "Error: Invalid move");
        connections.localBroadcast(gameID, notification, session);
        return false;
    }

    private boolean assertGameplay(int gameID, Session session) throws Exception {
        ChessGame game = gameService.getGame(gameID).game();
        if (game.getGameOver()){
            var message = String.format("Error: game %s is no longer playable", gameID);
            var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
            connections.localBroadcast(gameID, notification, session);
            return false;
        }
        return true;
    }
}
