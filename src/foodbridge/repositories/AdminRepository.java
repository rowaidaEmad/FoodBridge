package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for admin permissions (tables admins + admin_permissions).
 * Ported from FoodBridgeColdStart, where this class was org.nonprofit.repository.Admin.
 */
public class AdminRepository {
    private DatabaseManager db;

    public AdminRepository() {
        db = DatabaseManager.getInstance();
    }

    /** True for an active user with role ADMIN and a row in admins. */
    public boolean isAdmin(int adminId) {
        String sql = "SELECT 1 FROM admins a JOIN users u ON a.user_id = u.id"
                + " WHERE a.user_id = ? AND u.role = 'ADMIN' AND u.is_active = TRUE";
        return exists(sql, adminId, null);
    }

    public Set<String> findPermissions(int adminId) {
        Set<String> permissions = new HashSet<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT permission FROM admin_permissions WHERE admin_id = ?")) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permission"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load permissions: " + e.getMessage(), e);
        }
        return permissions;
    }

    public boolean hasPermission(int adminId, String permission) {
        return exists("SELECT 1 FROM admin_permissions WHERE admin_id = ? AND permission = ?", adminId, permission);
    }

    /** Returns false if the admin already had it. */
    public boolean addPermission(int adminId, String permission) {
        return change("INSERT INTO admin_permissions(admin_id, permission) VALUES (?, ?)"
                + " ON CONFLICT (admin_id, permission) DO NOTHING", adminId, permission);
    }

    /** Returns false if the admin didn't have it. */
    public boolean removePermission(int adminId, String permission) {
        return change("DELETE FROM admin_permissions WHERE admin_id = ? AND permission = ?", adminId, permission);
    }

    private boolean exists(String sql, int adminId, String permission) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            if (permission != null) {
                ps.setString(2, permission);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not check admin: " + e.getMessage(), e);
        }
    }

    private boolean change(String sql, int adminId, String permission) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setString(2, permission);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not change permission: " + e.getMessage(), e);
        }
    }
}
