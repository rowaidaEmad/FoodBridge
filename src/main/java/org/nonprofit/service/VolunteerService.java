package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.VolunteerRepository;
import org.nonprofit.model.Volunteer;
public class VolunteerService {
    private final VolunteerRepository repository;
    public VolunteerService(VolunteerRepository repository) {this.repository=repository;}
    public long create(Volunteer value) throws SQLException {return repository.save(value);}
    public Optional<Volunteer> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Volunteer> list() throws SQLException {return repository.findAll();}
}

