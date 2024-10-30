package dataaccess;

import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName, AuthData auth) throws ResponseException;

    GameData getGame(int gameID) throws ResponseException;

    Collection<GameData> listGames(AuthData auth) throws ResponseException;

    void joinGame(AuthData auth, String playerColor, int gameID) throws Exception;
}

