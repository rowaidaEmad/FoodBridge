package foodbridge.services;

import java.util.Map;

import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.patterns.factory.DonationFactory;
import foodbridge.patterns.strategy.DonationProcessingStrategy;
import foodbridge.repositories.DonationRepository;

/**
 * STRATEGY - Context, and FACTORY METHOD client (uses 1..* DonationFactory).
 */
public class DonationService {
    private Map<String, DonationFactory> factories;
    private Map<String, DonationProcessingStrategy> strategies;
    private DonationRepository repository;

    public DonationService() {
        // TODO: implement
    }

    public int createDonation(String type, DonationDetails request) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public DonationProcessingResult processMoneyDonation(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
