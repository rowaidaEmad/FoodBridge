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
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Event (connection from DatabaseManager).
 * Table: events (+ event_types for the type name). SQL ported from the FoodBridgeColdStart branch.
 */
public class EventRepository implements Repository<Event> {
    private static final ZoneId EVENT_TIME_ZONE = ZoneId.systemDefault();
    private static final String SELECT_EVENTS =
            "SELECT e.id, e.title, e.description, e.event_date, e.location, e.status,"
            + " t.id AS type_id, t.name AS type_name"
            + " FROM events e JOIN event_types t ON t.id = e.event_type_id";

    private DatabaseManager db;

    public EventRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Event item) {
        String sql = "INSERT INTO events(title, description, event_date, location, event_type_id)"
                + " VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, item);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No id returned");
                }
                item.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save event: " + e.getMessage(), e);
        }
    }

    @Override
    public Event findById(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_EVENTS + " WHERE e.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load event: " + e.getMessage(), e);
        }
    }

    /** All events, soonest first. */
    @Override
    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_EVENTS + " ORDER BY e.event_date");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list events: " + e.getMessage(), e);
        }
        return list;
    }

    /** Only scheduled events can change; throws if the event is cancelled or missing. */
    @Override
    public void update(Event item) {
        String sql = "UPDATE events SET title = ?, description = ?, event_date = ?, location = ?, event_type_id = ?"
                + " WHERE id = ? AND status = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, item);
            ps.setInt(6, item.getId());
            ps.setString(7, Event.SCHEDULED);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Only scheduled events can be updated");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update event: " + e.getMessage(), e);
        }
    }

    /** Returns false if the event is missing or already cancelled. */
    public boolean cancel(int eventId) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE events SET status = ? WHERE id = ? AND status = ?")) {
            ps.setString(1, Event.CANCELLED);
            ps.setInt(2, eventId);
            ps.setString(3, Event.SCHEDULED);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not cancel event: " + e.getMessage(), e);
        }
    }

    /** Registrations are removed by ON DELETE CASCADE. */
    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM events WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No event with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete event: " + e.getMessage(), e);
        }
    }

    private static void fill(PreparedStatement ps, Event item) throws SQLException {
        ps.setString(1, item.getTitle());
        ps.setString(2, item.getDescription());
        ps.setObject(3, item.getDate().atZone(EVENT_TIME_ZONE).toOffsetDateTime());
        ps.setString(4, item.getLocation());
        ps.setInt(5, item.getType().getId());
    }

    private static Event map(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.reschedule(rs.getObject("event_date", OffsetDateTime.class)
                .atZoneSameInstant(EVENT_TIME_ZONE).toLocalDateTime());
        event.setLocation(rs.getString("location"));
        event.setType(new EventType(rs.getInt("type_id"), rs.getString("type_name")));
        event.setStatus(rs.getString("status"));
        return event;
    }
}
