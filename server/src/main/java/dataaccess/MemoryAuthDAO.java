package dataaccess;

import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    final private HashMap<String, AuthData> auths = new HashMap<>();

    @Override
    public AuthData createAuth(UserData user) throws ResponseException {
        String auth = UUID.randomUUID().toString();
        var newAuth = new AuthData(auth, user.username());
        auths.put(newAuth.authToken(), newAuth);
        return newAuth;

    }

    @Override
    public AuthData getAuth(String auth) throws ResponseException {
        return auths.get(auth);
    }

    @Override
    public void deleteAuth(AuthData auth) throws ResponseException {
        auths.remove(auth.authToken());
    }

    public void clear() {
        auths.clear();
    }
}
