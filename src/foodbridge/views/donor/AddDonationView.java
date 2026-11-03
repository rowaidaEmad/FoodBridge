package foodbridge.views.donor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.DonationController;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.users.Donor;
import foodbridge.views.BackBar;
import foodbridge.views.View;

/**
 * Screen; talks only to DonationController. Money tab (amount + method) and Food tab (items).
 */
public class AddDonationView implements View {
    private DonationController donationController;
    private final Donor donor;
    private final JDialog dialog = new JDialog();

    private final JTextField amountField = new JTextField(12);
    private final JComboBox<String> methodBox = new JComboBox<>(MoneyDonation.METHODS.toArray(new String[0]));

    private final List<FoodItem> items = new ArrayList<>();
    private final DefaultTableModel itemsModel = new DefaultTableModel(
            new Object[] { "Name", "Quantity", "Unit", "Category", "Allergens", "Expiry" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable itemsTable = new JTable(itemsModel);
    private final JTextField itemName = new JTextField(10);
    private final JTextField itemQuantity = new JTextField(5);
    private final JComboBox<Unit> itemUnit = new JComboBox<>();
    private final JComboBox<FoodCategory> itemCategory = new JComboBox<>();
    private final JTextField itemAllergens = new JTextField(8);
    private final JTextField itemExpiry = new JTextField(9);
    private final JTextField expiryField = new JTextField(10);
    private final JTextField pickupField = new JTextField(14);

    public AddDonationView(DonationController donationController, Donor donor) {
        this.donationController = donationController;
        this.donor = donor;

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Money", moneyPanel());
        tabs.addTab("Food", foodPanel());

        JPanel page = new JPanel(new BorderLayout(4, 4));
        page.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        page.add(BackBar.create(dialog::dispose), BorderLayout.NORTH);
        page.add(tabs, BorderLayout.CENTER);

        dialog.setTitle("FoodBridge - Add donation");
        dialog.setModal(true);
        dialog.add(page);
        dialog.setSize(760, 490);
        dialog.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        try {
            for (Unit u : donationController.listUnits()) {
                itemUnit.addItem(u);
            }
            for (FoodCategory c : donationController.listFoodCategories()) {
                itemCategory.addItem(c);
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
            return;
        }
        dialog.setVisible(true);
    }

    private JPanel moneyPanel() {
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Amount:"));
        form.add(amountField);
        form.add(new JLabel("Payment method:"));
        form.add(methodBox);

        JButton save = new JButton("Save money donation");
        save.addActionListener(e -> {
            try {
                DonationDetails details = new DonationDetails();
                details.setDonor(donor);
                details.setAmount(new BigDecimal(amountField.getText().trim()));
                details.setMethod((String) methodBox.getSelectedItem());
                int id = donationController.createDonation("money", details);
                JOptionPane.showMessageDialog(dialog,
                        "Money donation #" + id + " saved (Posted, unpaid).\nPay it from 'My donations'.");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                showError("Enter a valid amount, e.g. 500");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(form, BorderLayout.NORTH);
        panel.add(right(save), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel foodPanel() {
        itemUnit.setRenderer(nameRenderer());
        itemCategory.setRenderer(nameRenderer());

        JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        itemRow.setBorder(BorderFactory.createTitledBorder("New item"));
        itemRow.add(new JLabel("Name"));
        itemRow.add(itemName);
        itemRow.add(new JLabel("Qty"));
        itemRow.add(itemQuantity);
        itemRow.add(itemUnit);
        itemRow.add(itemCategory);
        itemRow.add(new JLabel("Allergens"));
        itemRow.add(itemAllergens);
        itemRow.add(new JLabel("Expiry"));
        itemRow.add(itemExpiry);
        JButton addItem = new JButton("Add item");
        addItem.addActionListener(e -> onAddItem());
        itemRow.add(addItem);

        JPanel donationRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        donationRow.add(new JLabel("Donation expiry (yyyy-mm-dd):"));
        donationRow.add(expiryField);
        donationRow.add(new JLabel("Pickup window:"));
        donationRow.add(pickupField);

        JButton removeItem = new JButton("Remove selected item");
        removeItem.addActionListener(e -> {
            int row = itemsTable.getSelectedRow();
            if (row >= 0) {
                items.remove(row);
                itemsModel.removeRow(row);
            }
        });
        JButton save = new JButton("Save food donation");
        save.addActionListener(e -> onSaveFood());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(donationRow, BorderLayout.NORTH);
        JPanel buttons = right(removeItem);
        buttons.add(save);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(itemRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void onAddItem() {
        try {
            FoodItem item = new FoodItem();
            item.setName(itemName.getText());
            item.setQuantity(Double.parseDouble(itemQuantity.getText().trim()));
            item.setUnit((Unit) itemUnit.getSelectedItem());
            item.setCategory((FoodCategory) itemCategory.getSelectedItem());
            item.setAllergens(itemAllergens.getText().isBlank() ? null : itemAllergens.getText().trim());
            item.setExpiryDate(parseDate(itemExpiry.getText()));
            items.add(item);
            itemsModel.addRow(new Object[] { item.getName(), item.getQuantity(), item.getUnit().getName(),
                    item.getCategory().getName(), item.getAllergens(), item.getExpiryDate() });
            itemName.setText("");
            itemQuantity.setText("");
            itemAllergens.setText("");
            itemExpiry.setText("");
        } catch (NumberFormatException ex) {
            showError("Enter a valid quantity, e.g. 10");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void onSaveFood() {
        if (items.isEmpty()) {
            showError("Add at least one item.");
            return;
        }
        try {
            DonationDetails details = new DonationDetails();
            details.setDonor(donor);
            details.setItems(items);
            details.setExpiryDate(parseDate(expiryField.getText()));
            details.setPickupWindow(pickupField.getText().isBlank() ? null : pickupField.getText().trim());
            int id = donationController.createDonation("food", details);
            JOptionPane.showMessageDialog(dialog, "Food donation #" + id + " saved with " + items.size() + " item(s).");
            dialog.dispose();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    /** Empty -> null; otherwise yyyy-mm-dd. */
    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Dates must look like 2026-12-31");
        }
    }

    private static javax.swing.ListCellRenderer<Object> nameRenderer() {
        return new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                Object label = value instanceof Unit u ? u.getName()
                        : value instanceof FoodCategory c ? c.getName() : value;
                return super.getListCellRendererComponent(list, label, index, selected, focus);
            }
        };
    }

    private static JPanel right(JButton button) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(button);
        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(dialog, message, "Cannot save", JOptionPane.ERROR_MESSAGE);
    }
}
