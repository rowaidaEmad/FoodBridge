package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.PickupTaskService;
import org.nonprofit.model.PickupTask;
public class PickupTaskController {
    private final PickupTaskService service;
    public PickupTaskController(PickupTaskService service) {this.service=service;}
    public long create(PickupTask value) throws SQLException {return service.create(value);}
    public Optional<PickupTask> get(long id) throws SQLException {return service.findById(id);}
    public List<PickupTask> list() throws SQLException {return service.list();}
}

