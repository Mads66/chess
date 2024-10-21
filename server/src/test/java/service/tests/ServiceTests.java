package service.tests;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceTests {
    @Test
    public void registerUser() throws Exception {
        var service = new UserService();
        var user = new UserData("username", "password", "email@email.com");
        var expected = service.registerUser(user);
        var registered = service.getAuth(expected);
        assertEquals(expected, registered);
    }
}
