package server;

import com.google.gson.Gson;
import dataaccess.MemoryUserDAO;
import model.*;
import spark.*;
import service.Service;

public class Server {

    private Service s = new Service();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::createUser);


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private String createUser(Request req, Response res) throws Exception {
        var user = new Gson().fromJson(req.body(), UserData.class);
        user = s.registerUser(user);
        var dataAccess = new MemoryUserDAO();
        user = dataAccess.getUser(user);
        return new Gson().toJson(user);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
