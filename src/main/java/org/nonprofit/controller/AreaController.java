package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.AreaService;
import org.nonprofit.model.Area;
public class AreaController {
    private final AreaService service;
    public AreaController(AreaService service) {this.service=service;}
    public long create(Area value) throws SQLException {return service.create(value);}
    public Optional<Area> get(long id) throws SQLException {return service.findById(id);}
    public List<Area> list() throws SQLException {return service.list();}
}

