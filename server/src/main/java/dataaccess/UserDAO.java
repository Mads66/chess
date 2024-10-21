package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData createUser(UserData user) throws DataAccessException;

    UserData getUser(UserData user) throws DataAccessException;

}
