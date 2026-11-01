package org.nonprofit.patterns.strategy;


import org.nonprofit.model.MoneyDonation;
public interface DonationProcessingStrategy {
    String process(MoneyDonation donation);
}

