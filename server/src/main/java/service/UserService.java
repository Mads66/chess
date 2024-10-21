package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;

public class UserService {

    public AuthData registerUser(UserData user) throws Exception {
        var userAccess = new MemoryUserDAO();
        var authAcesss = new MemoryAuthDAO();
        if (userAccess.getUser(user) != null) {
            throw new ServiceException("Error: already taken");
        } else {
            userAccess.createUser(user);
            return authAcesss.createAuth(user);
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
        var authAccess = new MemoryAuthDAO();
        return authAccess.getAuth(auth);
    }
}
