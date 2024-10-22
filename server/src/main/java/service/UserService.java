package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import exception.ResponseException;
import model.AuthData;
import model.UserData;

public class UserService {
    private final MemoryUserDAO userAccess = new MemoryUserDAO();
    private final MemoryAuthDAO authAccess = new MemoryAuthDAO();

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
        if (userAccess.getUser(user) == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else {
            return authAccess.createAuth(user);
        }
    }

    public void logoutUser(AuthData auth) throws Exception {
        if (authAccess.getAuth(auth) == null) {
            throw new ResponseException(401, "Error: unauthorized");
        } else {
            authAccess.deleteAuth(auth);
        }
    }

    public AuthData getAuth(AuthData auth) throws Exception {
        return authAccess.getAuth(auth);
    }

    public void clear() {
        authAccess.clear();
        userAccess.clear();
    }
}
