package dataaccess.tests;

import chess.ChessGame;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {
    private final SQLUserDAO userDAO = new SQLUserDAO();
    private final SQLGameDAO gameDAO = new SQLGameDAO();
    private final SQLAuthDAO authDAO = new SQLAuthDAO();

    public DataAccessTest() throws ResponseException {
    }


    @Test
    public void registerUser() throws Exception {
        var expected = new UserData("a", "p", "a@a.com");
        userDAO.createUser(expected);
        var actual = userDAO.getUser(expected);
        assertEquals(expected, actual);
    }

    @Test
    public void registerSameUser() {
        var repeat = new UserData("a", "p", "a@a.com");
        assertThrows(ResponseException.class, () -> userDAO.createUser(repeat));
    }

    @Test
    public void getUser() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var db_user = userDAO.getUser(original);
        assertEquals(original, db_user);
    }

    @Test
    public void badGetUser() throws Exception {
        var notInDatabase = new UserData("imNotReal", "plainAsDay", "fake@person.com");
        assertNull(userDAO.getUser(notInDatabase));
    }

    @Test
    public void clearUser() throws Exception {
        userDAO.createUser(new UserData("b", "pass", "b@b.com"));
        userDAO.createUser(new UserData("c", "password", "c@c.com"));
        userDAO.createUser(new UserData("d", "password", "d@d.com"));
        userDAO.clear();
        assertNull(userDAO.getUser(new UserData("a", "p", "a@a.com")));
        assertNull(userDAO.getUser(new UserData("b", "pass", "b@b.com")));
        assertNull(userDAO.getUser(new UserData("c", "password", "c@c.com")));
    }

    @Test
    public void createAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        assertEquals(original.username(), auth.username());
    }

    @Test
    public void badCreateAuth() {
        var original = new UserData(null, "p", "a@a.com");
        assertThrows(ResponseException.class, () -> authDAO.createAuth(original));
    }

    @Test
    public void getAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var db_auth = authDAO.getAuth(auth);
        assertNotNull(db_auth);
    }

    @Test
    public void badGetAuth() throws Exception {
        var randomAuth = new AuthData("notanauthtokenindb", "username");
        assertNull(authDAO.getAuth(randomAuth));
    }

    @Test
    public void deleteAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        authDAO.deleteAuth(auth);
        assertNull(authDAO.getAuth(auth));
    }

    @Test
    public void badDeleteAuth() {
        assertThrows(ResponseException.class, () -> authDAO.deleteAuth(new AuthData("null", "username")));
    }

    @Test
    public void clearAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth1 = authDAO.createAuth(original);
        var auth2 = authDAO.createAuth(original);
        var auth3 = authDAO.createAuth(original);
        authDAO.clear();
        assertNull(authDAO.getAuth(auth1));
        assertNull(authDAO.getAuth(auth2));
        assertNull(authDAO.getAuth(auth3));
    }

    @Test
    public void createGame() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var game = gameDAO.createGame("NewGame", auth);
        assertNotNull(game);
        assertEquals(game.gameName(), "NewGame");
    }

    @Test
    public void badCreateGame() throws ResponseException {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        assertThrows(ResponseException.class, () -> gameDAO.createGame(null, auth));
    }

    @Test
    public void getGame() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var game = gameDAO.createGame("Original Game", auth);
        assertNotNull(gameDAO.getGame(game.gameID()));
    }

    @Test
    public void badGetGame() throws Exception {
        assertNull(gameDAO.getGame(1256));
    }

    @Test
    public void listGames() throws Exception {
        gameDAO.clear();
        var auth = authDAO.createAuth(new UserData("a", "p", "a@a.com"));
        var game1 = gameDAO.createGame("game1", auth);
        var game2 = gameDAO.createGame("game2", auth);
        var game3 = gameDAO.createGame("game3", auth);
        var gameList = gameDAO.listGames(auth);
        assertTrue(gameList.size() == 3);
        assertTrue(gameList.contains(game1));
        assertTrue(gameList.contains(game2));
        assertTrue(gameList.contains(game3));
    }

    @Test
    public void badListGames() throws Exception {
        gameDAO.clear();
        var auth = authDAO.createAuth(new UserData("a", "p", "a@a.com"));
        var game1 = gameDAO.createGame("game1", auth);
        var game2 = gameDAO.createGame("game2", auth);
        var game3 = gameDAO.createGame("game3", auth);
        var game4 = new GameData(1234, null, null, "unidentified", new ChessGame());
        var gameList = gameDAO.listGames(auth);
        assertEquals(3, gameList.size());
        assertTrue(gameList.contains(game1));
        assertTrue(gameList.contains(game2));
        assertTrue(gameList.contains(game3));
        assertFalse(gameList.contains(game4));
    }

    @Test
    public void clearGames() throws Exception {
        gameDAO.createGame("game1", null);
        gameDAO.createGame("game2", null);
        gameDAO.createGame("game3", null);
        gameDAO.clear();
        var gameList = gameDAO.listGames(null);
        assertEquals(0, gameList.size());
    }

}
