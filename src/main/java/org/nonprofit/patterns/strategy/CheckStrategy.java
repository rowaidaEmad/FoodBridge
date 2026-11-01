package org.nonprofit.patterns.strategy;

import org.nonprofit.model.MoneyDonation;
public class CheckStrategy implements DonationProcessingStrategy {
    @Override public String process(MoneyDonation donation) { return "SIMULATED CHECK processing; no real payment executed"; }
}

