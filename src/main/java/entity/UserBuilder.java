package entity;

import enums.Role;

import java.util.ArrayList;
import java.util.List;

public class UserBuilder {

    public static final int MIN_PASSWORD_LENGTH = 6;

    private String name;
    private Role role;
    private String password;

    UserBuilder() {
    }

    public UserBuilder name(String name) {
        this.name = name == null ? null : name.trim();
        return this;
    }

    public UserBuilder role(Role role) {
        this.role = role;
        return this;
    }

    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    public User build() {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            errors.add("Name is required");
        }
        if (role == null) {
            errors.add("Role is required");
        }
        if (password == null || password.isBlank()) {
            errors.add("Password is required");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join("; ", errors));
        }

        return new User(this);
    }

    String getName() {
        return name;
    }

    Role getRole() {
        return role;
    }

    String getPassword() {
        return password;
    }
}
