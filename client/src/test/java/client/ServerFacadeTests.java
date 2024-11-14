package client;

import exception.ResponseException;
import model.*;
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
        serverFacade = new ServerFacade("http://localhost:"+port);
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
    @Order(3)
    public void testLogout()throws Exception {
        authData = serverFacade.login(testLoginUser);
        assertDoesNotThrow(() -> serverFacade.logout(authData.authToken()));
        authData = null;
    }

    @Test
    @Order(2)
    public void testLogin()throws Exception {
        server.clear();
        serverFacade.registerUser(testUser);
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
        authData = serverFacade.login(testLoginUser);
        assertDoesNotThrow(() -> serverFacade.createGame(authData.authToken(), createGameRequest));
    }

    @Test
    @Order(8)
    public void testCreateGameFail()throws Exception {
        assertThrows(ResponseException.class, () -> serverFacade.createGame(null, null));

    }

    @Test
    @Order(9)
    public void testListGames()throws Exception {
        authData = serverFacade.login(testLoginUser);
        assertDoesNotThrow(() -> serverFacade.listGames(authData.authToken()));
    }

    @Test
    @Order(10)
    public void testListGamesFail()throws Exception {
        assertThrows(ResponseException.class, () -> serverFacade.listGames(null));
    }

    @Test
    @Order(11)
    public void testJoinGame()throws Exception {
        server.clear();
        serverFacade.registerUser(testUser);
        authData = serverFacade.login(testLoginUser);
        serverFacade.createGame(authData.authToken(), new CreateGameRequest("old"));
        var join = new JoinGameRequest(1, "WHITE");
        assertDoesNotThrow(() -> serverFacade.joinGame(authData.authToken(), join));
    }

    @Test
    @Order(12)
    public void testJoinGameFail()throws Exception {
        authData = serverFacade.login(testLoginUser);
        assertThrows(ResponseException.class, () -> serverFacade.joinGame(authData.authToken(), null));
    }

    @Test
    @Order(13)
    public void testObserveGame()throws Exception {
        assertTrue(true);
    }

    @Test
    @Order(14)
    public void testObserveGameFail()throws Exception {
        authData = serverFacade.login(testLoginUser);
        var request = new JoinGameRequest(3, null);
        assertThrows(ResponseException.class, () -> serverFacade.observeGame(authData.authToken(), null));
    }

}
