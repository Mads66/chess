package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

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
            case MAKE_MOVE -> {
                MoveCommand moveCommand = new Gson().fromJson(message, MoveCommand.class);
                makeMove(moveCommand.getGameID(), moveCommand.getAuthToken(), moveCommand.getMove());
            }
        }
    }

    private void connect(int gameID, Session session, String auth) throws Exception {
        connections.add(gameID, session, auth);
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth)) {
            try {
                GameData gameBoard = gameService.getGame(gameID);
                var gameNote = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameBoard);
                var message = String.format("joined game %s", gameID);
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.localBroadcast(gameID, gameNote, auth);
                connections.broadcast(gameID, notification, auth);
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
                connections.localBroadcast(gameID, notification, auth);
            }
        }
    }

    private void leave(int gameID, String auth) throws IOException {
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth)) {
            try {
                AuthData authData = userService.getAuth(auth);
                if (gameService.getGame(gameID).whiteUsername().equals(authData.username())) {
                    String playerColor = "WHITE";
                    gameService.leaveGame(playerColor, gameID);
                    var message = String.format("%s has resigned game", authData.username());
                    var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                    connections.generalBroadcast(gameID, notification);
                } else if (gameService.getGame(gameID).blackUsername().equals(authData.username())) {
                    String playerColor = "BLACK";
                    gameService.leaveGame(playerColor, gameID);
                    var message = String.format("%s has resigned game", authData.username());
                    var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                    connections.generalBroadcast(gameID, notification);
                } else {
                    var error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("%s is not a player in this game", authData.username()));
                    connections.localBroadcast(gameID, error, auth);
                }
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
                connections.localBroadcast(gameID, notification, auth);
            }
        }
    }

    private void resign(int gameID, String auth) throws IOException {
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth)) {
            try {
                AuthData authData = userService.getAuth(auth);
                GameData gameBoard = gameService.getGame(gameID);
                if (gameBoard.whiteUsername().equals(authData.username())){
                    gameBoard.game().resignGame();
                    var message = String.format("%s has resigned game and game %s is over", authData.username(), gameID);
                    var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                    connections.generalBroadcast(gameID, notification);
                } else if (gameBoard.blackUsername().equals(authData.username())){
                    gameBoard.game().resignGame();
                    var message = String.format("%s has resigned game and game %s is over", authData.username(), gameID);
                    var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                    connections.generalBroadcast(gameID, notification);
                } else {
                    var error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, String.format("%s is not a player in this game", authData.username()));
                    connections.localBroadcast(gameID, error, auth);
                }
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
                connections.localBroadcast(gameID, notification, auth);
            }
        }
    }

    private void makeMove(int gameID, String auth, ChessMove move) throws IOException {
        if (assertAuth(gameID, auth) && assertGameID(gameID, auth) && assertChessMove(gameID,auth,move)) {
            try {
                GameData gameBoard = gameService.getGame(gameID);
                try {
                    gameBoard.game().makeMove(move);
                } catch (InvalidMoveException e) {
                    var error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                    connections.localBroadcast(gameID,error,auth);
                }
                var game = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameBoard);
                String message = String.format("player made move %s", move.getEndPosition());
                var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.broadcast(gameID, notification, auth);
                connections.generalBroadcast(gameID, game);
                ChessGame.TeamColor color = gameBoard.game().getTeamTurn();
                if (gameBoard.game().isInCheck(color)) {
                    var check = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in check", color.toString()));
                    connections.generalBroadcast(gameID, check);
                }
                if (gameBoard.game().isInCheckmate(color)) {
                    var check = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in checkmate, Game Over!", color.toString()));
                    connections.generalBroadcast(gameID, check);
                }
                if (gameBoard.game().isInStalemate(color)) {
                    var check = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate!");
                    connections.generalBroadcast(gameID, check);
                }
            } catch (Exception ex) {
                var notification = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
                connections.localBroadcast(gameID, notification, auth);
            }
        }
    }

    private boolean assertAuth(int gameID, String auth) throws IOException {
        try {
            if (userService.getAuth(auth) == null){
                var message = "user is not authorized";
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, auth);
                return false;
            } else {
                return true;
            }
        } catch (Exception ex){
            var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, ex.getMessage());
            connections.localBroadcast(gameID,notification,auth);
        }
        return false;
    }

    private boolean assertGameID (int gameID, String auth) throws IOException {
        try {
            if (gameService.getGame(gameID) == null) {
                var message = String.format("game %s not found", gameID);
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, auth);
                return false;
            }else {
                return true;
            }
        } catch (Exception ex) {
            var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, ex.getMessage());
            connections.localBroadcast(gameID,notification,auth);
        }
        return false;
    }

    private boolean assertChessMove(int gameID, String auth, ChessMove move) throws IOException {
        try {
            GameData game = gameService.getGame(gameID);
            AuthData authData = userService.getAuth(auth);
            if (game == null) {
                var message = String.format("game %s not found", gameID);
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, auth);
                return false;
            }
            if (game.game().getGameOver()){
                var message = String.format("game %s is over", gameID);
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, message);
                connections.localBroadcast(gameID, notification, auth);
                return false;
            }

            ChessGame chessGame = game.game();
            ChessPiece piece = chessGame.getBoard().getPiece(move.getStartPosition());
            ChessGame.TeamColor teamColor = piece.getTeamColor();
            if (teamColor == ChessGame.TeamColor.BLACK && !game.blackUsername().equals(authData.username())){
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, "That is not your piece to move");
                connections.localBroadcast(gameID, notification, auth);
                return false;
            } else if (teamColor == ChessGame.TeamColor.WHITE && !game.whiteUsername().equals(authData.username())){
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, "That is not your piece to move");
                connections.localBroadcast(gameID, notification, auth);
                return false;
            }
            if(chessGame.isInCheckmate(piece.getTeamColor())){
                GameData gameData = gameService.getGame(gameID);
                var notification = new LoadGameMessage(ErrorMessage.ServerMessageType.LOAD_GAME, gameData);
                connections.generalBroadcast(gameID, notification);
                return false;
            }
            if(chessGame.getTeamTurn() != piece.getTeamColor()){
                var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, "It is not your turn");
                connections.localBroadcast(gameID, notification, auth);
                return false;
            }
            var validMoves = chessGame.validMoves(move.getStartPosition());
            if (validMoves.contains(move)){
                return true;
            }
        } catch (Exception ex) {
            var notification = new ErrorMessage(ErrorMessage.ServerMessageType.ERROR, ex.getMessage());
            connections.localBroadcast(gameID, notification, auth);
        }
        return false;
    }
}
