package dataaccess;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static dataaccess.DatabaseManager.executeUpdate;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws ResponseException {
        String[] authCreateStatements = {
                """
            CREATE TABLE IF NOT EXISTS  auth (
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`authToken`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };

        DatabaseManager.configureDatabase(authCreateStatements);
    }

    @Override
    public AuthData createAuth(UserData user) throws ResponseException {
        String auth = UUID.randomUUID().toString();
        var statement = "INSERT INTO auth (authToken, username, json) VALUES (?, ?, ?)";
        var json = new Gson().toJson(user);
        DatabaseManager.executeUpdate(statement, auth, user.username(), json);
        return new AuthData(auth, user.username());
    }

    @Override
    public AuthData getAuth(AuthData auth) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, json FROM auth WHERE authToken = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth.authToken());
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(AuthData auth) throws ResponseException {
        if (getAuth(auth) != null) {
            var statement = "DELETE FROM auth WHERE authToken=?";
            DatabaseManager.executeUpdate(statement, auth.authToken());
        } else throw new ResponseException(500, "Auth does not exist");
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var json = rs.getString("json");
        var auth = new Gson().fromJson(json, AuthData.class);
        return auth.setId(authToken);
    }

    public void clear() throws ResponseException {
        var statement = "TRUNCATE auth";
        executeUpdate(statement);
    }

}
