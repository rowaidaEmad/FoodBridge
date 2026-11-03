package foodbridge.repositories;

import java.util.List;

import foodbridge.models.donations.Receipt;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Receipt (connection from DatabaseManager).
 */
public class ReceiptRepository implements Repository<Receipt> {
    private DatabaseManager db;

    public ReceiptRepository() {
        // TODO: implement
    }

    @Override
    public void add(Receipt item) {
        // TODO: implement
    }

    @Override
    public Receipt findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Receipt> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Receipt item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
