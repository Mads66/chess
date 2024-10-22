package dataaccess;

import exception.ResponseException;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    final private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(UserData user) throws Exception {
        var thisUser = users.get(user.username());
        if (thisUser == null) {
            return thisUser;
        } else if (!thisUser.password().equals(user.password())) {
            throw new ResponseException(401, "Error: unauthorized");
        } else {
            return thisUser;
        }
    }

    public void clear() {
        users.clear();
    }
}
