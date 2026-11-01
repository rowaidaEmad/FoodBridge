package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.ReceiptRepository;
import org.nonprofit.model.Receipt;
public class ReceiptService {
    private final ReceiptRepository repository;
    public ReceiptService(ReceiptRepository repository) {this.repository=repository;}
    public long create(Receipt value) throws SQLException {return repository.save(value);}
    public Optional<Receipt> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Receipt> list() throws SQLException {return repository.findAll();}
}

