package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.DonationRequest;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class DonationRequestRepository {
    private final DatabaseManager db;
    public DonationRequestRepository(DatabaseManager db) {this.db=db;}
    public long save(DonationRequest value) throws SQLException {throw new UnsupportedOperationException("TODO: DonationRequestRepository.save JDBC");}
    public Optional<DonationRequest> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: DonationRequestRepository.findById JDBC");}
    public List<DonationRequest> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: DonationRequestRepository.findAll JDBC");}
}

