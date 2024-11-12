package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.Request;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    public Object registerUser(String[] request) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, request, UserData.class);
    }

    public Object login(String[] request) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path, request, AuthData.class);
    }

    public void logout(String[] request) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, request, null);
    }

    public Object listGames(String[] request) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, request, Collection.class);
    }

    public Object createGame(String[] request) throws ResponseException {
        var path = "/game";
        return this.makeRequest("POST", path, request, GameData.class);
    }

    public Object joinGame(String[] request) throws ResponseException {
        var path = "/game";
        return this.makeRequest("PUT", path, request, GameData.class);
    }



    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;


    }
}
