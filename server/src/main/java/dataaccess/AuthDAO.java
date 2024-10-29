package dataaccess;

import exception.ResponseException;
import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth(UserData user) throws ResponseException;

    AuthData getAuth(AuthData auth) throws ResponseException;

    void deleteAuth(AuthData auth) throws ResponseException;
}

