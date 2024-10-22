package dataaccess;

import model.UserData;

public interface UserDAO {
    void createUser(UserData user);

    UserData getUser(UserData user) throws Exception;

}
