package service;

import dataaccess.MemoryGameDAO;
import dataaccess.SQLGameDAO;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final SQLGameDAO gameAccess = new SQLGameDAO();

    public Collection<GameData> listGames(AuthData auth, UserService userService) throws Exception {
        var authCheck = userService.getAuth(auth);
        if (authCheck == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else {
            return gameAccess.listGames(authCheck);
        }
    }

    public GameData createGame(String gameName, AuthData auth, UserService service) throws Exception {
        var authCheck = service.getAuth(auth);
        if (authCheck == null) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        if (gameName == null) {
            throw new ResponseException(400, "Error: bad request");
        } else {
            return gameAccess.createGame(gameName, authCheck);
        }
    }

    public void joinGame(AuthData auth, String playerColor, int gameId, UserService service) throws Exception {
        var authCheck = service.getAuth(auth);
        if (authCheck == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else if (gameId <= 0) {
            throw new ResponseException(400, "Error: bad request");
        } else if (playerColor != null && (playerColor.equals("BLACK") || playerColor.equals("WHITE"))) {
            gameAccess.joinGame(authCheck, playerColor, gameId);
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }

    public void clear() {
        gameAccess.clear();
    }

}
