package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.EventRegistration;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class EventRegistrationRepository {
    private final DatabaseManager db;
    public EventRegistrationRepository(DatabaseManager db) {this.db=db;}
    public long save(EventRegistration value) throws SQLException {throw new UnsupportedOperationException("TODO: EventRegistrationRepository.save JDBC");}
    public Optional<EventRegistration> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: EventRegistrationRepository.findById JDBC");}
    public List<EventRegistration> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: EventRegistrationRepository.findAll JDBC");}
}

