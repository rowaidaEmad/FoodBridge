package org.nonprofit.repository;

import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Event;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventRepository {

    private final DatabaseManager db;

    private static final ZoneId EVENT_TIME_ZONE =  ZoneId.systemDefault();

    public EventRepository(DatabaseManager db) {
        this.db = db;
    }
    public long save(Event value) throws SQLException {

        String sql = """
                INSERT INTO events
                    (title, description, event_date,
                     location, event_type_id)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt =  conn.prepareStatement(sql)) {

            stmt.setString(1, value.getTitle());
            stmt.setString(2, value.getDescription());

            stmt.setObject(3,
                    value.getDate().atZone(EVENT_TIME_ZONE).toOffsetDateTime()
            );

            stmt.setString(4, value.getLocation());
            stmt.setLong(5, value.getEventTypeId());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    long id = rs.getLong("id");
                    value.setId(id);
                    return id;
                }

                throw new SQLException(
                        "Failed to save event: no ID returned."
                );
            }
        }
    }


    public Optional<Event> findById(long id)
            throws SQLException {

        String sql = """
                SELECT id, title, description,
                       event_date, location,
                       event_type_id, status
                FROM events
                WHERE id = ?
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    Event event = mapRow(rs);
                    return Optional.of(event);
                }
            }
        }

        return Optional.empty();
    }

    public List<Event> findAll() throws SQLException {

        String sql = """
                SELECT id, title, description,
                       event_date, location,
                       event_type_id, status
                FROM events
                ORDER BY event_date
                """;

        List<Event> events = new ArrayList<>();

        try (Connection conn = db.getConnection();

             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Event event = mapRow(rs);
                events.add(event);
            }
        }

        return events;
    }


    public boolean update(Event event) throws SQLException {

        String sql = """
            UPDATE events
            SET title = ?,
                description = ?,
                event_date = ?,
                location = ?,
                event_type_id = ?
            WHERE id = ?
              AND status = 'SCHEDULED'
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setObject(3, event.getDate());
            stmt.setString(4, event.getLocation());
            stmt.setLong(5, event.getEventTypeId());
            stmt.setLong(6, event.getId());

            return stmt.executeUpdate() > 0;
        }
    }


    public boolean cancel(long eventId) throws SQLException {

        String sql = """
            UPDATE events
            SET status = 'CANCELLED'
            WHERE id = ?
              AND status = 'SCHEDULED'
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, eventId);

            return stmt.executeUpdate() > 0;
        }
    }


    public boolean delete(long eventId)
            throws SQLException {

        String sql = """
                DELETE FROM events
                WHERE id = ?
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, eventId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }


    private Event mapRow(ResultSet rs) throws SQLException {

        Event event = new Event();

        event.setId(rs.getLong("id"));
        event.setTitle(rs.getString("title"));

        event.setDescription(rs.getString("description"));

        OffsetDateTime date = rs.getObject("event_date", OffsetDateTime.class);
        event.setDate(date.atZoneSameInstant(EVENT_TIME_ZONE).toLocalDateTime());
        event.setLocation(rs.getString("location"));
        event.setEventTypeId(rs.getLong("event_type_id"));
        event.setStatus(rs.getString("status"));

        return event;
    }
}