package foodbridge.repositories;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.*;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;

import foodbridge.models.donations.Donation;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Donation (connection from DatabaseManager).
 */
public class DonationRepository implements Repository<Donation> {
    private DatabaseManager db;

    public DonationRepository() {
        db = DatabaseManager.getInstance();
    }

    /** Read-only selection for the beneficiary delivery form; donation creation stays in its module. */
    public List<FoodDonation> findFoodDonations() { return readFood(0); }

    public FoodDonation findFoodById(int id) {
        return readFood(id).stream().findFirst().orElse(null);
    }

    private List<FoodDonation> readFood(int id) {
        String sql = "SELECT d.id,d.donation_date,f.expiry_date,f.pickup_window,i.id AS item_id,i.name AS item_name,"
            + "i.quantity,i.allergens,i.expiry_date AS item_expiry,i.category_id,c.name AS category_name,i.unit_id,n.name AS unit_name"
            + " FROM donations d JOIN food_donations f ON f.donation_id=d.id"
            + " JOIN donation_statuses s ON s.id=d.status_id"
            + " LEFT JOIN food_items i ON i.food_donation_id=d.id LEFT JOIN food_categories c ON c.id=i.category_id"
            + " LEFT JOIN units n ON n.id=i.unit_id WHERE s.name <> 'CANCELLED'"
            + (id > 0 ? " AND d.id=?" : "") + " ORDER BY d.id DESC,i.id";
        Map<Integer,FoodDonation> result = new LinkedHashMap<>();
        try (Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            if(id > 0) p.setInt(1,id);
            try(ResultSet r=p.executeQuery()) { while(r.next()) {
                int key=r.getInt("id"); FoodDonation food=result.get(key);
                if(food==null) {
                    food=new FoodDonation(); food.setId(key); food.setDate(r.getDate("donation_date").toLocalDate());
                    Date expiry=r.getDate("expiry_date"); food.setExpiryDate(expiry==null ? null : expiry.toLocalDate());
                    food.setPickupWindow(r.getString("pickup_window")); result.put(key,food);
                }
                int itemId=r.getInt("item_id");
                if(!r.wasNull()) {
                    FoodItem item=new FoodItem(); item.setId(itemId); item.setName(r.getString("item_name"));
                    item.setQuantity(r.getBigDecimal("quantity")); item.setAllergens(r.getString("allergens"));
                    Date expiry=r.getDate("item_expiry"); item.setExpiryDate(expiry==null ? null : expiry.toLocalDate());
                    item.setCategory(new FoodCategory(r.getInt("category_id"),r.getString("category_name")));
                    item.setUnit(new Unit(r.getInt("unit_id"),r.getString("unit_name"))); food.addItem(item);
                }
            }}
        } catch(SQLException e) { throw new IllegalStateException("Could not load food donations. Check the database connection.",e); }
        return new ArrayList<>(result.values());
    }

    @Override
    public void add(Donation item) {
        // TODO: implement
    }

    @Override
    public Donation findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Donation> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Donation item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
