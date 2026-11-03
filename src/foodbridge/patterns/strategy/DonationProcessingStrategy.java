package foodbridge.patterns.strategy;

import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.MoneyDonation;

// Strategy pattern - Strategy
/**
 * STRATEGY - Strategy. One implementation per payment method; returns a DonationProcessingResult.
 */
public interface DonationProcessingStrategy {
    DonationProcessingResult process(MoneyDonation donation);
}
