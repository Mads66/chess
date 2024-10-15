package dataaccess;

import model.AuthData;
import model.UserData;

private interface Auth {
    int createAuth();

    int getAuth(UserData user);

    int deleteAuth(AuthData auth);
}

public class AuthDAO implements Auth {

    @Override
    public int createAuth() {
        return 0;
    }

    @Override
    public int getAuth(UserData user) {
        return 0;
    }

    @Override
    public int deleteAuth(AuthData auth) {
        return 0;
    }
}
