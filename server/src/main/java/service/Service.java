package service;

import dataaccess.MemoryUserDAO;
import model.UserData;

public class Service {
    public UserData registerUser(UserData user) throws Exception {
        var dataAccess = new MemoryUserDAO();
        if (dataAccess.getUser(user) != null) {
            throw new ServiceException("User already exists");
        } else {
            dataAccess.createUser(user);
            return user;
        }
    }
}
