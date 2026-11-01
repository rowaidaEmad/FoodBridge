package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Beneficiary;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class BeneficiaryRepository {
    private final DatabaseManager db;
    public BeneficiaryRepository(DatabaseManager db) {this.db=db;}
    public long save(Beneficiary value) throws SQLException {throw new UnsupportedOperationException("TODO: BeneficiaryRepository.save JDBC");}
    public Optional<Beneficiary> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: BeneficiaryRepository.findById JDBC");}
    public List<Beneficiary> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: BeneficiaryRepository.findAll JDBC");}
}

