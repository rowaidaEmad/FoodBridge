package foodbridge.patterns.strategy;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationProcessingResult;

/**
 * STRATEGY - Strategy. Returns a DonationProcessingResult.
 */
public interface DonationProcessingStrategy {
    DonationProcessingResult process(Donation donation);
}
