package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class MemoryGameDAO implements GameDAO {
    private int gameID = 1;
    final private HashMap<String, Collection<GameData>> UsersGames = new HashMap<>();

    @Override
    public GameData createGame(String gameName, AuthData auth) {
        var game = new GameData(gameID++, null, null, gameName, new ChessGame());
        Collection<GameData> gameList = new ArrayList<>();
        gameList.add(game);
        if (UsersGames.containsKey(auth.authToken())) {
            UsersGames.get(auth.authToken()).add(game);
        } else {
            UsersGames.put(auth.authToken(), gameList);
        }
        return game;
    }

    @Override
    public GameData getGame(GameData game, AuthData auth) {
        var games = UsersGames.get(auth.authToken());
        if (games != null && games.contains(game)) {
            return game;
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames(AuthData auth) {
        return UsersGames.get(auth.authToken());
    }

    @Override
    public GameData joinGame(AuthData auth, String playerColor, int gameID) throws Exception {
        var games = UsersGames.get(auth.authToken());
        if (games != null) {
            for (GameData game : games) {
                if (game.gameId() == gameID) {
                    if (Objects.equals(playerColor, "BLACK") && game.blackUsername() == null) {
                        var newGame = new GameData(gameID, game.whiteUsername(), auth.username(), game.gameName(), game.game());
                        games.remove(game);
                        games.add(newGame);
                        return newGame;
                    } else if (Objects.equals(playerColor, "WHITE") && game.whiteUsername() == null) {
                        var newGame = new GameData(gameID, auth.username(), game.blackUsername(), game.gameName(), game.game());
                        games.remove(game);
                        games.add(newGame);
                        return newGame;
                    } else {
                        throw new DataAccessException("Error: already taken");
                    }
                }
            }
        }
        return null;
    }

    public void clear() {
        UsersGames.clear();
    }
}
