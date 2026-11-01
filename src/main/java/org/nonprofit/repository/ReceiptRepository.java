package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Receipt;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class ReceiptRepository {
    private final DatabaseManager db;
    public ReceiptRepository(DatabaseManager db) {this.db=db;}
    public long save(Receipt value) throws SQLException {throw new UnsupportedOperationException("TODO: ReceiptRepository.save JDBC");}
    public Optional<Receipt> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: ReceiptRepository.findById JDBC");}
    public List<Receipt> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: ReceiptRepository.findAll JDBC");}
}

