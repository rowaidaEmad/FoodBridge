package foodbridge.models.donations;

import java.util.List;

import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.users.Donor;

// Factory Method pattern - ConcreteProduct
/**
 * FACTORY METHOD - ConcreteProduct. Paid through a DonationProcessingStrategy.
 */
public class MoneyDonation extends Donation {
    public static final List<String> METHODS = List.of("cash", "check", "online");

    private double amount;
    private String method;
    private String transactionId;
    private boolean paid;

    public MoneyDonation(Donor donor) {
        super(donor);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        String value = method == null ? "" : method.trim().toLowerCase();
        if (!METHODS.contains(value)) {
            throw new IllegalArgumentException("Payment method must be cash, check or online");
        }
        this.method = value;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isPaid() {
        return paid;
    }

    /** Called by the payment Strategy after a successful payment. */
    public void markAsPaid(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction id is required");
        }
        this.transactionId = transactionId.trim();
        this.paid = true;
    }

    /** A paid donation cannot be cancelled (the money was already taken). */
    @Override
    public boolean cancel(DonationStatus cancelled) {
        return !paid && super.cancel(cancelled);
    }

    @Override
    public String getType() {
        return "money";
    }

    @Override
    public String getDetails() {
        return String.format("Money donation - %.2f (%s, %s) - %s",
                amount, method, paid ? "paid" : "unpaid", statusName());
    }
}
