package org.nonprofit.patterns.factory;


import java.math.BigDecimal;
import org.nonprofit.model.*;

public class DonationFactory {
    public MoneyDonation createMoneyDonation(long donorId, BigDecimal amount, String method) {
        if(amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        MoneyDonation d = new MoneyDonation(); d.setDonorId(donorId);d.setAmount(amount);d.setMethod(method);d.setDate(java.time.LocalDate.now());return d;
    }
    public FoodDonation createFoodDonation(long donorId) {
        FoodDonation d = new FoodDonation();d.setDonorId(donorId);d.setDate(java.time.LocalDate.now());return d;
    }
}

