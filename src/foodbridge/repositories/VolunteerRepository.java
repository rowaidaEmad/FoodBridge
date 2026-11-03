package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.operations.VolunteerHours;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Volunteer profiles and hours.
 * Tables: users, volunteers, volunteer_skills, volunteer_preferences, volunteer_hours.
 */
public class VolunteerRepository {
    private static final String SELECT_VOLUNTEERS =
            "SELECT u.id, u.name, u.email, u.phone, u.address, u.is_active,"
            + " v.availability, v.has_transport, v.total_hours"
            + " FROM users u JOIN volunteers v ON v.user_id = u.id";
    private static final String SELECT_SKILLS =
            "SELECT skill FROM volunteer_skills WHERE volunteer_id = ? ORDER BY skill";
    private static final String SELECT_PREFERENCES =
            "SELECT preference FROM volunteer_preferences WHERE volunteer_id = ? ORDER BY preference";

    private final DatabaseManager db;

    public VolunteerRepository() {
        db = DatabaseManager.getInstance();
    }

    public Volunteer findById(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_VOLUNTEERS + " WHERE u.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWithDetails(c, rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load volunteer: " + e.getMessage(), e);
        }
    }

    public List<Volunteer> findAllActive() {
        List<Volunteer> volunteers = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_VOLUNTEERS + " WHERE u.is_active ORDER BY u.id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                volunteers.add(mapWithDetails(c, rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list volunteers: " + e.getMessage(), e);
        }
        return volunteers;
    }

    public void updateProfile(int id, String availability, boolean hasTransport, List<String> skills,
            List<String> preferences) {
        JdbcSupport.inTransaction(db, "Could not update profile", c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE volunteers SET availability = ?, has_transport = ? WHERE user_id = ?")) {
                ps.setString(1, availability);
                ps.setBoolean(2, hasTransport);
                ps.setInt(3, id);
                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("No volunteer with id " + id);
                }
            }
            replaceValues(c, "DELETE FROM volunteer_skills WHERE volunteer_id = ?",
                    "INSERT INTO volunteer_skills(volunteer_id, skill) VALUES (?, ?)", id, skills);
            replaceValues(c, "DELETE FROM volunteer_preferences WHERE volunteer_id = ?",
                    "INSERT INTO volunteer_preferences(volunteer_id, preference) VALUES (?, ?)", id, preferences);
        });
    }

    public void addHours(int volunteerId, double hours, String source, String note) {
        JdbcSupport.inTransaction(db, "Could not log hours", c -> recordHours(c, volunteerId, hours, source, note));
    }

    public List<VolunteerHours> findHours(int volunteerId) {
        String sql = "SELECT id, volunteer_id, hours, source, note, logged_at FROM volunteer_hours"
                + " WHERE volunteer_id = ? ORDER BY logged_at DESC, id DESC";
        List<VolunteerHours> entries = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, volunteerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapHours(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load hours: " + e.getMessage(), e);
        }
        return entries;
    }

    static void recordHours(Connection c, int volunteerId, double hours, String source, String note)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO volunteer_hours(volunteer_id, hours, source, note) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, volunteerId);
            ps.setDouble(2, hours);
            ps.setString(3, source);
            ps.setString(4, note);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE volunteers SET total_hours = total_hours + ? WHERE user_id = ?")) {
            ps.setDouble(1, hours);
            ps.setInt(2, volunteerId);
            ps.executeUpdate();
        }
    }

    private Volunteer mapWithDetails(Connection c, ResultSet rs) throws SQLException {
        Volunteer volunteer = new Volunteer();
        volunteer.setId(rs.getInt("id"));
        volunteer.setName(rs.getString("name"));
        volunteer.changeEmail(rs.getString("email"));
        volunteer.updateContactInfo(rs.getString("phone"), rs.getString("address"));
        volunteer.setActive(rs.getBoolean("is_active"));
        volunteer.setAvailability(rs.getString("availability"));
        volunteer.setHasTransport(rs.getBoolean("has_transport"));
        volunteer.addHours(rs.getDouble("total_hours"));
        volunteer.setSkills(loadValues(c, SELECT_SKILLS, volunteer.getId()));
        volunteer.setPreferences(loadValues(c, SELECT_PREFERENCES, volunteer.getId()));
        return volunteer;
    }

    private VolunteerHours mapHours(ResultSet rs) throws SQLException {
        VolunteerHours entry = new VolunteerHours();
        entry.setId(rs.getInt("id"));
        entry.setVolunteerId(rs.getInt("volunteer_id"));
        entry.setHours(rs.getDouble("hours"));
        entry.setSource(rs.getString("source"));
        entry.setNote(rs.getString("note"));
        entry.setLoggedAt(JdbcSupport.getDateTime(rs, "logged_at"));
        return entry;
    }

    private List<String> loadValues(Connection c, String sql, int volunteerId) throws SQLException {
        List<String> values = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, volunteerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getString(1));
                }
            }
        }
        return values;
    }

    private void replaceValues(Connection c, String deleteSql, String insertSql, int volunteerId, List<String> values)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(deleteSql)) {
            ps.setInt(1, volunteerId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(insertSql)) {
            for (String value : values) {
                ps.setInt(1, volunteerId);
                ps.setString(2, value);
                ps.executeUpdate();
            }
        }
    }
}
