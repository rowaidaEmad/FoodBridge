package foodbridge.models.lookups;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Lookup table (CRUD). Table: donation_statuses.
 */
public class DonationStatus {
    public static final String POSTED = "Posted";
    public static final String PICKED_UP = "Picked up";
    public static final String DELIVERED = "Delivered";
    public static final String EXPIRED = "Expired";
    public static final String CANCELLED = "Cancelled";

    private int id;
    private String name;

    public DonationStatus() {
    }

    public DonationStatus(String name) {
        setName(name);
    }

    public DonationStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name.trim();
    }

    /** Inserts a new status or renames an existing one. */
    public void save() {
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (id == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO donation_statuses(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE donation_statuses SET name = ? WHERE id = ?")) {
                    ps.setString(1, name);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Donation status '" + name + "' already exists");
            }
            throw new RuntimeException("Could not save donation status: " + e.getMessage(), e);
        }
    }

    /** Returns false if the status does not exist or is still used by a donation. */
    public boolean delete() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM donation_statuses WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                return false;
            }
            throw new RuntimeException("Could not delete donation status: " + e.getMessage(), e);
        }
    }

    public static List<DonationStatus> findAll() {
        List<DonationStatus> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, name FROM donation_statuses ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DonationStatus(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list donation statuses: " + e.getMessage(), e);
        }
        return list;
    }

    public static DonationStatus findById(int id) {
        return findOne("SELECT id, name FROM donation_statuses WHERE id = ?", id);
    }

    public static DonationStatus findByName(String name) {
        return findOne("SELECT id, name FROM donation_statuses WHERE name = ?", name);
    }

    private static DonationStatus findOne(String sql, Object key) {
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new DonationStatus(rs.getInt("id"), rs.getString("name")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load donation status: " + e.getMessage(), e);
        }
    }
}
