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
 * Table: units (see database/schema.sql).
 */
public class UnitRepository implements Repository<Unit> {
    private DatabaseManager db;

    public UnitRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Unit item) {
        String sql = "INSERT INTO units(name) VALUES (?)";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not add unit: " + e.getMessage(), e);
        }
    }

    @Override
    public Unit findById(int id) {
        String sql = "SELECT id, name FROM units WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
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
        String sql = "SELECT id, name FROM units ORDER BY id";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
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
        String sql = "UPDATE units SET name = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No unit with id " + item.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update unit: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM units WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No unit with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete unit: " + e.getMessage(), e);
        }
    }
}
