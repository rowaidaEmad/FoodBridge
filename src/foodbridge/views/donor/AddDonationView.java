package foodbridge.views.donor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.users.Donor;
import foodbridge.views.BackBar;
import foodbridge.views.DatePicker;
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
    private final DatePicker itemExpiry = new DatePicker();
    private final DatePicker expiryField = new DatePicker();
    private final JTextField pickupField = new JTextField(14);
    private final JComboBox<Area> areaBox = new JComboBox<>();

    public AddDonationView(DonationController donationController, Donor donor) {
        this.donationController = donationController;
        this.donor = donor;
        // Food that is already expired can't be donated.
        itemExpiry.setMinDate(LocalDate.now());
        expiryField.setMinDate(LocalDate.now());

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
        dialog.setSize(760, 520);
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
            // Volunteers see pickup tasks by area, so default to the area the donor lives in.
            for (Area a : donationController.listAreas()) {
                areaBox.addItem(a);
                if (donor.getArea() != null && donor.getArea().getId() == a.getId()) {
                    areaBox.setSelectedItem(a);
                }
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

        // Two rows so every field and the Add button fit in the window width.
        JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        firstRow.add(new JLabel("Name"));
        firstRow.add(itemName);
        firstRow.add(new JLabel("Qty"));
        firstRow.add(itemQuantity);
        firstRow.add(new JLabel("Unit"));
        firstRow.add(itemUnit);
        firstRow.add(new JLabel("Category"));
        firstRow.add(itemCategory);

        JButton addItem = new JButton("Add item");
        addItem.addActionListener(e -> onAddItem());
        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        secondRow.add(new JLabel("Allergens"));
        secondRow.add(itemAllergens);
        secondRow.add(new JLabel("Expiry"));
        secondRow.add(itemExpiry);
        secondRow.add(addItem);

        JPanel itemRow = new JPanel(new GridLayout(2, 1));
        itemRow.setBorder(BorderFactory.createTitledBorder("New item"));
        itemRow.add(firstRow);
        itemRow.add(secondRow);

        JPanel donationRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        donationRow.add(new JLabel("Donation expiry:"));
        donationRow.add(expiryField);
        donationRow.add(new JLabel("Pickup window:"));
        donationRow.add(pickupField);
        JPanel areaRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        areaRow.add(new JLabel("Pickup area:"));
        areaRow.add(areaBox);
        JPanel donationRows = new JPanel(new GridLayout(0, 1));
        donationRows.add(donationRow);
        donationRows.add(areaRow);

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
        bottom.add(donationRows, BorderLayout.NORTH);
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
            item.setExpiryDate(itemExpiry.getDate());
            items.add(item);
            itemsModel.addRow(new Object[] { item.getName(), item.getQuantity(), item.getUnit().getName(),
                    item.getCategory().getName(), item.getAllergens(), item.getExpiryDate() });
            itemName.setText("");
            itemQuantity.setText("");
            itemAllergens.setText("");
            itemExpiry.setDate(null);
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
            details.setExpiryDate(expiryField.getDate());
            details.setArea((Area) areaBox.getSelectedItem());
            details.setPickupWindow(pickupField.getText().isBlank() ? null : pickupField.getText().trim());
            int id = donationController.createDonation("food", details);
            JOptionPane.showMessageDialog(dialog, "Food donation #" + id + " saved with " + items.size() + " item(s).");
            dialog.dispose();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
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
