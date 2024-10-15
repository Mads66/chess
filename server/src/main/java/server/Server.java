package server;

import com.google.gson.Gson;
import model.*;
import spark.*;
import service.Service;

public class Server {

    private Service s = new Service();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", (req, res) -> createUser(req, res));


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private String createUser(Request req, Response res) {
        var g = new Gson();
        var newUser = g.fromJson(
                """
                        { "username":"", "authToken":"" }""", UserData.class);
        var x = s.registerUser(newUser);

        return Gson.toJson(x);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
