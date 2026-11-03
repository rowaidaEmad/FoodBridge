package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.Unit;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Unit (connection from DatabaseManager).
 * Table: units.
 */
public class UnitRepository implements Repository<Unit> {
    private DatabaseManager db;

    public UnitRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Unit item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO units(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
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
    public Unit findById(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, name FROM units WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Unit(rs.getInt("id"), rs.getString("name")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load unit: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Unit> findAll() {
        List<Unit> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, name FROM units ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Unit(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list units: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Unit item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE units SET name = ? WHERE id = ?")) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No unit with id " + item.getId());
            }
        } catch (SQLException e) {
            throw error("update", item, e);
        }
    }

    /** Throws IllegalStateException if food items or requests still use this unit. */
    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM units WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No unit with id " + id);
            }
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalStateException("This unit is in use and cannot be deleted");
            }
            throw new RuntimeException("Could not delete unit: " + e.getMessage(), e);
        }
    }

    private RuntimeException error(String action, Unit item, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new IllegalArgumentException("Unit '" + item.getName() + "' already exists");
        }
        return new RuntimeException("Could not " + action + " unit: " + e.getMessage(), e);
    }
}
