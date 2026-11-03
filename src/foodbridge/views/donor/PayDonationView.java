package foodbridge.views.donor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.DonationController;
import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.models.users.Donor;
import foodbridge.views.BackBar;
import foodbridge.views.View;

/**
 * Screen; talks only to DonationController.
 * "My donations": lists the donor's donations; Pay runs the payment Strategy, Cancel works while Posted.
 */
public class PayDonationView implements View {
    private DonationController donationController;
    private final Donor donor;
    private final JDialog dialog = new JDialog();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Date", "Type", "Details", "Status", "Payment" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public PayDonationView(DonationController donationController, Donor donor) {
        this.donationController = donationController;
        this.donor = donor;
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(320);

        JButton pay = new JButton("Pay");
        JButton cancel = new JButton("Cancel donation");
        JButton viewItems = new JButton("View items");
        JButton receipt = new JButton("Receipt");
        JButton refresh = new JButton("Refresh");
        pay.addActionListener(e -> onPay());
        cancel.addActionListener(e -> onCancel());
        viewItems.addActionListener(e -> onViewItems());
        receipt.addActionListener(e -> onReceipt());
        refresh.addActionListener(e -> reload());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(pay);
        buttons.add(cancel);
        buttons.add(viewItems);
        buttons.add(receipt);
        buttons.add(refresh);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        content.add(BackBar.create(dialog::dispose), BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        dialog.setTitle("FoodBridge - My donations");
        dialog.setModal(true);
        dialog.add(content);
        dialog.setSize(860, 400);
        dialog.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        reload();
        dialog.setVisible(true);
    }

    private void reload() {
        model.setRowCount(0);
        try {
            for (Donation d : donationController.listDonations(donor.getId())) {
                String payment = "-";
                if (d instanceof MoneyDonation m) {
                    payment = m.isPaid() ? m.getMethod() + ", ref " + m.getTransactionId()
                            : m.getMethod() + ", unpaid";
                }
                model.addRow(new Object[] { d.getId(), d.getDate(), d.getType(), d.getDetails(),
                        d.getStatus().getName(), payment });
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void onPay() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        try {
            DonationProcessingResult result = donationController.payMoneyDonation(id);
            if (result.isSuccessful()) {
                JOptionPane.showMessageDialog(dialog,
                        result.getMessage() + "\nReference: " + result.getReference(), "Payment successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, result.getMessage(), "Payment failed",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
        reload();
    }

    private void onCancel() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        try {
            if (donationController.cancelDonation(id)) {
                JOptionPane.showMessageDialog(dialog, "Donation #" + id + " cancelled.");
            } else {
                showError("Only donations that are still Posted can be cancelled.");
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
        reload();
    }

    private void onViewItems() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        try {
            Donation d = donationController.getDonation(id);
            if (!(d instanceof FoodDonation f)) {
                showError("Only food donations have items.");
                return;
            }
            StringBuilder text = new StringBuilder();
            for (FoodItem item : f.getItems()) {
                text.append("- ").append(item.getInfo()).append(item.isExpired() ? "  [EXPIRED]" : "").append('\n');
            }
            text.append("\nTotal quantity: ").append(f.getTotalQuantity())
                    .append("\nDonation expiry: ").append(f.getExpiryDate() == null ? "-" : f.getExpiryDate())
                    .append(f.isExpired() ? " (expired)" : "")
                    .append("\nPickup window: ").append(f.getPickupWindow() == null ? "-" : f.getPickupWindow());
            JOptionPane.showMessageDialog(dialog, text.toString(), "Donation #" + id + " items",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void onReceipt() {
        Integer id = selectedId();
        if (id != null) {
            new ReceiptView(donationController, id).show();
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(dialog, "Select a donation first.");
            return null;
        }
        return (Integer) model.getValueAt(row, 0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(dialog, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
