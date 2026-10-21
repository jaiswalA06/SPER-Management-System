package entity;

import chainofresponsibility.PackageIssue;
import enums.Role;
import observer.Observer;

public class User implements Observer {
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

    User(UserBuilder builder) {
        this(0, builder.getName(), builder.getRole(), builder.getPassword());
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public void setUserId(int userId) { this.userId = userId; }

    public Role getRole() { return role; }
    public String getName() { return name; }
    public int getUserId() { return userId; }
    public String getPassword() { return password; }

    @Override
    public void update(PackageIssue issue) {
        System.out.println("  [NOTIFICATION -> " + role + "] "
                + name + " notified: Issue #" + issue.getIssueId()
                + " (" + issue.getIssueType() + ") status is now [" + issue.getStatus() + "]");
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}