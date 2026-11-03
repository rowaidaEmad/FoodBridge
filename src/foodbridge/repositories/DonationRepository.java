package foodbridge.repositories;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Donation (connection from DatabaseManager).
 * Tables: donations (shared columns) + money_donations / food_donations (subtype columns) + food_items.
 * A FoodDonation's items are saved with it; deleting a donation deletes its items (ON DELETE CASCADE).
 */
public class DonationRepository implements Repository<Donation> {
    private DatabaseManager db;

    private static final String SELECT_DONATIONS =
            "SELECT d.id, d.donor_id, d.donation_date, d.donation_type,"
            + " s.id AS status_id, s.name AS status_name,"
            + " a.id AS area_id, a.city, a.district,"
            + " m.amount, m.method, m.transaction_id, m.paid,"
            + " f.expiry_date, f.pickup_window"
            + " FROM donations d"
            + " JOIN donation_statuses s ON s.id = d.status_id"
            + " LEFT JOIN areas a ON a.id = d.area_id"
            + " LEFT JOIN money_donations m ON m.donation_id = d.id"
            + " LEFT JOIN food_donations f ON f.donation_id = d.id";

    public DonationRepository() {
        db = DatabaseManager.getInstance();
    }

    /** Inserts a new donation (status Posted if none is set) or updates an existing one. */
    public void save(Donation item) {
        if (item.getId() == 0) {
            add(item);
        } else {
            update(item);
        }
    }

    @Override
    public void add(Donation item) {
        if (item.getStatus() == null) {
            DonationStatus posted = new DonationStatusRepository().findByName(DonationStatus.POSTED);
            if (posted == null) {
                throw new IllegalStateException("Donation status 'Posted' is missing; run database/seed.sql");
            }
            item.updateStatus(posted);
        }
        inTransaction(item, c -> {
            insertDonation(c, item);
            saveDetails(c, item);
        }, true);
    }

