package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLGameDAO implements GameDAO {

    public SQLGameDAO() throws ResponseException {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS  games (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256) NOT NULL,
              `json` TEXT NOT NULL,
              PRIMARY KEY (`gameID`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public GameData createGame(String gameName, AuthData auth) throws ResponseException {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, json) VALUES (?, ?, ?, ?)";
        var chessGame = new ChessGame();
        var json = new Gson().toJson(chessGame);
        var gameID = executeUpdate(statement, null, null, gameName, json);
        return new GameData(gameID, null, null, gameName, chessGame);
    }

    @Override
    public GameData getGame(int gameID) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var json = rs.getString("json");
        var chessGame = new Gson().fromJson(json, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    @Override
    public Collection<GameData> listGames(AuthData auth) throws ResponseException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    @Override
    public void joinGame(AuthData auth, String playerColor, int gameID) throws Exception {
        var game = getGame(gameID);
        if (game != null) {
            if (Objects.equals(playerColor, "BLACK") && game.blackUsername() == null) {
                var statement = "UPDATE games SET blackUsername = ? WHERE gameID = ?";
                DatabaseManager.executeUpdate(statement, auth.username(), gameID);
            } else if (Objects.equals(playerColor, "WHITE") && game.whiteUsername() == null) {
                var statement = "UPDATE games SET whiteUsername = ? WHERE gameID = ?";
                DatabaseManager.executeUpdate(statement, auth.username(), gameID);
            } else {
                throw new ResponseException(403, "Error: position already taken");
            }
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }

    public void leaveGame(String playerColor, int gameID) throws ResponseException {
        var game = getGame(gameID);
        if (game != null) {
            if (Objects.equals(playerColor, "BLACK") && game.blackUsername() != null) {
                var statement = "UPDATE games SET blackUsername = null WHERE gameID = ?";
                DatabaseManager.executeUpdate(statement, gameID);
            }
            if (Objects.equals(playerColor, "WHITE") && game.whiteUsername() != null) {
                var statement = "UPDATE games SET whiteUsername = null WHERE gameID = ?";
                DatabaseManager.executeUpdate(statement, gameID);
            } else {
                throw new ResponseException(403, "Error: User is not a player in this game");
            }
        }
    }

    public void updateGame(int gameID, ChessGame chessGame) throws ResponseException {
        var game = getGame(gameID);
        if (game != null) {
            var json = new Gson().toJson(chessGame);
            var statement = "UPDATE games SET json = ? where gameID = ?";
            DatabaseManager.executeUpdate(statement, json, gameID);

        } else {
            throw new ResponseException(400, "Error: game does not exist");
        }
    }

    public void clear() throws ResponseException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }

    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    } else if (param instanceof Integer p) {
                        ps.setInt(i + 1, p);
                    } else if (param instanceof ChessGame p) {
                        ps.setString(i + 1, p.toString());
                    } else if (param == null) {
                        ps.setNull(i + 1, NULL);
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        } catch (DataAccessException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

}
