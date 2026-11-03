package foodbridge.patterns.strategy;

import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.MoneyDonation;

/**
 * STRATEGY - ConcreteStrategy.
 */
public class CheckStrategy implements DonationProcessingStrategy {
    @Override
    public DonationProcessingResult process(MoneyDonation donation) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
