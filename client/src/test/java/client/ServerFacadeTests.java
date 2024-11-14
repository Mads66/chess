package client;

import exception.ResponseException;
import model.AuthData;
import model.CreateGameRequest;
import model.LoginUser;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private UserData testUser = new UserData("testUser", "testPassword", "email@email.com");
    private LoginUser testLoginUser = new LoginUser("testUser", "testPassword");
    private AuthData authData;
    private CreateGameRequest createGameRequest = new CreateGameRequest("newGame");

    @BeforeAll
    public static void init() throws ResponseException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:0");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @Order(1)
    public void testRegister()throws Exception {
        server.clear();
        var response = serverFacade.registerUser(testUser);
        authData = response;
        assertEquals(testUser.username(), response.username());
    }

    @Test
    @Order(2)
    public void testLogout()throws Exception {
        assertDoesNotThrow(() -> serverFacade.logout(authData.authToken()));
        authData = null;
    }

    @Test
    @Order(3)
    public void testLogin()throws Exception {
        var response = serverFacade.login(testLoginUser);
        authData = response;
        assertEquals(authData.username(),response.username());
    }

    @Test
    @Order(4)
    public void testLoginFail()throws Exception {
        assertThrows(ResponseException.class, () -> serverFacade.login(new LoginUser("unusedUsername", "badPassword")));
    }

    @Test
    @Order(5)
    public void testLogoutFail()throws Exception {
        assertThrows(ResponseException.class, () -> serverFacade.logout(null));
    }

    @Test
    @Order(6)
    public void testRegisterFail()throws Exception {
        assertThrows(ResponseException.class, () -> serverFacade.registerUser(testUser));
    }

    @Test
    @Order(7)
    public void testCreateGame()throws Exception {
        assertDoesNotThrow(() -> serverFacade.createGame(authData.authToken(), createGameRequest));
    }

    @Test
    @Order(8)
    public void testCreateGameFail()throws Exception {
        assertThrows(ResponseException.class, () -> serverFacade.createGame(null, null));

    }

}
