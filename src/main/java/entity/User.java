package entity;

import enums.Role;

public class User {
    private int userId;
    private String name;
    private Role role;

    public User(int userId, String name, Role role) {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    public Role getRole() { return role; }
    public String getName() { return name; }
    public int getUserId() { return userId; }
}