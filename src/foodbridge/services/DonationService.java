package foodbridge.services;

import java.util.HashMap;
import java.util.Map;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.patterns.factory.DonationFactory;
import foodbridge.patterns.strategy.DonationProcessingStrategy;
import foodbridge.repositories.DonationRepository;

// Strategy pattern - Context
/**
 * STRATEGY - Context, and FACTORY METHOD client (uses 1..* DonationFactory).
 * Holds one DonationProcessingStrategy per payment method and delegates payment to it;
 * it never contains cash / check / online logic itself.
 */
public class DonationService {
    // TODO (Factory step): fill with MoneyDonationFactory / FoodDonationFactory.
    private Map<String, DonationFactory> factories;
    private final Map<String, DonationProcessingStrategy> strategies = new HashMap<>();
    private final DonationRepository donationRepository;

    public DonationService() {
        this(new DonationRepository());
    }

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    /** Adds or replaces the strategy for a payment method ("cash", "check", "online"). */
    public void registerStrategy(String method, DonationProcessingStrategy strategy) {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy is required");
        }
        strategies.put(method.trim().toLowerCase(), strategy);
    }

    // TODO (Factory step): create the donation through the matching DonationFactory.
    public int createDonation(String type, DonationDetails request) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public DonationProcessingResult processMoneyDonation(int id) {
        Donation found = donationRepository.findById(id);
        if (found == null) {
            throw new IllegalArgumentException("No donation with id " + id);
        }
        if (!(found instanceof MoneyDonation donation)) {
            throw new IllegalArgumentException("Donation " + id + " is not a money donation");
        }
        if (donation.isPaid()) {
            throw new IllegalStateException("Donation is already paid");
        }
        DonationProcessingStrategy strategy = strategies.get(donation.getMethod());
        if (strategy == null) {
            throw new IllegalStateException("No payment strategy for method " + donation.getMethod());
        }

        DonationProcessingResult result = strategy.process(donation);

        if (result.isSuccessful()) {
            donation.markAsPaid(result.getReference());
            donationRepository.save(donation);
            // TODO (Decorator step): issue the receipt through ReceiptService.
        }
        return result;
    }
}
