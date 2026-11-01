package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.PickupTask;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class PickupTaskRepository {
    private final DatabaseManager db;
    public PickupTaskRepository(DatabaseManager db) {this.db=db;}
    public long save(PickupTask value) throws SQLException {throw new UnsupportedOperationException("TODO: PickupTaskRepository.save JDBC");}
    public Optional<PickupTask> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: PickupTaskRepository.findById JDBC");}
    public List<PickupTask> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: PickupTaskRepository.findAll JDBC");}
}

