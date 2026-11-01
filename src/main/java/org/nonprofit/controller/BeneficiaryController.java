package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.BeneficiaryService;
import org.nonprofit.model.Beneficiary;
public class BeneficiaryController {
    private final BeneficiaryService service;
    public BeneficiaryController(BeneficiaryService service) {this.service=service;}
    public long create(Beneficiary value) throws SQLException {return service.create(value);}
    public Optional<Beneficiary> get(long id) throws SQLException {return service.findById(id);}
    public List<Beneficiary> list() throws SQLException {return service.list();}
}

