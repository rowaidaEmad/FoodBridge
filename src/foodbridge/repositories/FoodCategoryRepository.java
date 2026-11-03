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
 * Table: food_categories (see database/schema.sql).
 */
public class FoodCategoryRepository implements Repository<FoodCategory> {
    private DatabaseManager db;

    public FoodCategoryRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(FoodCategory item) {
        String sql = "INSERT INTO food_categories(name) VALUES (?)";
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
            throw new RuntimeException("Could not add food category: " + e.getMessage(), e);
        }
    }

    @Override
    public FoodCategory findById(int id) {
        String sql = "SELECT id, name FROM food_categories WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
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
        String sql = "SELECT id, name FROM food_categories ORDER BY id";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new FoodCategory(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list food categorys: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(FoodCategory item) {
        String sql = "UPDATE food_categories SET name = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No food category with id " + item.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update food category: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM food_categories WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No food category with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete food category: " + e.getMessage(), e);
        }
    }
}
