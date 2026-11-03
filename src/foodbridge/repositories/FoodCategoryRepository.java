package foodbridge.repositories;

import java.util.List;

import foodbridge.models.lookups.FoodCategory;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for FoodCategory (connection from DatabaseManager).
 */
public class FoodCategoryRepository implements Repository<FoodCategory> {
    private DatabaseManager db;

    public FoodCategoryRepository() {
        // TODO: implement
    }

    @Override
    public void add(FoodCategory item) {
        // TODO: implement
    }

    @Override
    public FoodCategory findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<FoodCategory> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(FoodCategory item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
