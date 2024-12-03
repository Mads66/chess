package service.tests;

import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    @BeforeEach
    public void init() throws ResponseException {
        var userService = new UserService();
        var gameService = new GameService();
        userService.clear();
        gameService.clear();
    }

    @Test
    public void registerUser() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var expected = service.registerUser(user);
        var registered = service.getAuth(expected.authToken());
        assertEquals(expected, registered);
    }

    @Test
    public void registerUserExists() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        service.registerUser(user);
        var duplicateUsername = new UserData("username", "password", "emails@email.com");
        assertThrows(ResponseException.class, () -> service.registerUser(duplicateUsername));
    }

    @Test
    public void registerUserBadRequest() throws ResponseException {
        var service = new UserService();
        var user = new UserData("username", null, null);
        assertThrows(ResponseException.class, () -> service.registerUser(user));
    }

    @Test
    public void loginUser() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        service.registerUser(user);
        var actual = service.loginUser(user);
        var expected = service.getAuth(actual.authToken());
        assertEquals(expected, actual);
    }

    @Test
    public void loginUserBadRequest() throws ResponseException {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        assertThrows(ResponseException.class, () -> service.loginUser(user));
    }

    @Test
    public void logoutUser() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        service.registerUser(user);
        var loggedIn = service.loginUser(user);
        assertDoesNotThrow(() -> service.logoutUser(loggedIn));
    }

    @Test
    public void badLogout() throws ResponseException {
        var service = new UserService();
        var badAuth = new AuthData("blahblahblah", "username");
        assertThrows(ResponseException.class, () -> service.logoutUser(badAuth));
    }

    @Test
    public void createGame() throws Exception {
        var game = new GameService();
        var user = new UserService();
        var gameUser = new UserData("username", "password", "email@email.com");
        var registered = user.registerUser(gameUser);
        var returned = game.createGame("AwesomeGame", registered, user);
        assertEquals("AwesomeGame", returned.gameName());
    }

    @Test
    public void createGameUnauthorized() throws ResponseException {
        var game = new GameService();
        var userService = new UserService();
        var badAuth = new AuthData("Tehe-I-am-not-real", "username_who");
        assertThrows(ResponseException.class, () -> game.createGame("IamUnauthorized", badAuth, userService));
    }

    @Test
    public void createGameBadRequest() throws Exception {
        var game = new GameService();
        var userService = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var registered = userService.registerUser(user);
        assertThrows(ResponseException.class, () -> game.createGame(null, registered, userService));
    }

    @Test
    public void listGames() throws Exception {
        var service = new GameService();
        var userS = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var registered = userS.registerUser(user);
        service.createGame("AwesomeGame", registered, userS);
        service.createGame("AwesomeGame2", registered, userS);
        service.createGame("AwesomeGame3", registered, userS);
        var list = service.listGames(registered, userS);
        assertEquals(3, list.size());
    }

    @Test
    public void listGamesUnauthorized() throws ResponseException {
        var service = new GameService();
        var userService = new UserService();
        var unauthorized = new AuthData("who-is-this", "username_who");
        assertThrows(ResponseException.class, () -> service.listGames(unauthorized, userService));
    }

    @Test
    public void joinGame() throws Exception {
        var service = new GameService();
        var userService = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var registered = userService.registerUser(user);
        service.createGame("AwesomeGame", registered, userService);
        service.createGame("AwesomeGame2", registered, userService);
        service.createGame("AwesomeGame3", registered, userService);
        service.joinGame(registered, "BLACK", 1, userService);
        var list = service.listGames(registered, userService);
        assertEquals(list.size(), 3);
    }

    @Test
    public void joinGameNotFound() throws Exception {
        var service = new GameService();
        var userService = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var registered = userService.registerUser(user);
        assertThrows(ResponseException.class, () -> service.joinGame(registered, "BLACK", 1, userService));
    }

    @Test
    public void joinGameBadRequest() throws Exception {
        var service = new GameService();
        var userService = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var registered = userService.registerUser(user);
        var badAuth = new AuthData("BLAH", "HAH");
        assertThrows(ResponseException.class, () -> service.joinGame(registered, "BLACK", 0, userService));
        assertThrows(ResponseException.class, () -> service.joinGame(registered, "BLUE", 1, userService));
        assertThrows(ResponseException.class, () -> service.joinGame(badAuth, "BLACK", 2, userService));
    }

    @Test
    public void clearDatabase() throws Exception {
        var service = new GameService();
        var userService = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var user2 = new UserData("username2", "password2", "email2@email.com");
        var user3 = new UserData("username3", "password3", "email3@email.com");
        var one = userService.registerUser(user);
        var two = userService.registerUser(user2);
        var three = userService.registerUser(user3);
        service.createGame("HOOTS", one, userService);
        service.createGame("HOOTS2", two, userService);
        service.createGame("HOOTS3", three, userService);
        service.clear();
        userService.clear();
        assertThrows(ResponseException.class, () -> service.joinGame(one, "BLACK", 1, userService));
        assertThrows(ResponseException.class, () -> userService.loginUser(user2));
        assertThrows(ResponseException.class, () -> service.listGames(three, userService));
    }
}
