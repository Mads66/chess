package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final MemoryGameDAO gameAccess = new MemoryGameDAO();

    public Collection<GameData> listGames(AuthData auth, UserService userService) throws Exception {
        var authCheck = userService.getAuth(auth);
        if (authCheck == null) {
            throw new ServiceException("Error: unauthorized");
        } else {
            return gameAccess.listGames(auth);
        }
    }

    public GameData createGame(String gameName, AuthData auth, UserService service) throws Exception {
        var authCheck = service.getAuth(auth);
        if (authCheck == null) {
            throw new ServiceException("Error: unauthorized");
        }
        if (gameName == null) {
            throw new ServiceException("Error: bad request");
        } else {
            return gameAccess.createGame(gameName, auth);
        }
    }

    public void joinGame(AuthData auth, String playerColor, int GameId, UserService service) throws Exception {
        var authCheck = service.getAuth(auth);
        if (authCheck == null) {
            throw new ServiceException("Error: unauthorized");
        } else if (GameId <= 0) {
            throw new ServiceException("Error: bad request");
        } else if (playerColor != "BLACK" || playerColor != "WHITE") {
            throw new ServiceException("Error: bad request");
        } else {
            gameAccess.joinGame(auth, playerColor, GameId);
        }
    }
}
