package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.Area;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for User (connection from DatabaseManager).
 * Tables: users + one subtype table per role (donors, volunteers, beneficiaries, admins).
 */
public class UserRepository implements Repository<User> {
    private DatabaseManager db;

    private static final String SELECT_USERS =
            "SELECT u.id, u.name, u.email, u.password_hash, u.phone, u.address, u.area_id, a.city, a.district, u.role, u.is_active, u.created_at,"
            + " d.donor_type, d.organization_name,"
            + " v.availability, v.has_transport, v.total_hours,"
            + " b.household_size, b.needs"
            + " FROM users u"
            + " LEFT JOIN areas a ON a.id = u.area_id"
            + " LEFT JOIN donors d ON d.user_id = u.id"
            + " LEFT JOIN volunteers v ON v.user_id = u.id"
            + " LEFT JOIN beneficiaries b ON b.user_id = u.id";

    public UserRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(User item) {
        String sql = "INSERT INTO users(name, email, password_hash, phone, address, area_id, role, is_active)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, item.getName());
                ps.setString(2, item.getEmail());
                ps.setString(3, item.getPasswordHash());
                ps.setString(4, item.getPhone());
                ps.setString(5, item.getAddress());
                JdbcSupport.setNullableInt(ps, 6, item.getArea() == null ? null : item.getArea().getId());
                ps.setString(7, roleOf(item));
                ps.setBoolean(8, item.isActive());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    item.setId(rs.getInt(1));
                }
                insertSubtype(c, item);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not add user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findById(int id) {
        return findOne(SELECT_USERS + " WHERE u.id = ?", id);
    }

    public User findByEmail(String email) {
        return findOne(SELECT_USERS + " WHERE LOWER(u.email) = LOWER(?)", email);
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_USERS + " ORDER BY u.id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list users: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(User item) {
        String sql = "UPDATE users SET name = ?, email = ?, password_hash = ?, phone = ?, address = ?, area_id = ?,"
                + " is_active = ? WHERE id = ?";
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, item.getName());
                ps.setString(2, item.getEmail());
                ps.setString(3, item.getPasswordHash());
                ps.setString(4, item.getPhone());
                ps.setString(5, item.getAddress());
                JdbcSupport.setNullableInt(ps, 6, item.getArea() == null ? null : item.getArea().getId());
                ps.setBoolean(7, item.isActive());
                ps.setInt(8, item.getId());
                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("No user with id " + item.getId());
                }
                updateSubtype(c, item);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        // Subtype rows are removed by ON DELETE CASCADE.
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No user with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete user: " + e.getMessage(), e);
        }
    }

    /** Lets any user (for example a volunteer) donate: adds a donor profile if they have none. */
    public void ensureDonorProfile(int userId) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO donors(user_id, donor_type) VALUES (?, 'Individual') ON CONFLICT (user_id) DO NOTHING")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not create donor profile: " + e.getMessage(), e);
        }
    }

    /** Updates beneficiary fields without deleting a subtype row referenced by deliveries. */
    public void updateBeneficiaryProfile(int id, int householdSize, String needs) {
        if (householdSize <= 0) throw new IllegalArgumentException("Household size must be at least 1.");
        try (Connection c = db.getConnection(); PreparedStatement p = c.prepareStatement(
                "UPDATE beneficiaries SET household_size=?, needs=? WHERE user_id=?")) {
            p.setInt(1, householdSize); p.setString(2, needs); p.setInt(3, id);
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Beneficiary no longer exists.");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save beneficiary profile. Check the database connection.", e);
        }
    }

    private User findOne(String sql, Object key) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load user: " + e.getMessage(), e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user;
        switch (rs.getString("role")) {
            case "DONOR" -> {
                Donor d = new Donor();
                d.setDonorType(rs.getString("donor_type"));
                d.setOrganizationName(rs.getString("organization_name"));
                user = d;
            }
            case "VOLUNTEER" -> {
                Volunteer v = new Volunteer();
                v.setAvailability(rs.getString("availability"));
                v.setHasTransport(rs.getBoolean("has_transport"));
                v.addHours(rs.getDouble("total_hours"));
                user = v;
            }
            case "BENEFICIARY" -> {
                Beneficiary b = new Beneficiary();
                int size = rs.getInt("household_size");
                if (size > 0) {
                    b.setHouseholdSize(size);
                }
                b.updateNeeds(rs.getString("needs"));
                user = b;
            }
            default -> user = new Admin();
        }
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.changeEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.updateContactInfo(rs.getString("phone"), rs.getString("address"));
        int areaId = rs.getInt("area_id");
        if (!rs.wasNull()) {
            user.setArea(new Area(areaId, rs.getString("city"), rs.getString("district")));
        }
        user.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        user.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return user;
    }

    private void insertSubtype(Connection c, User item) throws SQLException {
        if (item instanceof Donor d) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO donors(user_id, donor_type, organization_name) VALUES (?, ?, ?)")) {
                ps.setInt(1, d.getId());
                ps.setString(2, d.getDonorType());
                ps.setString(3, d.getOrganizationName());
                ps.executeUpdate();
            }
        } else if (item instanceof Volunteer v) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO volunteers(user_id, availability, has_transport, total_hours) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, v.getId());
                ps.setString(2, v.getAvailability());
                ps.setBoolean(3, v.hasTransport());
                ps.setDouble(4, v.getTotalHours());
                ps.executeUpdate();
            }
        } else if (item instanceof Beneficiary b) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO beneficiaries(user_id, household_size, needs) VALUES (?, ?, ?)")) {
                ps.setInt(1, b.getId());
                if (b.getHouseholdSize() > 0) {
                    ps.setInt(2, b.getHouseholdSize());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setString(3, b.getNeeds());
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO admins(user_id) VALUES (?)")) {
                ps.setInt(1, item.getId());
                ps.executeUpdate();
            }
        }
    }

    /** Updates in place: donations, tasks and deliveries reference the subtype row, so it must not be replaced. */
    private void updateSubtype(Connection c, User item) throws SQLException {
        if (item instanceof Donor d) {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE donors SET donor_type = ?, organization_name = ? WHERE user_id = ?")) {
                ps.setString(1, d.getDonorType());
                ps.setString(2, d.getOrganizationName());
                ps.setInt(3, d.getId());
                ps.executeUpdate();
            }
        } else if (item instanceof Volunteer v) {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE volunteers SET availability = ?, has_transport = ? WHERE user_id = ?")) {
                ps.setString(1, v.getAvailability());
                ps.setBoolean(2, v.hasTransport());
                ps.setInt(3, v.getId());
                ps.executeUpdate();
            }
        } else if (item instanceof Beneficiary b) {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE beneficiaries SET household_size = ?, needs = ? WHERE user_id = ?")) {
                if (b.getHouseholdSize() > 0) {
                    ps.setInt(1, b.getHouseholdSize());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                ps.setString(2, b.getNeeds());
                ps.setInt(3, b.getId());
                ps.executeUpdate();
            }
        }
    }

    private String roleOf(User user) {
        if (user instanceof Donor) {
            return "DONOR";
        }
        if (user instanceof Volunteer) {
            return "VOLUNTEER";
        }
        if (user instanceof Beneficiary) {
            return "BENEFICIARY";
        }
        return "ADMIN";
    }
}
