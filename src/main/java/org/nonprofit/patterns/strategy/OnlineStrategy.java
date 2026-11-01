package org.nonprofit.patterns.strategy;

import org.nonprofit.model.MoneyDonation;
public class OnlineStrategy implements DonationProcessingStrategy {
    @Override public String process(MoneyDonation donation) { return "SIMULATED ONLINE processing; no real payment executed"; }
}

