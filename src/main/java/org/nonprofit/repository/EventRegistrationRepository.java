package org.nonprofit.repository;


import java.net.ConnectException;
import java.sql.*;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.EventRegistration;
public class EventRegistrationRepository {

    private final DatabaseManager db;

    public EventRegistrationRepository(DatabaseManager db) {
        this.db=db;
    }

    public long save(EventRegistration value) throws SQLException {

        //prepare the query
        String sql = "INSERT INTO event_registrations (event_id, volunteer_id, registered_at, attendance_status) " +
                        " VALUES (?, ?, ?, ?) RETURNING id ";

        //connect and execute the query
        try(Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){

            //set quey values via stmt.set
            stmt.setLong(1,value.getEventId());
            stmt.setLong(2,value.getVolunteerId());
            stmt.setTimestamp(3,Timestamp.valueOf(value.getRegisteredAt()) );
            stmt.setString(4, value.getAttendanceStatus());

            try(ResultSet res = stmt.executeQuery()){
                if(res.next()){
                    return res.getLong("id");
                }
                throw new SQLException("failed to save registiration");
            }
        }
    }

    //
    public List<EventRegistration> findById(long eventId) throws SQLException {

        //prepare the query
        String sql = "SELECT id, event_id, volunteer_id, "+
        "registered_at, attendance_status "+
        "FROM event_registrations "+
        "WHERE event_id = ? "+
        "ORDER BY registered_at ";

        //fetch the resul in this list
        List<EventRegistration> registrations = new ArrayList<>();

        try (Connection connection = db.getConnection();PreparedStatement statement = connection.prepareStatement(sql)) {

            //set the id to event_id
            statement.setLong(1, eventId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    //registration for one entry into regs list
                    EventRegistration registration = new EventRegistration();

                    //fill it's data
                    registration.setId(rs.getLong("id"));
                    registration.setEventId(rs.getLong("event_id"));
                    registration.setVolunteerId(rs.getLong("volunteer_id"));
                    registration.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
                    registration.setAttendanceStatus(rs.getString("attendance_status"));

                    //push to regs list
                    registrations.add(registration);
                }
            }
        }

        return registrations;
    }

    public List<EventRegistration> findAll() throws SQLException {

        //prepare the query
        String sql = "SELECT id, event_id, volunteer_id,"+
                "registered_at, attendance_status "+
                "FROM event_registrations "+
                "ORDER BY registered_at";

        //fetch the resul in this list
        List<EventRegistration> registrations = new ArrayList<>();

        try (Connection connection = db.getConnection();PreparedStatement statement = connection.prepareStatement(sql)) {

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    //registration for one entry into regs list
                    EventRegistration registration = new EventRegistration();

                    //fill it's data
                    registration.setId(rs.getLong("id"));
                    registration.setEventId(rs.getLong("event_id"));
                    registration.setVolunteerId(rs.getLong("volunteer_id"));
                    registration.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
                    registration.setAttendanceStatus(rs.getString("attendance_status"));

                    //push to regs list
                    registrations.add(registration);
                }
            }
        }

        return registrations;
    }
}

