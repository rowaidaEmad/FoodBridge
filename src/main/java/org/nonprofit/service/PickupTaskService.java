package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.PickupTaskRepository;
import org.nonprofit.model.PickupTask;
public class PickupTaskService {
    private final PickupTaskRepository repository;
    public PickupTaskService(PickupTaskRepository repository) {this.repository=repository;}
    public long create(PickupTask value) throws SQLException {return repository.save(value);}
    public Optional<PickupTask> findById(long id) throws SQLException {return repository.findById(id);}
    public List<PickupTask> list() throws SQLException {return repository.findAll();}
}

