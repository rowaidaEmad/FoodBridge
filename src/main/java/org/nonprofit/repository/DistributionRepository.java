package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Distribution;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class DistributionRepository {
    private final DatabaseManager db;
    public DistributionRepository(DatabaseManager db) {this.db=db;}
    public long save(Distribution value) throws SQLException {throw new UnsupportedOperationException("TODO: DistributionRepository.save JDBC");}
    public Optional<Distribution> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: DistributionRepository.findById JDBC");}
    public List<Distribution> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: DistributionRepository.findAll JDBC");}
}

