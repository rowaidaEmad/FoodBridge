package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.EventType;
import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for EventRegistration (connection from DatabaseManager).
 * Table: event_registrations (one row per volunteer per event). SQL ported from FoodBridgeColdStart.
 */
public class EventRegistrationRepository implements Repository<EventRegistration> {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final String SELECT_REGISTRATIONS =
            "SELECT r.id, r.registered_at, r.attendance_status,"
            + " e.id AS event_id, e.title, e.event_date, e.location, e.status, e.event_type_id,"
            + " u.id AS volunteer_id, u.name AS volunteer_name"
            + " FROM event_registrations r"
            + " JOIN events e ON e.id = r.event_id"
            + " JOIN users u ON u.id = r.volunteer_id";

    private DatabaseManager db;

    public EventRegistrationRepository() {
        db = DatabaseManager.getInstance();
    }

    /** Fails with a clear message if the volunteer is already registered for the event. */
    @Override
    public void add(EventRegistration item) {
        String sql = "INSERT INTO event_registrations(event_id, volunteer_id, registered_at, attendance_status)"
                + " VALUES (?, ?, ?, ?) ON CONFLICT (event_id, volunteer_id) DO NOTHING RETURNING id";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, item.getEvent().getId());
            ps.setInt(2, item.getVolunteer().getId());
            ps.setObject(3, item.getRegisteredAt().atZone(ZONE).toOffsetDateTime());
            ps.setString(4, item.getAttendanceStatus());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("You are already registered for this event");
                }
                item.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save registration: " + e.getMessage(), e);
        }
    }

    @Override
    public EventRegistration findById(int id) {
        List<EventRegistration> found = query(" WHERE r.id = ?", id);
        return found.isEmpty() ? null : found.get(0);
    }

    /** Volunteers registered for one event (ColdStart's findById(eventId)). */
    public List<EventRegistration> findByEvent(int eventId) {
        return query(" WHERE r.event_id = ? ORDER BY r.registered_at", eventId);
    }

    public List<EventRegistration> findByVolunteer(int volunteerId) {
        return query(" WHERE r.volunteer_id = ? ORDER BY e.event_date", volunteerId);
    }

    @Override
    public List<EventRegistration> findAll() {
        return query(" ORDER BY r.registered_at", 0);
    }

    /** Only the attendance status can change. */
    @Override
    public void update(EventRegistration item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "UPDATE event_registrations SET attendance_status = ? WHERE id = ?")) {
            ps.setString(1, item.getAttendanceStatus());
            ps.setInt(2, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update registration: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM event_registrations WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete registration: " + e.getMessage(), e);
        }
    }

    private List<EventRegistration> query(String filter, int id) {
        List<EventRegistration> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_REGISTRATIONS + filter)) {
            if (filter.contains("?")) {
                ps.setInt(1, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load registrations: " + e.getMessage(), e);
        }
        return list;
    }

    private static EventRegistration map(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("event_id"));
        event.setTitle(rs.getString("title"));
        event.reschedule(rs.getObject("event_date", OffsetDateTime.class).atZoneSameInstant(ZONE).toLocalDateTime());
        event.setLocation(rs.getString("location"));
        event.setStatus(rs.getString("status"));
        event.setType(new EventType(rs.getInt("event_type_id"), null));

        Volunteer volunteer = new Volunteer();
        volunteer.setId(rs.getInt("volunteer_id"));
        volunteer.setName(rs.getString("volunteer_name"));

        EventRegistration registration = new EventRegistration();
        registration.setId(rs.getInt("id"));
        registration.setEvent(event);
        registration.setVolunteer(volunteer);
        registration.setRegisteredAt(rs.getObject("registered_at", OffsetDateTime.class)
                .atZoneSameInstant(ZONE).toLocalDateTime());
        registration.updateAttendance(rs.getString("attendance_status"));
        return registration;
    }
}
