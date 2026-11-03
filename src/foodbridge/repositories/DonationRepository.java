package foodbridge.repositories;

import java.util.List;

import foodbridge.models.donations.Donation;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Donation (connection from DatabaseManager).
 */
public class DonationRepository implements Repository<Donation> {
    private DatabaseManager db;

    public DonationRepository() {
        // TODO: implement
    }

    @Override
    public void add(Donation item) {
        // TODO: implement
    }

    @Override
    public Donation findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Donation> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Donation item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
