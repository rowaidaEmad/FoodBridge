package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.DonationService;
import org.nonprofit.model.Donation;
public class DonationController {
    private final DonationService service;
    public DonationController(DonationService service) {this.service=service;}
    public long create(Donation value) throws SQLException {return service.create(value);}
    public Optional<Donation> get(long id) throws SQLException {return service.findById(id);}
    public List<Donation> list() throws SQLException {return service.list();}
}

