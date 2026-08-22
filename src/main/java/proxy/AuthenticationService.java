package proxy;

import entity.User;
import enums.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class AuthenticationService {

    private final Map<Integer, User> users = new HashMap<>();

    public AuthenticationService() {
        users.put(1, new User(1, "admin", Role.ADMIN, "admin123"));
        users.put(2, new User(2, "alice", Role.CUSTOMER, "alicepwd"));
        users.put(3, new User(3, "bob", Role.CUSTOMER, "bobpwd"));
        users.put(4, new User(4, "charlie", Role.SUPPORT_STAFF, "charpwd"));
        users.put(5, new User(5, "diana", Role.HUB_MANAGER, "dianapwd"));

    }

    public Optional<User> authenticate(int userId, String password) {
        User candidate = users.get(userId);
        if (candidate == null) {
            return Optional.empty();
        }
        if (candidate.getPassword().equals(password)) {
            return Optional.of(candidate);
        }
        return Optional.empty();
    }
}