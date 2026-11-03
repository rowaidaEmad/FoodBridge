package org.nonprofit.repository;

import org.nonprofit.config.DatabaseManager;

import java.sql.*;
import java.util.*;

public class Admin {

    private final DatabaseManager db;

    public Admin(DatabaseManager db) {
        this.db = db;
    }

    // Check whether the user is an active admin
    public boolean isAdmin(long adminId) throws SQLException {

        String sql = """
                SELECT 1
                FROM admins a
                JOIN users u ON a.user_id = u.id
                WHERE a.user_id = ?
                  AND u.role = 'ADMIN'
                  AND u.is_active = TRUE
                """;

        try (Connection connection = db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, adminId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Get all permissions assigned to an admin
    public Set<String> findPermissions(long adminId)
            throws SQLException {

        Set<String> permissions = new HashSet<>();

        String sql = """
                SELECT permission
                FROM admin_permissions
                WHERE admin_id = ?
                """;

        try (Connection connection = db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, adminId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    permissions.add(
                            rs.getString("permission")
                    );
                }
            }
        }

        return permissions;
    }

    // Check whether an admin has a specific permission
    public boolean hasPermission(long adminId, String permission)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM admin_permissions
                WHERE admin_id = ?
                  AND permission = ?
                """;

        try (Connection connection = db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, adminId);
            statement.setString(2, permission);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Add a permission to an admin
    public boolean addPermission(long adminId, String permission)
            throws SQLException {

        String sql = """
                INSERT INTO admin_permissions (admin_id, permission)
                VALUES (?, ?)
                ON CONFLICT (admin_id, permission) DO NOTHING
                """;

        try (Connection connection = db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, adminId);
            statement.setString(2, permission);

            return statement.executeUpdate() > 0;
        }
    }

    // Remove a permission from an admin
    public boolean removePermission(long adminId, String permission)
            throws SQLException {

        String sql = """
                DELETE FROM admin_permissions
                WHERE admin_id = ?
                  AND permission = ?
                """;

        try (Connection connection = db.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, adminId);
            statement.setString(2, permission);

            return statement.executeUpdate() > 0;
        }
    }
}