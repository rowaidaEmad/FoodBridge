package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Unit;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class UnitRepository {
    private final DatabaseManager db;
    public UnitRepository(DatabaseManager db) {this.db=db;}
    public long save(Unit value) throws SQLException {throw new UnsupportedOperationException("TODO: UnitRepository.save JDBC");}
    public Optional<Unit> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: UnitRepository.findById JDBC");}
    public List<Unit> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: UnitRepository.findAll JDBC");}
}

