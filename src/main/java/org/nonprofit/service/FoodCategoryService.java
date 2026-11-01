package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.FoodCategoryRepository;
import org.nonprofit.model.FoodCategory;
public class FoodCategoryService {
    private final FoodCategoryRepository repository;
    public FoodCategoryService(FoodCategoryRepository repository) {this.repository=repository;}
    public long create(FoodCategory value) throws SQLException {return repository.save(value);}
    public Optional<FoodCategory> findById(long id) throws SQLException {return repository.findById(id);}
    public List<FoodCategory> list() throws SQLException {return repository.findAll();}
}

