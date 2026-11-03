package foodbridge.patterns.strategy;

import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.MoneyDonation;

// Strategy pattern - ConcreteStrategy
/**
 * STRATEGY - ConcreteStrategy. Cash collected by a volunteer at pickup.
 * Only decides the outcome; DonationService marks the donation paid and saves it.
 */
public class CashStrategy implements DonationProcessingStrategy {
    @Override
    public DonationProcessingResult process(MoneyDonation donation) {
        DonationProcessingResult invalid = invalid(donation);
        if (invalid != null) {
            return invalid;
        }
        String reference = "CASH-" + donation.getId() + "-" + System.currentTimeMillis();
        return new DonationProcessingResult(true, reference, "Cash payment recorded; collected at pickup");
    }

    private DonationProcessingResult invalid(MoneyDonation donation) {
        if (donation == null) {
            throw new IllegalArgumentException("Donation is required");
        }
        return donation.getAmount() <= 0 ? new DonationProcessingResult(false, null, "Invalid amount") : null;
    }
}
