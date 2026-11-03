package foodbridge.models.donations;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.patterns.singleton.DatabaseManager;
import foodbridge.repositories.UserRepository;

// Factory Method pattern - Product
/**
 * FACTORY METHOD - Product. Made by a Donor, has a DonationStatus, located in an Area, has 0..1 Receipt.
 * Tables: donations (shared columns) + money_donations / food_donations (subtype columns).
 */
public abstract class Donation {
    protected int id;
    protected Donor donor;
    protected LocalDate date;
    protected DonationStatus status;
    protected Area area;

    // Columns shared by findById / findByDonor; subtype columns are read by the child classes.
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

    /** Used when loading from the database. */
    protected Donation() {
    }

    protected Donation(Donor donor) {
        if (donor == null) {
            throw new IllegalArgumentException("A donation needs a donor");
        }
        this.donor = donor;
        this.date = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public Donor getDonor() {
        return donor;
    }

    public LocalDate getDate() {
        return date;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    /** Changes the status in memory; call save() to store it. */
    public void updateStatus(DonationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        this.status = status;
    }

    /** Sets the status to Cancelled only while it is still Posted; call save() to store it. */
    public boolean cancel() {
        if (status == null || !DonationStatus.POSTED.equals(status.getName())) {
            return false;
        }
        DonationStatus cancelled = DonationStatus.findByName(DonationStatus.CANCELLED);
        if (cancelled == null) {
            throw new IllegalStateException("Donation status 'Cancelled' is missing; run database/seed.sql");
        }
        updateStatus(cancelled);
        return true;
    }

    public String getDetails() {
        return capitalize(getType()) + " donation - " + statusName();
    }

    public abstract String getType();

    /** Inserts the donation (status Posted if none is set) or updates it, in one transaction. */
    public void save() {
        if (donor == null || donor.getId() == 0) {
            throw new IllegalArgumentException("The donor must be saved before the donation");
        }
        if (status == null) {
            status = DonationStatus.findByName(DonationStatus.POSTED);
            if (status == null) {
                throw new IllegalStateException("Donation status 'Posted' is missing; run database/seed.sql");
            }
        }
        boolean isNew = id == 0;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                if (isNew) {
                    insert(c);
                } else {
                    update(c);
                }
                saveDetails(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (isNew) {
                    id = 0;
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save donation: " + e.getMessage(), e);
        }
    }

    public static Donation findById(int id) {
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_DONATIONS + " WHERE d.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return load(c, rs, loadDonor(rs.getInt("donor_id")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load donation: " + e.getMessage(), e);
        }
    }

    public static List<Donation> findByDonor(int donorId) {
        List<Donation> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(
                        SELECT_DONATIONS + " WHERE d.donor_id = ? ORDER BY d.donation_date DESC, d.id DESC")) {
            ps.setInt(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                Donor donor = null;
                while (rs.next()) {
                    if (donor == null) {
                        donor = loadDonor(donorId);
                    }
                    list.add(load(c, rs, donor));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load donations: " + e.getMessage(), e);
        }
        return list;
    }

    /** Saves the child-specific columns (and, for food, the items) inside the save() transaction. */
    protected abstract void saveDetails(Connection c) throws SQLException;

    /** Fills the shared fields from a SELECT_DONATIONS row. */
    protected void readCommon(ResultSet rs, Donor donor) throws SQLException {
        this.id = rs.getInt("id");
        this.donor = donor;
        this.date = rs.getDate("donation_date").toLocalDate();
        this.status = new DonationStatus(rs.getInt("status_id"), rs.getString("status_name"));
        int areaId = rs.getInt("area_id");
        this.area = rs.wasNull() ? null : new Area(areaId, rs.getString("city"), rs.getString("district"));
    }

    protected String statusName() {
        return status == null ? "New" : status.getName();
    }

    private void insert(Connection c) throws SQLException {
        String sql = "INSERT INTO donations(donor_id, area_id, donation_date, status_id, donation_type)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, donor.getId());
            setArea(ps, 2);
            ps.setDate(3, Date.valueOf(date));
            ps.setInt(4, status.getId());
            ps.setString(5, getType().toUpperCase());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
        }
    }

    private void update(Connection c) throws SQLException {
        String sql = "UPDATE donations SET area_id = ?, donation_date = ?, status_id = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setArea(ps, 1);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, status.getId());
            ps.setInt(4, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No donation with id " + id);
            }
        }
    }

    private void setArea(PreparedStatement ps, int index) throws SQLException {
        if (area == null || area.getId() == 0) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setInt(index, area.getId());
        }
    }

    private static Donation load(Connection c, ResultSet rs, Donor donor) throws SQLException {
        if ("MONEY".equals(rs.getString("donation_type"))) {
            return MoneyDonation.load(rs, donor);
        }
        return FoodDonation.load(c, rs, donor);
    }

    private static Donor loadDonor(int donorId) {
        User user = new UserRepository().findById(donorId);
        if (!(user instanceof Donor donor)) {
            throw new IllegalStateException("User " + donorId + " is not a donor");
        }
        return donor;
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
