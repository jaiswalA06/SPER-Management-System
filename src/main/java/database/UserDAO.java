package database;

import entity.User;
import enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private final Connection connection;

    public UserDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public int insert(String name, Role role, String password) {
        String sql = "INSERT INTO users (name, role, password) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, role.name());
            ps.setString(3, password);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert user: " + e.getMessage());
        }
        return -1;
    }

    public int insert(User user) {
        int userId = insert(user.getName(), user.getRole(), user.getPassword());
        if (userId > 0) {
            user.setUserId(userId);
        }
        return userId;
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT id, name, role, password FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to find user: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> findByName(String name) {
        String sql = "SELECT id, name, role, password FROM users WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to find user: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<User> findByRole(Role role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, role, password FROM users WHERE role = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to find users by role: " + e.getMessage());
        }
        return users;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, role, password FROM users";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to list users: " + e.getMessage());
        }
        return users;
    }

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Failed to check users: " + e.getMessage());
        }
        return true;
    }

    public Optional<User> authenticate(int userId, String password) {
        String sql = "SELECT id, name, role, password FROM users WHERE id = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication query failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                Role.valueOf(rs.getString("role")),
                rs.getString("password")
        );
    }
}