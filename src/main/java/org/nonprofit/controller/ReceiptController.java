package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.ReceiptService;
import org.nonprofit.model.Receipt;
public class ReceiptController {
    private final ReceiptService service;
    public ReceiptController(ReceiptService service) {this.service=service;}
    public long create(Receipt value) throws SQLException {return service.create(value);}
    public Optional<Receipt> get(long id) throws SQLException {return service.findById(id);}
    public List<Receipt> list() throws SQLException {return service.list();}
}

