package foodbridge.models.donations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;

/**
 * Core domain. Belongs to a FoodCategory, measured in a Unit.
 * Table: food_items. Saved and loaded only through its FoodDonation (composition).
 */
public class FoodItem {
    private int id;
    private String name;
    private double quantity;
    private String allergens;
    private LocalDate expiryDate;
    private FoodCategory category;
    private Unit unit;

    public FoodItem() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        this.name = name.trim();
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.quantity = quantity;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /** e.g. "Rice - 10 kg - Grains (allergens: none)". */
    public String getInfo() {
        return name + " - " + formatQuantity(quantity) + " " + (unit == null ? "" : unit.getName())
                + " - " + (category == null ? "Uncategorized" : category.getName())
                + " (allergens: " + (allergens == null || allergens.isBlank() ? "none" : allergens) + ")";
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /** 10.0 -> "10", 2.5 -> "2.5". */
    static String formatQuantity(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    /** Inserts or updates this item inside the FoodDonation's save() transaction. */
    void save(Connection c, int foodDonationId) throws SQLException {
        if (name == null || quantity <= 0 || category == null || category.getId() == 0
                || unit == null || unit.getId() == 0) {
            throw new IllegalArgumentException("Each item needs a name, a quantity > 0, a saved category and a saved unit");
        }
        if (id != 0 && update(c)) {
            return;
        }
        String sql = "INSERT INTO food_items(food_donation_id, name, quantity, category_id, unit_id, allergens, expiry_date)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, foodDonationId);
            fill(ps, 2);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
        }
    }

    private boolean update(Connection c) throws SQLException {
        String sql = "UPDATE food_items SET name = ?, quantity = ?, category_id = ?, unit_id = ?, allergens = ?,"
                + " expiry_date = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, 1);
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void fill(PreparedStatement ps, int start) throws SQLException {
        ps.setString(start, name);
        ps.setDouble(start + 1, quantity);
        ps.setInt(start + 2, category.getId());
        ps.setInt(start + 3, unit.getId());
        ps.setString(start + 4, allergens);
        ps.setDate(start + 5, expiryDate == null ? null : Date.valueOf(expiryDate));
    }

    static List<FoodItem> findByDonation(Connection c, int foodDonationId) throws SQLException {
        String sql = "SELECT i.id, i.name, i.quantity, i.allergens, i.expiry_date,"
                + " fc.id AS category_id, fc.name AS category_name, u.id AS unit_id, u.name AS unit_name"
                + " FROM food_items i"
                + " JOIN food_categories fc ON fc.id = i.category_id"
                + " JOIN units u ON u.id = i.unit_id"
                + " WHERE i.food_donation_id = ? ORDER BY i.id";
        List<FoodItem> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, foodDonationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodItem item = new FoodItem();
                    item.id = rs.getInt("id");
                    item.name = rs.getString("name");
                    item.quantity = rs.getDouble("quantity");
                    item.allergens = rs.getString("allergens");
                    Date expiry = rs.getDate("expiry_date");
                    item.expiryDate = expiry == null ? null : expiry.toLocalDate();
                    item.category = new FoodCategory(rs.getInt("category_id"), rs.getString("category_name"));
                    item.unit = new Unit(rs.getInt("unit_id"), rs.getString("unit_name"));
                    list.add(item);
                }
            }
        }
        return list;
    }
}
