package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.FoodCategory;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for FoodCategory (connection from DatabaseManager).
 * Table: food_categories.
 */
public class FoodCategoryRepository implements Repository<FoodCategory> {
    private DatabaseManager db;

    public FoodCategoryRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(FoodCategory item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO food_categories(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
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
    public FoodCategory findById(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, name FROM food_categories WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new FoodCategory(rs.getInt("id"), rs.getString("name")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load food category: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FoodCategory> findAll() {
        List<FoodCategory> list = new ArrayList<>();
        try (Connection c = db.getConnection();
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

    @Override
    public void update(FoodCategory item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE food_categories SET name = ? WHERE id = ?")) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No food category with id " + item.getId());
            }
        } catch (SQLException e) {
            throw error("update", item, e);
        }
    }

    /** Throws IllegalStateException if food items or requests still use this category. */
    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM food_categories WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No food category with id " + id);
            }
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalStateException("This food category is in use and cannot be deleted");
            }
            throw new RuntimeException("Could not delete food category: " + e.getMessage(), e);
        }
    }

    private RuntimeException error(String action, FoodCategory item, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new IllegalArgumentException("Food category '" + item.getName() + "' already exists");
        }
        return new RuntimeException("Could not " + action + " food category: " + e.getMessage(), e);
    }
}
