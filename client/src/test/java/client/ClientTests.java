package client;

import exception.ResponseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import server.Server;
import ui.ChessClient;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClientTests {
    public static ChessClient client;
    public static Server server;


    @BeforeAll
    public static void startServer() throws ResponseException{
        server = new Server();
        server.run(8080);
        client = new ChessClient("http://localhost:8080");
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @Order(1)
    public void testRegister()throws Exception {
        server.clear();
        var response = client.register("testUser", "testPassword", "email@email.com");
        var expected = "You successfully registered and logged in as testUser";
        assertEquals(expected,response);
    }

    @Test
    @Order(2)
    public void testLogout()throws Exception {
        var response = client.logout();
        var expected = "You successfully logged out";
        assertEquals(expected,response);
    }

    @Test
    @Order(4)
    public void testLogin()throws Exception {
        server.clear();
        client.register("second", "user", "email@email.com");
        client.logout();
        var response = client.login("second", "user");
        var expected = "You successfully logged in as second";
        assertEquals(expected,response);
        client.logout();
    }

    @Test
    @Order(5)
    public void testLoginFail()throws Exception {
        assertThrows(ResponseException.class, () -> client.login("unusedUsername", "badPassword"));
    }

    @Test
    @Order(6)
    public void testLogoutFail()throws Exception {
        assertThrows(ResponseException.class, () -> client.logout());
    }

    @Test
    @Order(3)
    public void testRegisterFail()throws Exception {
        assertThrows(ResponseException.class, () -> client.register("testUser", "testPassword", "email@email.com"));
    }
}
