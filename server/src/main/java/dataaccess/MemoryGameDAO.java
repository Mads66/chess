package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private int gameID = 1;
    final private HashMap<String, Collection<GameData>> UsersGames = new HashMap<>();

    @Override
    public GameData createGame(String gameName, AuthData auth) {
        var game = new GameData(gameID++, null, null, gameName, new ChessGame());
        Collection<GameData> gameList = new ArrayList<>();
        gameList.add(game);
        UsersGames.put(auth.authToken(), gameList);
        return game;
    }

    @Override
    public GameData getGame(GameData game, AuthData auth) {
        var games = UsersGames.get(auth.authToken());
        if (games.contains(game)) {
            return game;
        }
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
