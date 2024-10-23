package dataaccess;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private int gameID = 1234;
    final private HashMap<Integer, GameData> allGames = new HashMap<>();
    final private Collection<GameData> games = new ArrayList<>();

    @Override
    public GameData createGame(String gameName, AuthData auth) {
        var game = new GameData(gameID++, null, null, gameName, new ChessGame());
        allGames.put(game.gameID(), game);
        games.add(game);
        return game;
    }

    @Override
    public GameData getGame(int gameID) {
        return allGames.get(gameID);
    }

    @Override
    public Collection<GameData> listGames(AuthData auth) {
        return games;
    }

    @Override
    public void joinGame(AuthData auth, String playerColor, int gameID) throws Exception {
        var game = getGame(gameID);
        if (game != null) {
            if (Objects.equals(playerColor, "BLACK") && game.blackUsername() == null) {
                var newGame = new GameData(gameID, game.whiteUsername(), auth.username(), game.gameName(), game.game());
                allGames.replace(gameID, newGame);
                updateGames(newGame);
            } else if (Objects.equals(playerColor, "WHITE") && game.whiteUsername() == null) {
                var newGame = new GameData(gameID, auth.username(), game.blackUsername(), game.gameName(), game.game());
                allGames.replace(gameID, newGame);
                updateGames(newGame);
            } else {
                throw new ResponseException(403, "Error: already taken");
            }
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }

    private void updateGames(GameData newGame) {
        GameData oldGameData = null;

        for (GameData game : games) {
            if (game.gameID() == newGame.gameID()) {
                oldGameData = game;
                break;
            }
        }

        if (oldGameData != null) {
            games.remove(oldGameData);
            games.add(newGame);
        }
    }

    public void clear() {
        allGames.clear();
        games.clear();
    }
}
