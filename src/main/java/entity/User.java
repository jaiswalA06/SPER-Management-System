package entity;

import enums.Role;

public class User {
    private int userId;
    private String name;
    private Role role;
    private String password;

    public User(int userId, String name, Role role, String password) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.password = password;
    }

    public Role getRole() { return role; }
    public String getName() { return name; }
    public int getUserId() { return userId; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + '\'' + ", role=" + role + '}';
    }
}