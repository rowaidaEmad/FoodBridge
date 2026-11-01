package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.DonationRequestService;
import org.nonprofit.model.DonationRequest;
public class DonationRequestController {
    private final DonationRequestService service;
    public DonationRequestController(DonationRequestService service) {this.service=service;}
    public long create(DonationRequest value) throws SQLException {return service.create(value);}
    public Optional<DonationRequest> get(long id) throws SQLException {return service.findById(id);}
    public List<DonationRequest> list() throws SQLException {return service.list();}
}

