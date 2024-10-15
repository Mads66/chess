package dataaccess;

import model.AuthData;
import model.GameData;

private interface Game {
    int createGame(GameData game);

    int getGame(GameData game, AuthData auth);

    int listGames(AuthData auth);

    int updateGame(GameData game, AuthData auth);
}

public class GameDAO implements Game {
    @Override
    public int createGame(GameData game) {
        return 0;
    }

    @Override
    public int getGame(GameData game, AuthData auth) {
        return 0;
    }

    @Override
    public int listGames(AuthData auth) {
        return 0;
    }

    @Override
    public int updateGame(GameData game, AuthData auth) {
        return 0;
    }
}
