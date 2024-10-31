package dataaccess;

import com.google.gson.Gson;
import exception.ResponseException;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;

import static dataaccess.DatabaseManager.executeUpdate;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws ResponseException {
        String[] userCreateStatements = {
                """
            CREATE TABLE IF NOT EXISTS  user (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        DatabaseManager.configureDatabase(userCreateStatements);
    }

    @Override
    public void createUser(UserData user) throws ResponseException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        var password = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeUpdate(statement, user.username(), password, user.email());
    }

    @Override
    public UserData getUser(UserData user) throws Exception {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM user WHERE username = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, user.username());
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }

    public void clear() throws ResponseException {
        var statement = "TRUNCATE user";
        executeUpdate(statement);
    }


}
