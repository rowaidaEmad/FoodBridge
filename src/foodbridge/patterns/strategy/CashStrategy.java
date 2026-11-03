package foodbridge.patterns.strategy;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationProcessingResult;

/**
 * STRATEGY - ConcreteStrategy.
 */
public class CashStrategy implements DonationProcessingStrategy {
    @Override
    public DonationProcessingResult process(Donation donation) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
