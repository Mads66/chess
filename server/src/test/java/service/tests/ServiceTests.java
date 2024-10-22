package service.tests;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
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
}
