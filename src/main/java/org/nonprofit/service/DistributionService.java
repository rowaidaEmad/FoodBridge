package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.DistributionRepository;
import org.nonprofit.model.Distribution;
public class DistributionService {
    private final DistributionRepository repository;
    public DistributionService(DistributionRepository repository) {this.repository=repository;}
    public long create(Distribution value) throws SQLException {return repository.save(value);}
    public Optional<Distribution> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Distribution> list() throws SQLException {return repository.findAll();}
}

