package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.UnitService;
import org.nonprofit.model.Unit;
public class UnitController {
    private final UnitService service;
    public UnitController(UnitService service) {this.service=service;}
    public long create(Unit value) throws SQLException {return service.create(value);}
    public Optional<Unit> get(long id) throws SQLException {return service.findById(id);}
    public List<Unit> list() throws SQLException {return service.list();}
}

