package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Event;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class EventRepository {
    private final DatabaseManager db;
    public EventRepository(DatabaseManager db) {this.db=db;}
    public long save(Event value) throws SQLException {throw new UnsupportedOperationException("TODO: EventRepository.save JDBC");}
    public Optional<Event> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: EventRepository.findById JDBC");}
    public List<Event> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: EventRepository.findAll JDBC");}
}

