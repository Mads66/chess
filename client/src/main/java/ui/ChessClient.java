package ui;

import com.sun.nio.sctp.NotificationHandler;
import exception.ResponseException;
import jdk.jshell.spi.ExecutionControl;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private State state = State.LOGGEDOUT;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd){
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout(params);
                case "join" -> joinGame(params);
                case "create" -> createGame(params);
                case "list" -> listGames(params);
                case "observe" -> observeGame(params);
                default -> help();
            };
        } catch (ResponseException e){
            return e.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            state = State.LOGGEDIN;
            server.registerUser(params);
            return String.format("You successfully registered and logged in as %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            state = State.LOGGEDIN;
            server.login(params);
            return String.format("You successfully logged in as %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String logout(String... params) throws ResponseException {
        assertSignedIn();
        state = State.LOGGEDOUT;
        server.logout(params);
        return "You successfully logged out";
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 2) {
            server.joinGame(params);
            return String.format("You successfully joined game %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <gameID> <BLACK|WHITE>");
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 1) {
            server.createGame(params);
            return String.format("You successfully created game %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <gameName>");
    }

    public String listGames(String... params) throws ResponseException {
        assertSignedIn();
        return server.listGames(params).toString();
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        throw new ResponseException(500, "Not implemented");
    }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        }
        return """
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - join a game
                observe <ID> - observe a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }
}
