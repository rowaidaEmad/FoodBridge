package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.DonationRequestRepository;
import org.nonprofit.model.DonationRequest;
public class DonationRequestService {
    private final DonationRequestRepository repository;
    public DonationRequestService(DonationRequestRepository repository) {this.repository=repository;}
    public long create(DonationRequest value) throws SQLException {return repository.save(value);}
    public Optional<DonationRequest> findById(long id) throws SQLException {return repository.findById(id);}
    public List<DonationRequest> list() throws SQLException {return repository.findAll();}
}

