package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.donations.FoodDonation;
import foodbridge.models.enums.PickupTaskStatus;
import foodbridge.models.lookups.Area;
import foodbridge.models.operations.PickupTask;
import foodbridge.models.operations.VolunteerHours;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for PickupTask (connection from DatabaseManager).
 */
public class PickupTaskRepository implements Repository<PickupTask> {
    private static final String SELECT_TASKS =
            "SELECT t.id, t.status, t.required_skill, t.food_donation_id, t.volunteer_id, t.beneficiary_id,"
            + " t.pickup_time, t.delivery_time, t.hours_spent, fd.pickup_window, d.donor_id,"
            + " d.area_id, ar.city, ar.district, b.name AS beneficiary_name,"
            + " string_agg(fi.name || ' ' || trim_scale(fi.quantity), ', ' ORDER BY fi.id) AS items"
            + " FROM pickup_tasks t"
            + " JOIN food_donations fd ON fd.donation_id = t.food_donation_id"
            + " JOIN donations d ON d.id = t.food_donation_id"
            + " LEFT JOIN food_items fi ON fi.food_donation_id = t.food_donation_id"
            + " LEFT JOIN areas ar ON ar.id = d.area_id"
            + " LEFT JOIN users b ON b.id = t.beneficiary_id";
    private static final String AVAILABLE = " WHERE t.status = 'PENDING'"
            + " AND d.status_id = (SELECT id FROM donation_statuses WHERE name = 'Posted')";
    private static final String GROUP_AND_ORDER =
            " GROUP BY t.id, fd.pickup_window, d.donor_id, d.area_id, ar.city, ar.district, b.name ORDER BY t.id";

    private final DatabaseManager db;

