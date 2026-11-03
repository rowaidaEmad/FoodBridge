package foodbridge.models.donations;

import java.math.BigDecimal;

/**
 * FACTORY METHOD - ConcreteProduct. Paid through a DonationProcessingStrategy.
 */
public class MoneyDonation extends Donation {
    private BigDecimal amount;
    private String method;
    private String transactionId;
    private boolean paid;

    public MoneyDonation() {
        // TODO: implement
    }

    public BigDecimal getAmount() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isPaid() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAsPaid() {
        // TODO: implement
    }

    @Override
    public String getType() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
