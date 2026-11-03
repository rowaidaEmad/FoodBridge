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
 * Lookup table (CRUD). Table: units.
 */
public class Unit {
    private int id;
    private String name;

    public Unit() {
    }

    public Unit(String name) {
        setName(name);
    }

    public Unit(int id, String name) {
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

    /** Inserts a new unit or renames an existing one. */
    public void save() {
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (id == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO units(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE units SET name = ? WHERE id = ?")) {
                    ps.setString(1, name);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Unit '" + name + "' already exists");
            }
            throw new RuntimeException("Could not save unit: " + e.getMessage(), e);
        }
    }

    /** Returns false if the unit does not exist or is still used (e.g. by food items). */
    public boolean delete() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM units WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                return false;
            }
            throw new RuntimeException("Could not delete unit: " + e.getMessage(), e);
        }
    }

    public static List<Unit> findAll() {
        List<Unit> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
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

    public static Unit findById(int id) {
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, name FROM units WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Unit(rs.getInt("id"), rs.getString("name")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load unit: " + e.getMessage(), e);
        }
    }
}
