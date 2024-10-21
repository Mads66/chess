package dataaccess;

import model.AuthData;
import model.GameData;

import java.util.Collection;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    @Override
    public GameData createGame(GameData game) {
        return null;
    }

    @Override
    public GameData getGame(GameData game, AuthData auth) {
        return null;
    }

    @Override
    public Collection<GameData> listGames(AuthData auth) {
        return List.of();
    }

    @Override
    public GameData updateGame(GameData game, AuthData auth) {
        return null;
    }
}
