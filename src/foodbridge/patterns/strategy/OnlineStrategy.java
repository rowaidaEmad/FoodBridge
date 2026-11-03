package foodbridge.patterns.strategy;

import java.util.UUID;

import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.MoneyDonation;

// Strategy pattern - ConcreteStrategy
/**
 * STRATEGY - ConcreteStrategy. Simulated card / wallet gateway (no real payment API).
 * Only decides the outcome; DonationService marks the donation paid and saves it.
 */
public class OnlineStrategy implements DonationProcessingStrategy {
    private static final double GATEWAY_LIMIT = 100000;

    @Override
    public DonationProcessingResult process(MoneyDonation donation) {
        DonationProcessingResult invalid = invalid(donation);
        if (invalid != null) {
            return invalid;
        }
        if (donation.getAmount() > GATEWAY_LIMIT) {
            return new DonationProcessingResult(false, null, "Payment declined by gateway (limit exceeded)");
        }
        return new DonationProcessingResult(true, generateTransactionId(), "Online payment approved");
    }

    private String generateTransactionId() {
        return "ONL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private DonationProcessingResult invalid(MoneyDonation donation) {
        if (donation == null) {
            throw new IllegalArgumentException("Donation is required");
        }
        return donation.getAmount() <= 0 ? new DonationProcessingResult(false, null, "Invalid amount") : null;
    }
}
