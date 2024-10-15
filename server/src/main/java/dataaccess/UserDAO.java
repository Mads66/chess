package dataaccess;

import model.UserData;

private interface User {
    int createUser(UserData user);

    int getUser(UserData user);

}

public class UserDAO implements User {

    @Override
    public int createUser(UserData user) {
        return 0;
    }

    @Override
    public int getUser(UserData user) {
        return 0;
    }
}
