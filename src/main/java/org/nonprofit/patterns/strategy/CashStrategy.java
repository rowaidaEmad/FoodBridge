package org.nonprofit.patterns.strategy;

import org.nonprofit.model.MoneyDonation;
public class CashStrategy implements DonationProcessingStrategy {
    @Override public String process(MoneyDonation donation) { return "SIMULATED CASH processing; no real payment executed"; }
}

