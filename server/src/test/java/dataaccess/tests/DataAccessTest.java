package dataaccess.tests;

import dataaccess.MemoryUserDAO;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
