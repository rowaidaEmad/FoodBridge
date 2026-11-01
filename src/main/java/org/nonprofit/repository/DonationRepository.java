package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Donation;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class DonationRepository {
    private final DatabaseManager db;
    public DonationRepository(DatabaseManager db) {this.db=db;}
    public long save(Donation value) throws SQLException {throw new UnsupportedOperationException("TODO: DonationRepository.save JDBC");}
    public Optional<Donation> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: DonationRepository.findById JDBC");}
    public List<Donation> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: DonationRepository.findAll JDBC");}
}

