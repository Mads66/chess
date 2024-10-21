package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth() throws DataAccessException;

    AuthData getAuth(UserData user) throws DataAccessException;

    AuthData deleteAuth(AuthData auth) throws DataAccessException;
}

