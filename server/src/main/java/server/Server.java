package server;

import com.google.gson.Gson;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.*;
import spark.*;
import service.UserService;

public class Server {

    private UserService userService = new UserService();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private String registerUser(Request req, Response res) throws Exception {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var result = userService.registerUser(user);
        var dataAccess = new MemoryAuthDAO();
        var response = dataAccess.getAuth(result);
        return new Gson().toJson(response);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
