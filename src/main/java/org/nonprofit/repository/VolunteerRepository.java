package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Volunteer;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class VolunteerRepository {
    private final DatabaseManager db;
    public VolunteerRepository(DatabaseManager db) {this.db=db;}
    public long save(Volunteer value) throws SQLException {throw new UnsupportedOperationException("TODO: VolunteerRepository.save JDBC");}
    public Optional<Volunteer> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: VolunteerRepository.findById JDBC");}
    public List<Volunteer> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: VolunteerRepository.findAll JDBC");}
}

