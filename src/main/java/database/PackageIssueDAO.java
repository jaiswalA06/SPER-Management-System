
package database;

import chainofresponsibility.PackageIssue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PackageIssueDAO {

    private final Connection connection;

    public PackageIssueDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public int insert(String trackingNumber, String issueType,
                      String description, String severity,
                      double packageValue, int customerId) {

        String sql = "INSERT INTO package_issues " +
                "(tracking_number, issue_type, description, severity, " +
                "package_value, status, customer_id) " +
                "VALUES (?, ?, ?, ?, ?, 'REPORTED', ?)";

        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, trackingNumber);
            ps.setString(2, issueType);
            ps.setString(3, description);
            ps.setString(4, severity);
            ps.setDouble(5, packageValue);
            ps.setInt(6, customerId);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert issue: " + e.getMessage());
        }

        return -1;
    }

    public int insert(PackageIssue issue) {
        int issueId = insert(
                issue.getTrackingNumber(),
                issue.getIssueType(),
                issue.getDescription(),
                issue.getSeverity(),
                issue.getPackageValue(),
                issue.getCustomerId()
        );

        if (issueId > 0) {
            issue.setIssueId(issueId);
        }

        return issueId;
    }

    public Optional<PackageIssue> findById(int issueId) {
        String sql = "SELECT * FROM package_issues WHERE issue_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, issueId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to find issue: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<PackageIssue> findByCustomerId(int customerId) {
        List<PackageIssue> issues = new ArrayList<>();

        String sql = "SELECT * FROM package_issues WHERE customer_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    issues.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to find issues: " + e.getMessage());
        }

        return issues;
    }

    // Compatibility method used by Main.java
    public List<PackageIssue> findAssignedTo(int customerId) {
        return findByCustomerId(customerId);
    }

    public List<PackageIssue> findByStatus(String status) {
        List<PackageIssue> issues = new ArrayList<>();

        String sql = "SELECT * FROM package_issues WHERE status = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    issues.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to find issues by status: " + e.getMessage());
        }

        return issues;
    }

    public List<PackageIssue> findAll() {
        List<PackageIssue> issues = new ArrayList<>();

        String sql = "SELECT * FROM package_issues";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                issues.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to list issues: " + e.getMessage());
        }

        return issues;
    }

    public boolean updateStatus(int issueId, String newStatus) {
        String sql = "UPDATE package_issues SET status = ? WHERE issue_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, issueId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update status: " + e.getMessage());
        }

        return false;
    }

    public boolean resolveIssue(int issueId, String resolvedStatus) {
        String sql = "UPDATE package_issues " +
                "SET status = ?, resolved_at = datetime('now') " +
                "WHERE issue_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, resolvedStatus);
            ps.setInt(2, issueId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to resolve issue: " + e.getMessage());
        }

        return false;
    }

    // Used by Memento undo
    public boolean restoreStatus(int issueId, String restoredStatus) {
        String sql = "UPDATE package_issues SET status = ?, " +
                "resolved_at = CASE " +
                "WHEN ? = 'RESOLVED' THEN resolved_at " +
                "ELSE NULL END WHERE issue_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, restoredStatus);
            ps.setString(2, restoredStatus);
            ps.setInt(3, issueId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to restore status: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(int issueId) {
        String sql = "DELETE FROM package_issues WHERE issue_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, issueId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete issue: " + e.getMessage());
        }

        return false;
    }

    public void recordHistory(int issueId, String oldStatus,
                              String newStatus, int changedBy) {

        String sql = "INSERT INTO issue_history " +
                "(issue_id, old_status, new_status, changed_by) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, changedBy);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to record history: " + e.getMessage());
        }
    }

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM package_issues";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Failed to check issues: " + e.getMessage());
        }

        return true;
    }

    public boolean updateStatusAndHandler(int issueId, String status,
                                          String handledBy) {

        String sql = "UPDATE package_issues SET status = ?, " +
                "handled_by = ?, " +
                "resolved_at = CASE WHEN ? = 'RESOLVED' " +
                "THEN datetime('now') ELSE NULL END " +
                "WHERE issue_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, handledBy);
            ps.setString(3, status);
            ps.setInt(4, issueId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(
                    "Failed to update status and handler: " + e.getMessage()
            );
        }

        return false;
    }

    private PackageIssue mapRow(ResultSet rs) throws SQLException {
        PackageIssue issue = new PackageIssue(
                rs.getInt("issue_id"),
                rs.getString("tracking_number"),
                rs.getString("issue_type"),
                rs.getString("description"),
                rs.getString("severity"),
                rs.getDouble("package_value")
        );

        issue.setStatus(rs.getString("status"));
        issue.setCustomerId(rs.getInt("customer_id"));

        // Read the responsible handler if the database has this column.
        try {
            issue.setHandledBy(rs.getString("handled_by"));
        } catch (SQLException ignored) {
            // Supports an older schema without handled_by.
        }

        return issue;
    }
}
