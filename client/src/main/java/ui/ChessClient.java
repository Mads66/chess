package ui;

import exception.ResponseException;
import model.*;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.LOGGEDOUT;
    private AuthData authData;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.authData = null;
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
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e){
            return e.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            var user = new UserData(params[0],params[1],params[2]);
            this.authData =  server.registerUser(user);
            state = State.LOGGEDIN;
            return String.format("You successfully registered and logged in as %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            var user = new LoginUser(params[0],params[1]);
            this.authData = server.login(user);
            state = State.LOGGEDIN;
            return String.format("You successfully logged in as %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String logout(String... params) throws ResponseException {
        assertSignedIn();
        state = State.LOGGEDOUT;
        server.logout(authData.authToken());
        authData = null;
        return "You successfully logged out";
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 2) {
            JoinGameRequest join = new JoinGameRequest(params[0],Integer.parseInt(params[1]));
            server.joinGame(authData.authToken(), join);
            return String.format("You successfully joined game %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <gameID> <BLACK|WHITE>");
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 1) {
            CreateGameRequest game = new CreateGameRequest(params[0]);
            server.createGame(authData.authToken(), game);
            return String.format("You successfully created game %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <gameName>");
    }

    public String listGames(String... params) throws ResponseException {
        assertSignedIn();
        return server.listGames(authData.authToken()).toString();
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
