package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.EventType;
import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.models.operations.VolunteerHours;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for EventRegistration (connection from DatabaseManager).
 */
public class EventRegistrationRepository implements Repository<EventRegistration> {
    private static final String SELECT_REGISTRATIONS =
            "SELECT r.id, r.registered_at, r.attendance_status, r.volunteer_id, u.name AS volunteer_name,"
            + " e.id AS event_id, e.title, e.event_date, e.location, e.status, t.id AS type_id, t.name AS type_name"
            + " FROM event_registrations r"
            + " JOIN events e ON e.id = r.event_id"
            + " JOIN event_types t ON t.id = e.event_type_id"
            + " JOIN users u ON u.id = r.volunteer_id";

    private final DatabaseManager db;

    public EventRegistrationRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(EventRegistration item) {
        String sql = "INSERT INTO event_registrations(event_id, volunteer_id, attendance_status) VALUES (?, ?, ?)";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getEvent().getId());
            ps.setInt(2, item.getVolunteer().getId());
            ps.setString(3, item.getAttendanceStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save registration: " + e.getMessage(), e);
        }
    }

    @Override
    public EventRegistration findById(int id) {
        List<EventRegistration> registrations = query(" WHERE r.id = ?", id);
        return registrations.isEmpty() ? null : registrations.get(0);
    }

    @Override
    public List<EventRegistration> findAll() {
        return query(" ORDER BY r.id");
    }

    public List<EventRegistration> findByEvent(int eventId) {
        return query(" WHERE r.event_id = ? ORDER BY u.name", eventId);
    }

    public List<EventRegistration> findByVolunteer(int volunteerId) {
        return query(" WHERE r.volunteer_id = ? ORDER BY e.event_date", volunteerId);
    }

    public boolean exists(int eventId, int volunteerId) {
        return !query(" WHERE r.event_id = ? AND r.volunteer_id = ?", eventId, volunteerId).isEmpty();
    }

    @Override
    public void update(EventRegistration item) {
        try (Connection c = db.getConnection()) {
            saveAttendance(c, item);
        } catch (SQLException e) {
            throw new RuntimeException("Could not update registration: " + e.getMessage(), e);
        }
    }

    /** Saves the attendance and credits the volunteer's hours together. */
    public void saveAttended(EventRegistration item, double hours) {
        JdbcSupport.inTransaction(db, "Could not save attendance", c -> {
            saveAttendance(c, item);
            VolunteerRepository.recordHours(c, item.getVolunteer().getId(), hours, VolunteerHours.EVENT,
                    item.getEvent().getTitle());
        });
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM event_registrations WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No registration with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete registration: " + e.getMessage(), e);
        }
    }

    private void saveAttendance(Connection c, EventRegistration item) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE event_registrations SET attendance_status = ? WHERE id = ?")) {
            ps.setString(1, item.getAttendanceStatus());
            ps.setInt(2, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No registration with id " + item.getId());
            }
        }
    }

    private List<EventRegistration> query(String clause, int... params) {
        List<EventRegistration> registrations = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_REGISTRATIONS + clause)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load registrations: " + e.getMessage(), e);
        }
        return registrations;
    }

    private EventRegistration map(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("event_id"));
        event.setTitle(rs.getString("title"));
        event.setDate(JdbcSupport.getDateTime(rs, "event_date"));
        event.setLocation(rs.getString("location"));
        event.setStatus(rs.getString("status"));
        event.setType(new EventType(rs.getInt("type_id"), rs.getString("type_name")));

        Volunteer volunteer = new Volunteer();
        volunteer.setId(rs.getInt("volunteer_id"));
        volunteer.setName(rs.getString("volunteer_name"));

        EventRegistration registration = new EventRegistration(event, volunteer);
        registration.setId(rs.getInt("id"));
        registration.setRegisteredAt(JdbcSupport.getDateTime(rs, "registered_at"));
        registration.updateAttendance(rs.getString("attendance_status"));
        return registration;
    }
}
