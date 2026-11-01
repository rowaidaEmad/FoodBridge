package org.nonprofit.repository;


import java.sql.*;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.EventType;
public class EventTypeRepository {
    private final DatabaseManager db;
    public EventTypeRepository(DatabaseManager db) { this.db = db; }
    public long create(String name) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO event_types(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()) { if(rs.next()) return rs.getLong(1); }
            throw new SQLException("No generated ID returned");
        }
    }
    public List<EventType> findAll() throws SQLException {
        List<EventType> list = new ArrayList<>();
        try(Connection c=db.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT id,name FROM event_types ORDER BY id"); ResultSet rs=ps.executeQuery()) {
            while(rs.next()) { EventType x=new EventType(); x.setId(rs.getLong("id")); x.setName(rs.getString("name")); list.add(x); }
        }
        return list;
    }
    public boolean update(long id,String name) throws SQLException {
        try(Connection c=db.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE event_types SET name=? WHERE id=?")) { ps.setString(1,name); ps.setLong(2,id); return ps.executeUpdate()>0; }
    }
    public boolean delete(long id) throws SQLException {
        try(Connection c=db.getConnection(); PreparedStatement ps=c.prepareStatement("DELETE FROM event_types WHERE id=?")) { ps.setLong(1,id); return ps.executeUpdate()>0; }
    }
}

