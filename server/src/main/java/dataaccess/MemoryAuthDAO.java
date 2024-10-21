package dataaccess;

import model.AuthData;
import model.UserData;

public class MemoryAuthDAO implements AuthDAO {
    @Override
    public AuthData createAuth() throws DataAccessException {
        return null;
    }

    @Override
    public AuthData getAuth(UserData user) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData deleteAuth(AuthData auth) throws DataAccessException {
        return null;
    }
}
