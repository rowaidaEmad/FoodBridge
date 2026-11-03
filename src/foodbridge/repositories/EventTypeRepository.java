package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.EventType;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for EventType (connection from DatabaseManager).
 * Table: event_types (see database/schema.sql).
 */
public class EventTypeRepository implements Repository<EventType> {
    private DatabaseManager db;

    public EventTypeRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(EventType item) {
        String sql = "INSERT INTO event_types(name) VALUES (?)";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not add event type: " + e.getMessage(), e);
        }
    }

    @Override
    public EventType findById(int id) {
        String sql = "SELECT id, name FROM event_types WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new EventType(rs.getInt("id"), rs.getString("name")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load event type: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EventType> findAll() {
        List<EventType> list = new ArrayList<>();
        String sql = "SELECT id, name FROM event_types ORDER BY id";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new EventType(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list event types: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(EventType item) {
        String sql = "UPDATE event_types SET name = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No event type with id " + item.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update event type: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM event_types WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No event type with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete event type: " + e.getMessage(), e);
        }
    }
}
