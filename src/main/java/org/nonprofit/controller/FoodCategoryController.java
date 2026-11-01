package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.FoodCategoryService;
import org.nonprofit.model.FoodCategory;
public class FoodCategoryController {
    private final FoodCategoryService service;
    public FoodCategoryController(FoodCategoryService service) {this.service=service;}
    public long create(FoodCategory value) throws SQLException {return service.create(value);}
    public Optional<FoodCategory> get(long id) throws SQLException {return service.findById(id);}
    public List<FoodCategory> list() throws SQLException {return service.list();}
}

