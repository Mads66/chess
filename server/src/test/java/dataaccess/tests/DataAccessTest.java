package dataaccess.tests;

import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

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
    public void registerSameUser() throws Exception {
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


}
