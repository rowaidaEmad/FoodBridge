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
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Event (connection from DatabaseManager).
 */
public class EventRepository implements Repository<Event> {
    private static final String SELECT_EVENTS =
            "SELECT e.id, e.title, e.description, e.event_date, e.location, e.required_skill,"
            + " t.id AS type_id, t.name AS type_name"
            + " FROM events e JOIN event_types t ON t.id = e.event_type_id";

    private final DatabaseManager db;

    public EventRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Event item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO events(title, description, event_date, location, event_type_id, required_skill)"
                        + " VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, item);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                item.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not add event: " + e.getMessage(), e);
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

    @Override
    public List<Event> findAll() {
        List<Event> events = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_EVENTS + " ORDER BY e.event_date");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list events: " + e.getMessage(), e);
        }
        return events;
    }

    @Override
    public void update(Event item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "UPDATE events SET title = ?, description = ?, event_date = ?, location = ?,"
                        + " event_type_id = ?, required_skill = ? WHERE id = ?")) {
            bind(ps, item);
            ps.setInt(7, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No event with id " + item.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update event: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        // Registrations are removed by ON DELETE CASCADE.
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

    private void bind(PreparedStatement ps, Event item) throws SQLException {
        ps.setString(1, item.getTitle());
        ps.setString(2, item.getDescription());
        ps.setTimestamp(3, JdbcSupport.toTimestamp(item.getDate()));
        ps.setString(4, item.getLocation());
        ps.setInt(5, item.getType().getId());
        ps.setString(6, item.getRequiredSkill());
    }

    private Event map(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setDate(JdbcSupport.getDateTime(rs, "event_date"));
        event.setLocation(rs.getString("location"));
        event.setRequiredSkill(rs.getString("required_skill"));
        event.setType(new EventType(rs.getInt("type_id"), rs.getString("type_name")));
        return event;
    }
}
