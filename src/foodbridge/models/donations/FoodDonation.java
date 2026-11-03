package foodbridge.models.donations;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import foodbridge.models.users.Donor;

// Factory Method pattern - ConcreteProduct
/**
 * FACTORY METHOD - ConcreteProduct. Contains 1..* FoodItem (composition).
 * Tables: food_donations + food_items (items are deleted with the donation, ON DELETE CASCADE).
 */
public class FoodDonation extends Donation {
    private List<FoodItem> items = new ArrayList<>();
    private LocalDate expiryDate;
    private String pickupWindow;

    /** Used when loading from the database. */
    private FoodDonation() {
    }

    public FoodDonation(Donor donor) {
        super(donor);
    }

    public void addItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required");
        }
        items.add(item);
    }

    /** Removes a saved item; the row is deleted from the database on the next save(). */
    public void removeItem(int itemId) {
        if (!items.removeIf(item -> item.getId() == itemId && itemId != 0)) {
            throw new IllegalArgumentException("No item with id " + itemId + " in this donation");
        }
    }

    public List<FoodItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPickupWindow() {
        return pickupWindow;
    }

    public void setPickupWindow(String window) {
        this.pickupWindow = window;
    }

    public double getTotalQuantity() {
        double total = 0;
        for (FoodItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    @Override
    public String getType() {
        return "food";
    }

    /** e.g. "Food donation - 20 meals - Posted", or "... - 3 items - ..." when the units differ. */
    @Override
    public String getDetails() {
        String unit = items.isEmpty() || items.get(0).getUnit() == null ? null : items.get(0).getUnit().getName();
        for (FoodItem item : items) {
            if (item.getUnit() == null || !item.getUnit().getName().equals(unit)) {
                unit = null;
                break;
            }
        }
        String amount = unit != null
                ? FoodItem.formatQuantity(getTotalQuantity()) + " " + unit
                : items.size() + (items.size() == 1 ? " item" : " items");
        return "Food donation - " + amount + " - " + statusName();
    }

    @Override
    protected void saveDetails(Connection c) throws SQLException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A food donation needs at least one item");
        }
        String sql = "INSERT INTO food_donations(donation_id, expiry_date, pickup_window) VALUES (?, ?, ?)"
                + " ON CONFLICT (donation_id) DO UPDATE SET expiry_date = EXCLUDED.expiry_date,"
                + " pickup_window = EXCLUDED.pickup_window";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setDate(2, expiryDate == null ? null : Date.valueOf(expiryDate));
            ps.setString(3, pickupWindow);
            ps.executeUpdate();
        }
        deleteRemovedItems(c);
        for (FoodItem item : items) {
            item.save(c, id);
        }
    }

    private void deleteRemovedItems(Connection c) throws SQLException {
        Integer[] keep = items.stream().map(FoodItem::getId).filter(itemId -> itemId != 0).toArray(Integer[]::new);
        String sql = "DELETE FROM food_items WHERE food_donation_id = ? AND NOT (id = ANY (?))";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setArray(2, c.createArrayOf("bigint", keep));
            ps.executeUpdate();
        }
    }

    static FoodDonation load(Connection c, ResultSet rs, Donor donor) throws SQLException {
        FoodDonation d = new FoodDonation();
        d.readCommon(rs, donor);
        Date expiry = rs.getDate("expiry_date");
        d.expiryDate = expiry == null ? null : expiry.toLocalDate();
        d.pickupWindow = rs.getString("pickup_window");
        d.items.addAll(FoodItem.findByDonation(c, d.id));
        return d;
    }
}