    public PickupTaskRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(PickupTask item) {
        String sql = "INSERT INTO pickup_tasks(food_donation_id, beneficiary_id, required_skill) VALUES (?, ?, ?)";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getDonation().getId());
            JdbcSupport.setNullableInt(ps, 2, idOf(item.getBeneficiary()));
            ps.setString(3, item.getRequiredSkill());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not add task: " + e.getMessage(), e);
        }
    }

    @Override
    public PickupTask findById(int id) {
        List<PickupTask> tasks = query(" WHERE t.id = ?", id);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    @Override
    public List<PickupTask> findAll() {
        return query("");
    }

    /** Pending tasks whose donation is still Posted, so a cancelled donation is not offered. */
    public List<PickupTask> findAvailable() {
        return query(AVAILABLE);
    }

    /** As findAvailable, in the given area. A donation with no area is offered in every area. */
    public List<PickupTask> findAvailableInArea(int areaId) {
        return query(AVAILABLE + " AND (d.area_id = ? OR d.area_id IS NULL)", areaId);
    }

    /**
     * Opens a pending task for each Posted food donation that has none, so donations saved by the donation
     * module reach volunteers. The beneficiary is the active beneficiary in the donation's area (any active
     * beneficiary if none lives there), so the delivery has someone to go to.
     */
    public void openTasksForNewFoodDonations() {
        String sql = "INSERT INTO pickup_tasks(food_donation_id, beneficiary_id)"
                + " SELECT fd.donation_id, (SELECT b.user_id FROM beneficiaries b JOIN users u ON u.id = b.user_id"
                + " WHERE u.is_active ORDER BY (u.area_id IS NOT DISTINCT FROM d.area_id) DESC, b.user_id LIMIT 1)"
                + " FROM food_donations fd"
                + " JOIN donations d ON d.id = fd.donation_id"
                + " JOIN donation_statuses s ON s.id = d.status_id"
                + " WHERE s.name = 'Posted'"
                + " AND NOT EXISTS (SELECT 1 FROM pickup_tasks t WHERE t.food_donation_id = fd.donation_id)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not open pickup tasks: " + e.getMessage(), e);
        }
    }

    public List<PickupTask> findByVolunteer(int volunteerId) {
        return query(" WHERE t.volunteer_id = ?", volunteerId);
    }

    @Override
    public void update(PickupTask item) {
        String sql = "UPDATE pickup_tasks SET volunteer_id = ?, beneficiary_id = ?, status = ?, pickup_time = ?,"
                + " delivery_time = ?, hours_spent = ?, required_skill = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            JdbcSupport.setNullableInt(ps, 1, idOf(item.getVolunteer()));
            JdbcSupport.setNullableInt(ps, 2, idOf(item.getBeneficiary()));
            ps.setString(3, item.getStatus().name());
            ps.setTimestamp(4, JdbcSupport.toTimestamp(item.getPickupTime()));
            ps.setTimestamp(5, JdbcSupport.toTimestamp(item.getDeliveryTime()));
            ps.setDouble(6, item.getHoursSpent());
            ps.setString(7, item.getRequiredSkill());
            ps.setInt(8, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No task with id " + item.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update task: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM pickup_tasks WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No task with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete task: " + e.getMessage(), e);
        }
    }

    /** Takes a pending task for a volunteer. Returns false if someone else got it first. */
    public boolean claim(int taskId, int volunteerId) {
        String sql = "UPDATE pickup_tasks SET volunteer_id = ?, status = 'ASSIGNED' WHERE id = ? AND status = 'PENDING'";
        return changesOneRow(sql, volunteerId, taskId);
    }

    public boolean release(int taskId, int volunteerId) {
        String sql = "UPDATE pickup_tasks SET volunteer_id = NULL, status = 'PENDING'"
                + " WHERE id = ? AND volunteer_id = ? AND status = 'ASSIGNED'";
        return changesOneRow(sql, taskId, volunteerId);
    }

    public void savePickedUp(PickupTask task) {
        String sql = "UPDATE pickup_tasks SET status = ?, pickup_time = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, task.getStatus().name());
            ps.setTimestamp(2, JdbcSupport.toTimestamp(task.getPickupTime()));
            ps.setInt(3, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save pickup: " + e.getMessage(), e);
        }
    }

    /** Saves the delivery, its distribution and the volunteer's hours together. */
    public void completeDelivery(PickupTask task) {
        JdbcSupport.inTransaction(db, "Could not complete delivery", c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE pickup_tasks SET status = ?, delivery_time = ?, hours_spent = ? WHERE id = ?")) {
                ps.setString(1, task.getStatus().name());
                ps.setTimestamp(2, JdbcSupport.toTimestamp(task.getDeliveryTime()));
                ps.setDouble(3, task.getHoursSpent());
                ps.setInt(4, task.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO distributions(food_donation_id, beneficiary_id, volunteer_id, pickup_task_id, quantity)"
                    + " SELECT t.food_donation_id, t.beneficiary_id, t.volunteer_id, t.id,"
                    + " (SELECT SUM(i.quantity) FROM food_items i WHERE i.food_donation_id = t.food_donation_id)"
                    + " FROM pickup_tasks t WHERE t.id = ?")) {
                ps.setInt(1, task.getId());
                ps.executeUpdate();
            }
            VolunteerRepository.recordHours(c, task.getVolunteer().getId(), task.getHoursSpent(),
                    VolunteerHours.PICKUP, "Pickup task #" + task.getId());
        });
    }

    private List<PickupTask> query(String where, int... params) {
        List<PickupTask> tasks = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_TASKS + where + GROUP_AND_ORDER)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load tasks: " + e.getMessage(), e);
        }
        return tasks;
    }

    private boolean changesOneRow(String sql, int first, int second) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, first);
            ps.setInt(2, second);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update task: " + e.getMessage(), e);
        }
    }

    private PickupTask map(ResultSet rs) throws SQLException {
        PickupTask task = new PickupTask();
        task.setId(rs.getInt("id"));
        task.setStatus(PickupTaskStatus.valueOf(rs.getString("status")));
        task.setRequiredSkill(rs.getString("required_skill"));
        task.setPickupTime(JdbcSupport.getDateTime(rs, "pickup_time"));
        task.setDeliveryTime(JdbcSupport.getDateTime(rs, "delivery_time"));
        task.setHoursSpent(rs.getDouble("hours_spent"));
        task.setItemsSummary(rs.getString("items"));
        task.setDonation(mapDonation(rs));
        int areaId = rs.getInt("area_id");
        if (!rs.wasNull()) {
            task.setArea(new Area(areaId, rs.getString("city"), rs.getString("district")));
        }
        int volunteerId = rs.getInt("volunteer_id");
        if (!rs.wasNull()) {
            Volunteer volunteer = new Volunteer();
            volunteer.setId(volunteerId);
            task.setVolunteer(volunteer);
        }
        int beneficiaryId = rs.getInt("beneficiary_id");
        if (!rs.wasNull()) {
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setId(beneficiaryId);
            beneficiary.setName(rs.getString("beneficiary_name"));
            task.setBeneficiary(beneficiary);
        }
        return task;
    }

    private FoodDonation mapDonation(ResultSet rs) throws SQLException {
        Donor donor = new Donor();
        donor.setId(rs.getInt("donor_id"));
        FoodDonation donation = new FoodDonation(donor);
        donation.setId(rs.getInt("food_donation_id"));
        donation.setPickupWindow(rs.getString("pickup_window"));
        return donation;
    }

    private static Integer idOf(User user) {
        return user == null ? null : user.getId();
    }
}
