package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.Area;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Area (connection from DatabaseManager).
 * Table: areas.
 */
public class AreaRepository implements Repository<Area> {
    private DatabaseManager db;

    public AreaRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Area item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO areas(city, district) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getCity());
            ps.setString(2, item.getDistrict());
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
    public Area findById(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, city, district FROM areas WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load area: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Area> findAll() {
        List<Area> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, city, district FROM areas ORDER BY city, district");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list areas: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Area item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE areas SET city = ?, district = ? WHERE id = ?")) {
            ps.setString(1, item.getCity());
            ps.setString(2, item.getDistrict());
            ps.setInt(3, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No area with id " + item.getId());
            }
        } catch (SQLException e) {
            throw error("update", item, e);
        }
    }

    /** Throws IllegalStateException if users or donations still use this area. */
    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM areas WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No area with id " + id);
            }
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalStateException("This area is in use and cannot be deleted");
            }
            throw new RuntimeException("Could not delete area: " + e.getMessage(), e);
        }
    }

    private Area map(ResultSet rs) throws SQLException {
        return new Area(rs.getInt("id"), rs.getString("city"), rs.getString("district"));
    }

    private RuntimeException error(String action, Area item, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new IllegalArgumentException("Area '" + item + "' already exists");
        }
        return new RuntimeException("Could not " + action + " area: " + e.getMessage(), e);
    }
}
