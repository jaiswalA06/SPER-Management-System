package proxy;

import database.UserDAO;
import entity.User;

import java.util.Optional;

public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public Optional<User> authenticate(int userId, String password) {
        return userDAO.authenticate(userId, password);
    }

    public Optional<User> findUserById(int userId) {
        return userDAO.findById(userId);
    }

    public Optional<User> findUserByName(String name) {
        return userDAO.findByName(name);
    }
}