package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;
import server.websocket.WebSocketHandler;
import service.GameService;
import spark.*;
import service.UserService;

import java.util.*;

public class Server {

    private UserService userService;
    private GameService gameService;
    private WebSocketHandler webSocketHandler;

    private void initializeServices() throws ResponseException {
        gameService = new GameService();
        userService = new UserService();
        webSocketHandler = new WebSocketHandler(gameService, userService);
    }

    public void clear() throws ResponseException {
        gameService.clear();
        userService.clear();
    }


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        try{
            initializeServices();
        }catch (Exception e){
            return -1;
        }

        Spark.webSocket("/ws", webSocketHandler);

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.delete("/db", this::clear);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.exception(ResponseException.class, this::exceptionHandler);


        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.statusCode());

        // Build the JSON response string manually
        String jsonResponse = String.format("{ \"message\": \"%s\" }", ex.getMessage());

        // Set the response body and type
        res.body(jsonResponse);
        res.type("application/json");
    }

    private Object joinGame(Request request, Response response) throws Exception {
        AuthData auth = headerHandler(request);
        var gameData = new Gson().fromJson(request.body(), JoinGameRequest.class);
        var game = gameService.joinGame(auth, gameData.playerColor(), gameData.gameID(), userService);

        response.status(200);
        response.type("application/json");
        return new Gson().toJson(game);
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
        String jsonResponse = String.format("{ \"gameID\":\"%s\" }", result.gameID());
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
            listGameResponses.add(new ListGameResponse(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName()));
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

    private Object clear(Request request, Response response) throws ResponseException {
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
