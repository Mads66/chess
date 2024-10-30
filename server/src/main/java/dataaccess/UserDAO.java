package dataaccess;

import exception.ResponseException;
import model.UserData;

public interface UserDAO {
    void createUser(UserData user) throws ResponseException;

    UserData getUser(UserData user) throws Exception;

}
