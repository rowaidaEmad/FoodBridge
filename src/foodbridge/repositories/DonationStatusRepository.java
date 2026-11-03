package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.DonationStatus;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for DonationStatus (connection from DatabaseManager).
 * Table: donation_statuses.
 */
public class DonationStatusRepository implements Repository<DonationStatus> {
    private DatabaseManager db;

    public DonationStatusRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(DonationStatus item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO donation_statuses(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                item.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw error("add", item, e);
        }
    }

    @Override
    public DonationStatus findById(int id) {
        return findOne("SELECT id, name FROM donation_statuses WHERE id = ?", id);
    }

    public DonationStatus findByName(String name) {
        return findOne("SELECT id, name FROM donation_statuses WHERE name = ?", name);
    }

    @Override
    public List<DonationStatus> findAll() {
        List<DonationStatus> list = new ArrayList<>();
        try (Connection c = db.getConnection();
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

    @Override
    public void update(DonationStatus item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE donation_statuses SET name = ? WHERE id = ?")) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No donation status with id " + item.getId());
            }
        } catch (SQLException e) {
            throw error("update", item, e);
        }
    }

    /** Throws IllegalStateException if a donation still uses this status. */
    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM donation_statuses WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No donation status with id " + id);
            }
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalStateException("This donation status is in use and cannot be deleted");
            }
            throw new RuntimeException("Could not delete donation status: " + e.getMessage(), e);
        }
    }

    private DonationStatus findOne(String sql, Object key) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new DonationStatus(rs.getInt("id"), rs.getString("name")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load donation status: " + e.getMessage(), e);
        }
    }

    private RuntimeException error(String action, DonationStatus item, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new IllegalArgumentException("Donation status '" + item.getName() + "' already exists");
        }
        return new RuntimeException("Could not " + action + " donation status: " + e.getMessage(), e);
    }
}
