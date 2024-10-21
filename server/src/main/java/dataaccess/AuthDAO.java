package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData createAuth() throws DataAccessException;

    AuthData getAuth(AuthData auth) throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException;
}

