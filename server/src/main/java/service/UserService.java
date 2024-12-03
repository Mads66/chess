package service;

import dataaccess.SQLAuthDAO;
import dataaccess.SQLUserDAO;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final SQLUserDAO userAccess = new SQLUserDAO();
    private final SQLAuthDAO authAccess = new SQLAuthDAO();

    public UserService() throws ResponseException {
    }

    public AuthData registerUser(UserData user) throws Exception {
        if (user.password() == null || user.email() == null) {
            throw new ResponseException(400, "Error: bad request");
        }
        if (userAccess.getUser(user) != null) {
            throw new ResponseException(403, "Error: already taken");
        } else {
            userAccess.createUser(user);
            return authAccess.createAuth(user);
        }
    }

    public AuthData loginUser(UserData user) throws Exception {
        var dbUser = userAccess.getUser(user);
        if (dbUser == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else if (BCrypt.checkpw(user.password(), dbUser.password())) {
            return authAccess.createAuth(user);
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }

    }

    public void logoutUser(AuthData auth) throws Exception {
        if (authAccess.getAuth(auth.authToken()) == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else {
            authAccess.deleteAuth(auth);
        }
    }

    public AuthData getAuth(String auth) throws Exception {
        return authAccess.getAuth(auth);
    }

    public void clear() throws ResponseException {
        authAccess.clear();
        userAccess.clear();
    }
}
