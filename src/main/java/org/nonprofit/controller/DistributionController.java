package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.DistributionService;
import org.nonprofit.model.Distribution;
public class DistributionController {
    private final DistributionService service;
    public DistributionController(DistributionService service) {this.service=service;}
    public long create(Distribution value) throws SQLException {return service.create(value);}
    public Optional<Distribution> get(long id) throws SQLException {return service.findById(id);}
    public List<Distribution> list() throws SQLException {return service.list();}
}

