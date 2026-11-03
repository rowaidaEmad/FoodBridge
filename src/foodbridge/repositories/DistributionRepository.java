package foodbridge.repositories;

import java.util.List;

import foodbridge.models.operations.Distribution;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Distribution (connection from DatabaseManager).
 */
public class DistributionRepository implements Repository<Distribution> {
    private DatabaseManager db;

    public DistributionRepository() {
        // TODO: implement
    }

    @Override
    public void add(Distribution item) {
        // TODO: implement
    }

    @Override
    public Distribution findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Distribution> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Distribution item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
