package service.tests;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.ServiceException;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    @Test
    public void registerUser() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var expected = service.registerUser(user);
        var registered = service.getAuth(expected);
        assertEquals(expected, registered);
    }

    @Test
    public void registerUserExists() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        service.registerUser(user);
        var duplicateUsername = new UserData("username", "password", "emails@email.com");
        assertThrows(ServiceException.class, () -> service.registerUser(duplicateUsername));
    }

    @Test
    public void registerUserBadRequest() throws Exception {
        var service = new UserService();
        var user = new UserData("username", null, null);
        assertThrows(ServiceException.class, () -> service.registerUser(user));
    }

    @Test
    public void loginUser() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        service.registerUser(user);
        var actual = service.loginUser(user);
        var expected = service.getAuth(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void loginUserBadRequest() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        assertThrows(ServiceException.class, () -> service.loginUser(user));
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
    public void badLogout() throws Exception {
        var service = new UserService();
        var badAuth = new AuthData("blahblahblah", "username");
        assertThrows(ServiceException.class, () -> service.logoutUser(badAuth));
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
    public void createGameUnauthorized() throws Exception {
        var game = new GameService();
        var userS = new UserService();
        var badAuth = new AuthData("Tehe-I-am-not-real", "username_who");
        assertThrows(ServiceException.class, () -> game.createGame("IamUnauthorized", badAuth, userS));
    }

    @Test
    public void createGameBadRequest() throws Exception {
        var game = new GameService();
        var userS = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var registered = userS.registerUser(user);
        assertThrows(ServiceException.class, () -> game.createGame(null, registered, userS));
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
    public void listGamesUnauthorized() throws Exception {
        var service = new GameService();
        var userS = new UserService();
        var unauthorized = new AuthData("who-is-this", "username_who");
        assertThrows(ServiceException.class, () -> service.listGames(unauthorized, userS));
    }


}
