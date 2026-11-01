package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Area;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class AreaRepository {
    private final DatabaseManager db;
    public AreaRepository(DatabaseManager db) {this.db=db;}
    public long save(Area value) throws SQLException {throw new UnsupportedOperationException("TODO: AreaRepository.save JDBC");}
    public Optional<Area> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: AreaRepository.findById JDBC");}
    public List<Area> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: AreaRepository.findAll JDBC");}
}

