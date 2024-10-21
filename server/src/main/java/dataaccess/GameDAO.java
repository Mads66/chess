package dataaccess;

import model.AuthData;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(GameData game);

    GameData getGame(GameData game, AuthData auth);

    Collection<GameData> listGames(AuthData auth);

    GameData updateGame(GameData game, AuthData auth);
}

