package dataaccess.tests;

import chess.ChessGame;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTest {
    private final SQLUserDAO userDAO = new SQLUserDAO();
    private final SQLGameDAO gameDAO = new SQLGameDAO();
    private final SQLAuthDAO authDAO = new SQLAuthDAO();

    public DataAccessTest() throws ResponseException {
    }


    @Test
    @Order(1)
    public void registerUser() throws Exception {
        userDAO.clear();
        var expected = new UserData("a", "p", "a@a.com");
        userDAO.createUser(expected);
        var actual = userDAO.getUser(expected);
        assertEquals(expected.username(), actual.username());
    }

    @Test
    @Order(2)
    public void registerSameUser() {
        var repeat = new UserData("a", "p", "a@a.com");
        assertThrows(ResponseException.class, () -> userDAO.createUser(repeat));
    }

    @Test
    @Order(3)
    public void getUser() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var dbUser = userDAO.getUser(original);
        assertEquals(original.username(), dbUser.username());
    }

    @Test
    @Order(4)
    public void badGetUser() throws Exception {
        var notInDatabase = new UserData("imNotReal", "plainAsDay", "fake@person.com");
        assertNull(userDAO.getUser(notInDatabase));
    }

    @Test
    @Order(5)
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
    @Order(6)
    public void createAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        assertEquals(original.username(), auth.username());
    }

    @Test
    @Order(7)
    public void badCreateAuth() {
        var original = new UserData(null, "p", "a@a.com");
        assertThrows(ResponseException.class, () -> authDAO.createAuth(original));
    }

    @Test
    @Order(8)
    public void getAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var dbAuth = authDAO.getAuth(auth.authToken());
        assertNotNull(dbAuth);
    }

    @Test
    @Order(9)
    public void badGetAuth() throws Exception {
        var randomAuth = new AuthData("notanauthtokenindb", "username");
        assertNull(authDAO.getAuth(randomAuth.authToken()));
    }

    @Test
    @Order(10)
    public void deleteAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        authDAO.deleteAuth(auth);
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    @Order(11)
    public void badDeleteAuth() {
        assertThrows(ResponseException.class, () -> authDAO.deleteAuth(new AuthData("null", "username")));
    }

    @Test
    @Order(12)
    public void clearAuth() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth1 = authDAO.createAuth(original);
        var auth2 = authDAO.createAuth(original);
        var auth3 = authDAO.createAuth(original);
        authDAO.clear();
        assertNull(authDAO.getAuth(auth1.authToken()));
        assertNull(authDAO.getAuth(auth2.authToken()));
        assertNull(authDAO.getAuth(auth3.authToken()));
    }

    @Test
    @Order(13)
    public void createGame() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var game = gameDAO.createGame("NewGame", auth);
        assertNotNull(game);
        assertEquals(game.gameName(), "NewGame");
    }

    @Test
    @Order(14)
    public void badCreateGame() throws ResponseException {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        assertThrows(ResponseException.class, () -> gameDAO.createGame(null, auth));
    }

    @Test
    @Order(15)
    public void getGame() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var game = gameDAO.createGame("Original Game", auth);
        assertNotNull(gameDAO.getGame(game.gameID()));
    }

    @Test
    @Order(16)
    public void badGetGame() throws Exception {
        assertNull(gameDAO.getGame(1256));
    }

    @Test
    @Order(17)
    public void listGames() throws Exception {
        gameDAO.clear();
        var auth = authDAO.createAuth(new UserData("a", "p", "a@a.com"));
        var game1 = gameDAO.createGame("game1", auth);
        var game2 = gameDAO.createGame("game2", auth);
        var game3 = gameDAO.createGame("game3", auth);
        var gameList = gameDAO.listGames(auth);
        assertEquals(3, gameList.size());
        assertTrue(gameList.contains(game1));
        assertTrue(gameList.contains(game2));
        assertTrue(gameList.contains(game3));
    }

    @Test
    @Order(18)
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
    @Order(19)
    public void joinGame() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var game1 = gameDAO.createGame("game1", auth);
        gameDAO.joinGame(auth, "WHITE", game1.gameID());
        var newGame = gameDAO.getGame(game1.gameID());
        assertEquals(auth.username(), newGame.whiteUsername());
    }

    @Test
    @Order(20)
    public void badJoinGame() throws Exception {
        var original = new UserData("a", "p", "a@a.com");
        var auth = authDAO.createAuth(original);
        var game2 = gameDAO.createGame("game2", auth);
        gameDAO.joinGame(auth, "WHITE", game2.gameID());
        var newAuth = authDAO.createAuth(original);
        assertThrows(ResponseException.class, () -> gameDAO.joinGame(newAuth, "WHITE", game2.gameID()));
    }

    @Test
    @Order(21)
    public void clearGames() throws Exception {
        gameDAO.createGame("game1", null);
        gameDAO.createGame("game2", null);
        gameDAO.createGame("game3", null);
        gameDAO.clear();
        var gameList = gameDAO.listGames(null);
        assertEquals(0, gameList.size());
    }

}
