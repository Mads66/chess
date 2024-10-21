package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    final private HashMap<String, AuthData> auths = new HashMap<>();

    @Override
    public AuthData createAuth(UserData user) throws DataAccessException {
        String auth = UUID.randomUUID().toString();
        var newAuth = new AuthData(auth, user.username());
        auths.put(newAuth.authToken(), newAuth);
        return newAuth;

    }

    @Override
    public AuthData getAuth(AuthData auth) throws DataAccessException {
        return auths.get(auth.authToken());
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {
        auths.remove(auth.authToken());
        return;
    }
}
