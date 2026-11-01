package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.BeneficiaryRepository;
import org.nonprofit.model.Beneficiary;
public class BeneficiaryService {
    private final BeneficiaryRepository repository;
    public BeneficiaryService(BeneficiaryRepository repository) {this.repository=repository;}
    public long create(Beneficiary value) throws SQLException {return repository.save(value);}
    public Optional<Beneficiary> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Beneficiary> list() throws SQLException {return repository.findAll();}
}

