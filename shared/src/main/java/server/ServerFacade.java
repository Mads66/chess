package server;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GamesResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

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

    private static void writeHeader(String request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.setRequestProperty("Authorization", request);
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

    public AuthData registerUser(Object request) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, null, request, AuthData.class);
    }

    public AuthData login(Object request) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path, null, request, AuthData.class);
    }

    public void logout(String header) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, header, null, null);
    }

    public GamesResponse listGames(String header) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, header, null, GamesResponse.class);
    }

    public GameData createGame(String header, Object request) throws ResponseException {
        var path = "/game";
        return this.makeRequest("POST", path, header, request, GameData.class);
    }

    public GameData joinGame(String header, Object request) throws ResponseException {
        var path = "/game";
        return this.makeRequest("PUT", path, header, request, GameData.class);
    }

    public GameData observeGame(String header, Object request) throws ResponseException {
        var path = "/game";
        return new GameData(1234,null,null,"fakeGame", new ChessGame());
//        return this.makeRequest("PUT", path, header, request, GameData.class);
    }



    private <T> T makeRequest(String method, String path, String header, Object request, Class<T> responseClass) throws ResponseException {
        HttpURLConnection http = null;
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);


            http.setDoOutput(true);
            writeHeader(header, http);
            writeBody(request, http);

            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (IOException ex) {
            System.err.println("Connection error: " + ex.getMessage());
            throw new ResponseException(500, "Connection error: " + ex.getMessage());
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            System.err.println("Error making request: " + ex.getMessage());
            throw new ResponseException(500, ex.getMessage());
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        int status = http.getResponseCode();
        if (!isSuccessful(status)) {
            // Read the error stream to get the response body with the error message
            try (InputStream errorStream = http.getErrorStream()) {
                InputStreamReader reader = new InputStreamReader(errorStream);
                Map<String, String> errorResponse = new Gson().fromJson(reader, Map.class);
                String errorMessage = errorResponse.getOrDefault("message", "Unknown error");
                throw new ResponseException(status, errorMessage);
            }
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;


    }
}
