package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.AreaRepository;
import org.nonprofit.model.Area;
public class AreaService {
    private final AreaRepository repository;
    public AreaService(AreaRepository repository) {this.repository=repository;}
    public long create(Area value) throws SQLException {return repository.save(value);}
    public Optional<Area> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Area> list() throws SQLException {return repository.findAll();}
}

