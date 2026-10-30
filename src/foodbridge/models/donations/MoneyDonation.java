package foodbridge.models.donations;

import foodbridge.models.enums.PaymentMethod;

public class MoneyDonation extends Donation {
    private double amount;
    private PaymentMethod method;

    public boolean pay() {
        // TODO
        return false;
    }
}
