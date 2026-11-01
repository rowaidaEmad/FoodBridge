package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.FoodCategory;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class FoodCategoryRepository {
    private final DatabaseManager db;
    public FoodCategoryRepository(DatabaseManager db) {this.db=db;}
    public long save(FoodCategory value) throws SQLException {throw new UnsupportedOperationException("TODO: FoodCategoryRepository.save JDBC");}
    public Optional<FoodCategory> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: FoodCategoryRepository.findById JDBC");}
    public List<FoodCategory> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: FoodCategoryRepository.findAll JDBC");}
}

