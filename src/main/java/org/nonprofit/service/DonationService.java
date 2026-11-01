package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.DonationRepository;
import org.nonprofit.model.Donation;
public class DonationService {
    private final DonationRepository repository;
    public DonationService(DonationRepository repository) {this.repository=repository;}
    //crud
    public long create(Donation value) throws SQLException {return repository.save(value);}
    public Optional<Donation> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Donation> list() throws SQLException {return repository.findAll();}


    private final org.nonprofit.patterns.factory.DonationFactory factory = new org.nonprofit.patterns.factory.DonationFactory();
    public org.nonprofit.model.MoneyDonation prepareMoneyDonation(long donorId,java.math.BigDecimal amount,String method) {
        return factory.createMoneyDonation(donorId,amount,method);
    }
}