    @Override
    public Donation findById(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_DONATIONS + " WHERE d.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(c, rs, loadDonor(rs.getInt("donor_id"))) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load donation: " + e.getMessage(), e);
        }
    }

    public List<Donation> findByDonor(int donorId) {
        List<Donation> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        SELECT_DONATIONS + " WHERE d.donor_id = ? ORDER BY d.donation_date DESC, d.id DESC")) {
            ps.setInt(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                Donor donor = null;
                while (rs.next()) {
                    if (donor == null) {
                        donor = loadDonor(donorId);
                    }
                    list.add(map(c, rs, donor));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load donations: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Donation> findAll() {
        List<Donation> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_DONATIONS + " ORDER BY d.donation_date DESC, d.id DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(c, rs, loadDonor(rs.getInt("donor_id"))));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list donations: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Donation item) {
        if (item.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        inTransaction(item, c -> {
            updateDonation(c, item);
            saveDetails(c, item);
        }, false);
    }

    /** Subtype rows and food items are removed by ON DELETE CASCADE. */
    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM donations WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No donation with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete donation: " + e.getMessage(), e);
        }
    }

    // ---- saving -------------------------------------------------------------------------------

    private interface SqlWork {
        void run(Connection c) throws SQLException;
    }

    private void inTransaction(Donation item, SqlWork work, boolean isNew) {
        if (item.getDonor() == null || item.getDonor().getId() == 0) {
            throw new IllegalArgumentException("The donor must be saved before the donation");
        }
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                work.run(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (isNew) {
                    item.setId(0);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save donation: " + e.getMessage(), e);
        }
    }

    private void insertDonation(Connection c, Donation item) throws SQLException {
        String sql = "INSERT INTO donations(donor_id, area_id, donation_date, status_id, donation_type)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getDonor().getId());
            setArea(ps, 2, item.getArea());
            ps.setDate(3, Date.valueOf(item.getDate()));
            ps.setInt(4, item.getStatus().getId());
            ps.setString(5, item.getType().toUpperCase());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                item.setId(rs.getInt(1));
            }
        }
    }

    private void updateDonation(Connection c, Donation item) throws SQLException {
        String sql = "UPDATE donations SET area_id = ?, donation_date = ?, status_id = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setArea(ps, 1, item.getArea());
            ps.setDate(2, Date.valueOf(item.getDate()));
            ps.setInt(3, item.getStatus().getId());
            ps.setInt(4, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No donation with id " + item.getId());
            }
        }
    }

    private void saveDetails(Connection c, Donation item) throws SQLException {
        if (item instanceof MoneyDonation m) {
            saveMoney(c, m);
        } else if (item instanceof FoodDonation f) {
            saveFood(c, f);
        }
    }

    private void saveMoney(Connection c, MoneyDonation m) throws SQLException {
        if (m.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (m.getMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        String sql = "INSERT INTO money_donations(donation_id, amount, method, transaction_id, paid)"
                + " VALUES (?, ?, ?, ?, ?)"
                + " ON CONFLICT (donation_id) DO UPDATE SET amount = EXCLUDED.amount, method = EXCLUDED.method,"
                + " transaction_id = EXCLUDED.transaction_id, paid = EXCLUDED.paid";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, m.getId());
            ps.setDouble(2, m.getAmount());
            ps.setString(3, m.getMethod());
            ps.setString(4, m.getTransactionId());
            ps.setBoolean(5, m.isPaid());
            ps.executeUpdate();
        }
    }

    private void saveFood(Connection c, FoodDonation f) throws SQLException {
        if (f.getItems().isEmpty()) {
            throw new IllegalArgumentException("A food donation needs at least one item");
        }
        String sql = "INSERT INTO food_donations(donation_id, expiry_date, pickup_window) VALUES (?, ?, ?)"
                + " ON CONFLICT (donation_id) DO UPDATE SET expiry_date = EXCLUDED.expiry_date,"
                + " pickup_window = EXCLUDED.pickup_window";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, f.getId());
            ps.setDate(2, f.getExpiryDate() == null ? null : Date.valueOf(f.getExpiryDate()));
            ps.setString(3, f.getPickupWindow());
            ps.executeUpdate();
        }
        // Items removed with removeItem() are deleted; the rest are updated or inserted.
        Integer[] keep = f.getItems().stream().map(FoodItem::getId).filter(id -> id != 0).toArray(Integer[]::new);
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM food_items WHERE food_donation_id = ? AND NOT (id = ANY (?))")) {
            ps.setInt(1, f.getId());
            ps.setArray(2, c.createArrayOf("bigint", keep));
            ps.executeUpdate();
        }
        for (FoodItem item : f.getItems()) {
            saveItem(c, item, f.getId());
        }
    }

    private void saveItem(Connection c, FoodItem item, int foodDonationId) throws SQLException {
        if (item.getName() == null || item.getQuantity() <= 0 || item.getCategory() == null
                || item.getCategory().getId() == 0 || item.getUnit() == null || item.getUnit().getId() == 0) {
            throw new IllegalArgumentException("Each item needs a name, a quantity > 0, a saved category and a saved unit");
        }
        if (item.getId() != 0) {
            String sql = "UPDATE food_items SET name = ?, quantity = ?, category_id = ?, unit_id = ?, allergens = ?,"
                    + " expiry_date = ? WHERE id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                fillItem(ps, 1, item);
                ps.setInt(7, item.getId());
                if (ps.executeUpdate() > 0) {
                    return;
                }
            }
        }
        String sql = "INSERT INTO food_items(food_donation_id, name, quantity, category_id, unit_id, allergens, expiry_date)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, foodDonationId);
            fillItem(ps, 2, item);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                item.setId(rs.getInt(1));
            }
        }
    }

    private void fillItem(PreparedStatement ps, int start, FoodItem item) throws SQLException {
        ps.setString(start, item.getName());
        ps.setDouble(start + 1, item.getQuantity());
        ps.setInt(start + 2, item.getCategory().getId());
        ps.setInt(start + 3, item.getUnit().getId());
        ps.setString(start + 4, item.getAllergens());
        ps.setDate(start + 5, item.getExpiryDate() == null ? null : Date.valueOf(item.getExpiryDate()));
    }

    private void setArea(PreparedStatement ps, int index, Area area) throws SQLException {
        if (area == null || area.getId() == 0) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setInt(index, area.getId());
        }
    }

    // ---- loading ------------------------------------------------------------------------------

    /** Rebuilds a stored donation; this is not new-donation creation (that belongs to the factory). */
    private Donation map(Connection c, ResultSet rs, Donor donor) throws SQLException {
        Donation d;
        if ("MONEY".equals(rs.getString("donation_type"))) {
            MoneyDonation m = new MoneyDonation(donor);
            m.setAmount(rs.getDouble("amount"));
            m.setMethod(rs.getString("method"));
            String transactionId = rs.getString("transaction_id");
            if (rs.getBoolean("paid") && transactionId != null) {
                m.markAsPaid(transactionId);
            }
            d = m;
        } else {
            FoodDonation f = new FoodDonation(donor);
            Date expiry = rs.getDate("expiry_date");
            f.setExpiryDate(expiry == null ? null : expiry.toLocalDate());
            f.setPickupWindow(rs.getString("pickup_window"));
            d = f;
        }
        d.setId(rs.getInt("id"));
        d.setDate(rs.getDate("donation_date").toLocalDate());
        d.updateStatus(new DonationStatus(rs.getInt("status_id"), rs.getString("status_name")));
        int areaId = rs.getInt("area_id");
        d.setArea(rs.wasNull() ? null : new Area(areaId, rs.getString("city"), rs.getString("district")));
        if (d instanceof FoodDonation f) {
            for (FoodItem item : findItems(c, f.getId())) {
                f.addItem(item);
            }
        }
        return d;
    }

    private List<FoodItem> findItems(Connection c, int foodDonationId) throws SQLException {
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
                    item.setId(rs.getInt("id"));
                    item.setName(rs.getString("name"));
                    item.setQuantity(rs.getDouble("quantity"));
                    item.setAllergens(rs.getString("allergens"));
                    Date expiry = rs.getDate("expiry_date");
                    item.setExpiryDate(expiry == null ? null : expiry.toLocalDate());
                    item.setCategory(new FoodCategory(rs.getInt("category_id"), rs.getString("category_name")));
                    item.setUnit(new Unit(rs.getInt("unit_id"), rs.getString("unit_name")));
                    list.add(item);
                }
            }
        }
        return list;
    }

    private Donor loadDonor(int donorId) {
        User user = new UserRepository().findById(donorId);
        if (!(user instanceof Donor donor)) {
            throw new IllegalStateException("User " + donorId + " is not a donor");
        }
        return donor;
    }
}
