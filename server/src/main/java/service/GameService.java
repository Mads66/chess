package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final MemoryGameDAO gameAccess = new MemoryGameDAO();

    public Collection<GameData> listGames(AuthData auth) {
        return null;
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
}
