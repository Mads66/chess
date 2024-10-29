package dataaccess;

import model.AuthData;
import model.GameData;

import java.util.Collection;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    @Override
    public GameData createGame(String gameName, AuthData auth) {
        return null;
    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public Collection<GameData> listGames(AuthData auth) {
        return List.of();
    }

    @Override
    public void joinGame(AuthData auth, String playerColor, int gameID) throws Exception {

    }
}
