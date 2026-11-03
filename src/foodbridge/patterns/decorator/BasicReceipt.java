package foodbridge.patterns.decorator;

import foodbridge.models.donations.Receipt;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.donations.MoneyDonation;
import java.util.Locale;

/**
 * DECORATOR - ConcreteComponent. Formats a Receipt.
 */
public class BasicReceipt implements ReceiptComponent {
    private final Receipt receipt;

    public BasicReceipt(Receipt receipt) {
        if (receipt == null || receipt.getDonation() == null || receipt.getReceiptNumber() == null) {
            throw new IllegalArgumentException("A complete receipt is required");
        }
        this.receipt = receipt;
    }

    @Override
    public String generate() {
        var donation = receipt.getDonation();
        StringBuilder text = new StringBuilder("FoodBridge - Donation receipt\n")
                .append("Receipt: ").append(receipt.getReceiptNumber())
                .append("\nIssued: ").append(receipt.getIssueDate())
                .append("\nDonation: #").append(donation.getId())
                .append("\nDonation date: ").append(donation.getDate())
                .append("\nDonor: ").append(donation.getDonor().getName())
                .append("\nType: ").append(donation.getType());
        if (donation instanceof MoneyDonation money) {
            text.append(String.format(Locale.ROOT, "\nAmount: %.2f", money.getAmount()))
                    .append("\nPayment method: ").append(money.getMethod())
                    .append("\nPayment: ").append(money.isPaid() ? "Paid" : "Unpaid");
            if (money.getTransactionId() != null) {
                text.append("\nPayment reference: ").append(money.getTransactionId());
            }
        } else if (donation instanceof FoodDonation food) {
            text.append("\nItems:");
            for (FoodItem item : food.getItems()) {
                text.append("\n- ").append(item.getInfo());
            }
        }
        return text.toString();
    }
}
