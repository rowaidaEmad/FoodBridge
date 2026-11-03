package foodbridge.models.donations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import foodbridge.models.users.Donor;

// Factory Method pattern - ConcreteProduct
/**
 * FACTORY METHOD - ConcreteProduct. Paid through a DonationProcessingStrategy.
 * Table: money_donations.
 */
public class MoneyDonation extends Donation {
    public static final List<String> METHODS = List.of("cash", "check", "online");

    private double amount;
    private String method;
    private String transactionId;
    private boolean paid;

    /** Used when loading from the database. */
    private MoneyDonation() {
    }

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

    /** Called by the payment Strategy after a successful payment; call save() to store it. */
    public void markAsPaid(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction id is required");
        }
        this.transactionId = transactionId.trim();
        this.paid = true;
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

    @Override
    protected void saveDetails(Connection c) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        String sql = "INSERT INTO money_donations(donation_id, amount, method, transaction_id, paid)"
                + " VALUES (?, ?, ?, ?, ?)"
                + " ON CONFLICT (donation_id) DO UPDATE SET amount = EXCLUDED.amount, method = EXCLUDED.method,"
                + " transaction_id = EXCLUDED.transaction_id, paid = EXCLUDED.paid";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setDouble(2, amount);
            ps.setString(3, method);
            ps.setString(4, transactionId);
            ps.setBoolean(5, paid);
            ps.executeUpdate();
        }
    }

    static MoneyDonation load(ResultSet rs, Donor donor) throws SQLException {
        MoneyDonation d = new MoneyDonation();
        d.readCommon(rs, donor);
        d.amount = rs.getDouble("amount");
        d.method = rs.getString("method");
        d.transactionId = rs.getString("transaction_id");
        d.paid = rs.getBoolean("paid");
        return d;
    }
}
