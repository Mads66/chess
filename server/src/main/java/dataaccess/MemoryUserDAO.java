package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    final private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public UserData createUser(UserData user) throws DataAccessException {
        users.put(user.username(), user);
        return user;
    }

    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        var thisUser = users.get(user.username());
        if (thisUser == null) {
            return thisUser;
        } else if (!thisUser.password().equals(user.password())) {
            throw new DataAccessException("Error: unauthorized");
        } else {
            return thisUser;
        }
    }
}
