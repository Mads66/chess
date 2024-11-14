package service;

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
            throw new ResponseException(400, "Error: bad request, please input a name");
        } else {
            return gameAccess.createGame(gameName, authCheck);
        }
    }

    public GameData joinGame(AuthData auth, String playerColor, int gameId, UserService service) throws Exception {
        var authCheck = service.getAuth(auth);
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

    public void clear() throws ResponseException {
        gameAccess.clear();
    }

}
