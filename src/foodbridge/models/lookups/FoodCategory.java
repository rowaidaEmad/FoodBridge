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
 * Lookup table (CRUD). Table: food_categories.
 */
public class FoodCategory {
    private int id;
    private String name;

    public FoodCategory() {
    }

    public FoodCategory(String name) {
        setName(name);
    }

    public FoodCategory(int id, String name) {
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

    /** Inserts a new category or renames an existing one. */
    public void save() {
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (id == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO food_categories(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE food_categories SET name = ? WHERE id = ?")) {
                    ps.setString(1, name);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Food category '" + name + "' already exists");
            }
            throw new RuntimeException("Could not save food category: " + e.getMessage(), e);
        }
    }

    /** Returns false if the category does not exist or is still used (e.g. by food items). */
    public boolean delete() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM food_categories WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                return false;
            }
            throw new RuntimeException("Could not delete food category: " + e.getMessage(), e);
        }
    }

    public static List<FoodCategory> findAll() {
        List<FoodCategory> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, name FROM food_categories ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new FoodCategory(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list food categories: " + e.getMessage(), e);
        }
        return list;
    }

    public static FoodCategory findById(int id) {
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, name FROM food_categories WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new FoodCategory(rs.getInt("id"), rs.getString("name")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load food category: " + e.getMessage(), e);
        }
    }
}
