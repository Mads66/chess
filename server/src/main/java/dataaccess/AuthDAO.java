package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth(UserData user) throws DataAccessException;

    AuthData getAuth(AuthData auth) throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException;
}

