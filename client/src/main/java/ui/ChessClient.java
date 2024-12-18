package ui;

import chess.*;
import exception.ResponseException;
import model.*;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebsocketFacade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ChessClient {
    private final ServerFacade server;
    private final NotificationHandler notificationHandler;
    private WebsocketFacade ws;
    private final String serverUrl;
    private State state = State.LOGGEDOUT;
    private AuthData authData;
    private ChessGame chessGame;
    private final List<String> rows;
    private GameData myGameData;
    private ChessGame.TeamColor myTeamColor;
    private boolean resigning = false;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.authData = null;
        this.notificationHandler = notificationHandler;
        this.rows = Arrays.asList("empty","a", "b", "c", "d", "e", "f", "g", "h");

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
                case "list" -> listGames();
                case "observe" -> observeGame(params);
                case "redraw" -> redrawBoard(params);
                case "leave" -> leaveGame(params);
                case "move" -> movePiece(params);
                case "resign" -> resignGame(params);
                case "highlight" -> highlightMoves(params);
                case "quit" -> "quit";
                case "yes" -> handleResignConfirmation("yes");
                case "no" -> handleResignConfirmation("no");
                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    private String highlightMoves(String... params) throws ResponseException {
        assertGamePlay();
        if (params.length == 2) {
            try {
                int col = rows.indexOf(params[1]);
                int row = Integer.parseInt(params[0]);
                ChessPosition piece = new ChessPosition(row, col);
                if (chessGame.getBoard().getPiece(piece) != null) {
                    Collection<ChessMove> moves = chessGame.validMoves(piece);
                    List<ChessPosition> highlight = new ArrayList<>();
                    for (ChessMove move : moves) {
                        highlight.add(move.getEndPosition());
                    }
                    ws.getGame(myGameData.gameID(), authData, highlight);
                    return String.format("Possible moves for piece at (%s, %s)", params[0], params[1]);
                } else {
                    throw new ResponseException(400, "Error: position does not have a piece");
                }
            } catch (NumberFormatException e) {
                throw new ResponseException(400, "Error: Please input <row> <col> of the piece you would " +
                        "like to highlight moves for");
            }
        } else {
                throw new ResponseException(400, "Error: Please input <row> <col> of the piece you would " +
                        "like to highlight moves for");
            }
    }

    private String resignGame(String... params) throws ResponseException {
        assertGamePlay();
        resigning = true;
        return "Are you sure you want to resign? Reply with 'yes' to confirm or 'no' to cancel.";
    }

    private String handleResignConfirmation(String... params) throws ResponseException {
        if (resigning) {
            if (params[0].equals("yes")) {
                ws.resignGame(myGameData.gameID(), authData);
                resigning = false;
                return "";
            } else if (params[0].equals("no")) {
                resigning = false;
                return "Resignation has been canceled.";
            } else {
                throw new ResponseException(400, "Error: Please reply with yes or no");
            }
        } else {
            throw new ResponseException(400,"Error: You are not trying to resign");
        }
    }

    private String movePiece(String... params) throws ResponseException {
        assertGamePlay();
        assertResigning();
        assertTeamTurn(myTeamColor);
        if (params.length == 4){
            try {
                int startCol = rows.indexOf(params[1]);
                int startRow = Integer.parseInt(params[0]);
                int endCol = rows.indexOf(params[3]);
                int endRow = Integer.parseInt(params[2]);
                ChessPosition piece = new ChessPosition(startRow, startCol);
                ChessPosition newPosition = new ChessPosition(endRow, endCol);
                ChessMove move = new ChessMove(piece, newPosition, null);
                ws.makeMove(myGameData.gameID(), authData, move, myTeamColor);
            } catch (NumberFormatException e) {
                throw new ResponseException(400, "Error: Please input <start row> <start col> <new row> <new col> " +
                        "<opt promotion piece> for the move you want to make");
            }
        } else if (params.length == 5){
            try {
                int startRow = rows.indexOf(params[0]);
                int startCol = Integer.parseInt(params[1]);
                int endRow = rows.indexOf(params[2]);
                int endCol = Integer.parseInt(params[3]);
                ChessPiece.PieceType promotion = attainPromotionPiece(params[4]);
                ChessPosition piece = new ChessPosition(startRow, startCol);
                ChessPosition newPosition = new ChessPosition(endRow, endCol);
                ChessMove move = new ChessMove(piece, newPosition, promotion);
                ws.makeMove(myGameData.gameID(), authData, move, myTeamColor);
            } catch (NumberFormatException e) {
                throw new ResponseException(400, "Error: Please input <start row> <start col> <new row> <new col> " +
                        "<opt promotion piece> for the move you want to make");
            }
        } else {
            throw new ResponseException(400, "Error: Please input <start row> <start col> <new row> <new col> " +
                    "<opt promotion piece> for the move you want to make");
        }
        return "";
    }

    private String leaveGame(String... params) throws ResponseException {
        assertGamePlay();
        assertResigning();
        int gameID = myGameData.gameID();
        ws.leaveGame(gameID, authData);
        state = State.LOGGEDIN;
        return "";
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            var user = new UserData(params[0],params[1],params[2]);
            authData =  server.registerUser(user);
            state = State.LOGGEDIN;
            return String.format("You successfully registered and logged in as %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            var user = new LoginUser(params[0],params[1]);
            authData = server.login(user);
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
        assertResigning();
        if (params.length == 2) {
            JoinGameRequest join;
            try {
                join = new JoinGameRequest(Integer.parseInt(params[0]), params[1].toUpperCase());
                if (params[1].equalsIgnoreCase("BLACK")) {
                    myTeamColor = ChessGame.TeamColor.BLACK;
                } else {
                    myTeamColor = ChessGame.TeamColor.WHITE;
                }
            } catch (NumberFormatException e) {
                return "Please select a number from the list of games.";
            }
            server.joinGame(authData.authToken(), join);
            ws = new WebsocketFacade(serverUrl,notificationHandler, this);
            ws.joinGame(join, authData);
            state = State.GAMEPLAY;
            return String.format("You successfully joined and are playing game %s\n", params[0]);
        }
        throw new ResponseException(400, "Expected: <gameID> <BLACK|WHITE>");
    }

    public String redrawBoard(String... params) throws ResponseException {
        assertGamePlay();
        assertResigning();
        ws.getGame(myGameData.gameID(), authData, null);
        return "";
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            var gameName = String.join(" ", params);
            CreateGameRequest game = new CreateGameRequest(gameName);
            server.createGame(authData.authToken(), game);
            return String.format("You successfully created the game %s", gameName);
        }
        throw new ResponseException(400, "Expected: <gameName>");
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        return server.listGames(authData.authToken()).toString();
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 1) {
            if (Integer.parseInt(params[0]) <= 0){
                throw new ResponseException(400, "Please input a gameID from the list of games.");
            }
            var list = server.listGames(authData.authToken());
            if (Integer.parseInt(params[0]) > list.getGames().size()){
                throw new ResponseException(400, "Please input a gameID from the list of games.");
            }
            JoinGameRequest join = new JoinGameRequest(Integer.parseInt(params[0]), null);
            ws = new WebsocketFacade(serverUrl,notificationHandler, this);
            ws.joinGame(join, authData);
            state = State.GAMEPLAY;
            return String.format("You are successfully observing the game %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <gameID>");
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
        if (state == State.GAMEPLAY){
            return """
                    redraw - the current chessboard
                    leave - quit playing as current Black/White player
                    move <start row> <start col> <new row> <new col> <opt promotion piece> - moves piece to new position
                    resign - forfeit game
                    highlight <row> <col> - highlights legal moves for a specific piece at location
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
        if (state == State.GAMEPLAY){
            throw new ResponseException(400, "You are currently playing a game");
        }
    }

    private void assertGamePlay() throws ResponseException {
        if (!(state == State.GAMEPLAY)) {
            throw new ResponseException(400, "You must be playing a game to redraw the board");
        }
    }

    private void assertTeamTurn(ChessGame.TeamColor teamColor) throws ResponseException {
        if (!(chessGame.getTeamTurn() == teamColor)){
            throw new ResponseException(400, "It is not your turn");
        }
    }

    private ChessPiece.PieceType attainPromotionPiece(String promotion) throws ResponseException {
        if (promotion.equalsIgnoreCase("queen")) {
            return ChessPiece.PieceType.QUEEN;
        }
        if (promotion.equalsIgnoreCase("rook")) {
            return ChessPiece.PieceType.ROOK;
        }
        if (promotion.equalsIgnoreCase("knight")) {
            return ChessPiece.PieceType.KNIGHT;
        }
        if (promotion.equalsIgnoreCase("bishop")) {
            return ChessPiece.PieceType.BISHOP;
        }
        throw new ResponseException(400, "Invalid promotion piece");
    }

    public void updateGameData(GameData gameData) {
        chessGame = gameData.game();
        myGameData = gameData;
    }

    public ChessGame.TeamColor getMyTeamColor() {
        return myTeamColor;
    }

    public void assertResigning() throws ResponseException {
        if (resigning){
            throw new ResponseException(400, "Please reply with yes or no");
        }
    }
}
