package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;
import service.GameService;
import spark.*;
import service.UserService;

import java.util.*;

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
        Spark.exception(ResponseException.class, this::exceptionHandler);


        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.StatusCode());

        // Build the JSON response string manually
        String jsonResponse = String.format("{ \"message\": \"%s\" }", ex.getMessage());

        // Set the response body and type
        res.body(jsonResponse);
        res.type("application/json");
    }

    private Object joinGame(Request request, Response response) throws Exception {
        AuthData auth = headerHandler(request);
        var gameData = new Gson().fromJson(request.body(), JoinGameRequest.class);
        gameService.joinGame(auth, gameData.playerColor(), gameData.gameID(), userService);
        response.status(200);
        response.type("application/json");
        return "{}";
    }

    private AuthData headerHandler(Request request) {
        String auth = request.headers("Authorization");
        return new AuthData(auth, null);
    }

    private Object createGame(Request request, Response response) throws Exception {
        AuthData auth = headerHandler(request);
        var gameName = new Gson().fromJson(request.body(), CreateGameRequest.class);
        var result = gameService.createGame(gameName.gameName(), auth, userService);
        response.status(200);
        String jsonResponse = String.format("{ \"gameID\":\"%s\" }", result.gameId());
        response.type("application/json");
        return jsonResponse;
    }

    private Object listGames(Request request, Response response) throws Exception {
        var auth = headerHandler(request);
        var result = gameService.listGames(auth, userService);
        response.status(200);
        return listGameFormatter(result);
    }

    private Object listGameFormatter(Collection<GameData> gameList) {
        Collection<ListGameResponse> listGameResponses = new ArrayList<>();

        for (GameData gameData : gameList) {
            listGameResponses.add(new ListGameResponse(gameData.gameId(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName()));
        }

        Map<String, Collection<ListGameResponse>> responseMap = new HashMap<>();
        responseMap.put("games", listGameResponses);

        return new Gson().toJson(responseMap);
    }

    private Object logout(Request request, Response response) throws Exception {
        var auth = headerHandler(request);
        userService.logoutUser(auth);
        response.status(200);
        return "";
    }

    private Object login(Request request, Response response) throws Exception {
        var user = new Gson().fromJson(request.body(), UserData.class);
        var res = userService.loginUser(user);
        response.status(200);
        return new Gson().toJson(res);
    }

    private Object clear(Request request, Response response) {
        userService.clear();
        gameService.clear();
        response.status(200);
        return "";
    }

    private String registerUser(Request request, Response response) throws Exception {
        var user = new Gson().fromJson(request.body(), UserData.class);
        var res = userService.registerUser(user);
        response.status(200);
        return new Gson().toJson(res);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
