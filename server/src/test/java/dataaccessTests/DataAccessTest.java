package dataaccessTests;

import dataaccess.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataAccessTest {
    @Test
    public void registerUser() throws Exception {
        var dataAccess = new MemoryUserDAO();
        var expected = new UserData("a", "p", "a@a.com");
        dataAccess.createUser(expected);
        var actual = dataAccess.getUser(expected);
        assertEquals(expected, actual);
    }
}
