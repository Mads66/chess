package dataaccess;

import model.AuthData;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName, AuthData auth);

    GameData getGame(GameData game, AuthData auth);

    Collection<GameData> listGames(AuthData auth);

    GameData joinGame(AuthData auth, String playerColor, int gameID) throws Exception;
}

