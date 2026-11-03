package foodbridge.repositories;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import foodbridge.models.donations.Receipt;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Receipt (connection from DatabaseManager).
 */
public class ReceiptRepository implements Repository<Receipt> {
    private DatabaseManager db;

    public ReceiptRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Receipt item) {
        validate(item);
        String sql = "INSERT INTO receipts(donation_id, receipt_number, issue_date) VALUES (?, ?, ?) RETURNING id";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, item);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                item.setId(rs.getInt("id"));
            }
        } catch (SQLException ex) {
            throw failure("save receipt", ex);
        }
    }

    /** The unique donation key also prevents duplicates when two clients create a receipt together. */
    public Receipt createIfAbsent(Receipt item) {
        validate(item);
        String sql = "INSERT INTO receipts(donation_id, receipt_number, issue_date) VALUES (?, ?, ?)"
                + " ON CONFLICT (donation_id) DO UPDATE SET donation_id = EXCLUDED.donation_id RETURNING *";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, item);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Receipt saved = new Receipt(item.getDonation(), rs.getString("receipt_number"),
                        rs.getDate("issue_date").toLocalDate());
                saved.setId(rs.getInt("id"));
                return saved;
            }
        } catch (SQLException ex) {
            throw failure("create receipt", ex);
        }
    }

    @Override
    public Receipt findById(int id) {
        return query(" WHERE id = ?", id).stream().findFirst().orElse(null);
    }

    public Receipt findByDonation(int donationId) {
        return query(" WHERE donation_id = ?", donationId).stream().findFirst().orElse(null);
    }

    @Override
    public List<Receipt> findAll() {
        return query("");
    }

    @Override
    public void update(Receipt item) {
        validate(item);
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE receipts SET donation_id = ?, receipt_number = ?, issue_date = ? WHERE id = ?")) {
            bind(ps, item);
            ps.setInt(4, item.getId());
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("No receipt with id " + item.getId());
            }
        } catch (SQLException ex) {
            throw failure("update receipt", ex);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM receipts WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("No receipt with id " + id);
            }
        } catch (SQLException ex) {
            throw failure("delete receipt", ex);
        }
    }

    private List<Receipt> query(String suffix, int... ids) {
        List<Receipt> receipts = new ArrayList<>();
        DonationRepository donations = new DonationRepository();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM receipts" + suffix + " ORDER BY issue_date DESC, id DESC")) {
            for (int i = 0; i < ids.length; i++) ps.setInt(i + 1, ids[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Receipt receipt = new Receipt(donations.findById(rs.getInt("donation_id")),
                            rs.getString("receipt_number"), rs.getDate("issue_date").toLocalDate());
                    receipt.setId(rs.getInt("id"));
                    receipts.add(receipt);
                }
            }
        } catch (SQLException ex) {
            throw failure("load receipts", ex);
        }
        return receipts;
    }

    private void validate(Receipt item) {
        if (item == null || item.getDonation() == null || item.getDonation().getId() <= 0
                || item.getReceiptNumber() == null || item.getReceiptNumber().isBlank()
                || item.getIssueDate() == null) {
            throw new IllegalArgumentException("A receipt needs a saved donation, number and issue date");
        }
    }

    private void bind(PreparedStatement ps, Receipt item) throws SQLException {
        ps.setInt(1, item.getDonation().getId());
        ps.setString(2, item.getReceiptNumber());
        ps.setDate(3, Date.valueOf(item.getIssueDate()));
    }

    private RuntimeException failure(String action, SQLException ex) {
        return new IllegalStateException("Could not " + action + ": " + ex.getMessage(), ex);
    }
}
