package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.VolunteerService;
import org.nonprofit.model.Volunteer;
public class VolunteerController {
    private final VolunteerService service;
    public VolunteerController(VolunteerService service) {this.service=service;}
    public long create(Volunteer value) throws SQLException {return service.create(value);}
    public Optional<Volunteer> get(long id) throws SQLException {return service.findById(id);}
    public List<Volunteer> list() throws SQLException {return service.list();}
}

