package service;

import chess.ChessGame;
import dataaccess.SQLGameDAO;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final SQLGameDAO gameAccess = new SQLGameDAO();

    public GameService() throws ResponseException {
    }

    public Collection<GameData> listGames(AuthData auth, UserService userService) throws Exception {
        var authCheck = userService.getAuth(auth.authToken());
        if (authCheck == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else {
            return gameAccess.listGames(authCheck);
        }
    }

    public GameData createGame(String gameName, AuthData auth, UserService service) throws Exception {
        var authCheck = service.getAuth(auth.authToken());
        if (authCheck == null) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        if (gameName == null) {
            throw new ResponseException(400, "Error: bad request, please input a name");
        } else {
            return gameAccess.createGame(gameName, authCheck);
        }
    }

    public GameData joinGame(AuthData auth, String playerColor, int gameId, UserService service) throws Exception {
        var authCheck = service.getAuth(auth.authToken());
        if (authCheck == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else if (gameId <= 0) {
            throw new ResponseException(400, "Error: bad request, pick a gameID from the list of games");
        } else if (playerColor != null &&(playerColor.equals("BLACK") || playerColor.equals("WHITE"))) {
            gameAccess.joinGame(authCheck, playerColor, gameId);
            return gameAccess.getGame(gameId);
        }else {
            throw new ResponseException(400, "Error: bad request, pick either Black or White");
        }
    }

    public GameData getGame(int gameId) throws Exception {
        return gameAccess.getGame(gameId);
    }

    public GameData leaveGame(String playerColor, int gameId) throws Exception {
        gameAccess.leaveGame(playerColor, gameId);
        return gameAccess.getGame(gameId);
    }

    public GameData resignGame(int gameId) throws Exception {
        GameData game = gameAccess.getGame(gameId);
        game.game().resignGame();
        gameAccess.updateGame(gameId, game.game());
        return game;
    }

    public void updateGame(ChessGame game, int gameID) throws Exception {
        gameAccess.updateGame(gameID, game);
    }

    public void clear() throws ResponseException {
        gameAccess.clear();
    }

}
