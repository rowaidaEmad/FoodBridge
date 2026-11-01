package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.UnitRepository;
import org.nonprofit.model.Unit;
public class UnitService {
    private final UnitRepository repository;
    public UnitService(UnitRepository repository) {this.repository=repository;}
    public long create(Unit value) throws SQLException {return repository.save(value);}
    public Optional<Unit> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Unit> list() throws SQLException {return repository.findAll();}
}

