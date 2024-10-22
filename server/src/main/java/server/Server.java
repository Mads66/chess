package server;

import com.google.gson.Gson;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.*;
import service.GameService;
import spark.*;
import service.UserService;

public class Server {

    private final UserService userService = new UserService();
    private final GameService gameService = new GameService();


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.delete("/db", this::clear);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);


        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object joinGame(Request request, Response response) {
    }

    private Object createGame(Request request, Response response) {
    }

    private Object listGames(Request request, Response response) {
    }

    private Object logout(Request request, Response response) {
    }

    private Object login(Request request, Response response) {
    }

    private Object clear(Request request, Response response) {
        userService.clear()
    }

    private String registerUser(Request req, Response res) throws Exception {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var response = f'[200] {userService.registerUser(user)}';
        return new Gson().toJson(response);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
