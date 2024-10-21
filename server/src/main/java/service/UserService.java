package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    private final MemoryUserDAO userAccess = new MemoryUserDAO();
    private final MemoryAuthDAO authAccess = new MemoryAuthDAO();

    public AuthData registerUser(UserData user) throws Exception {
        if (userAccess.getUser(user) != null) {
            throw new ServiceException("Error: already taken");
        } else {
            userAccess.createUser(user);
            return authAccess.createAuth(user);
        }
    }

    public AuthData loginUser(UserData user) throws Exception {
        var userAccess = new MemoryUserDAO();
        var authAcesss = new MemoryAuthDAO();
        if (userAccess.getUser(user) == null) {
            throw new ServiceException("Error: unauthorized");
        } else {
            return authAcesss.createAuth(user);
        }
    }

    public AuthData getAuth(AuthData auth) throws Exception {
        return authAccess.getAuth(auth);
    }
}
