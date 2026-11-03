package foodbridge.views.donor;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import foodbridge.controllers.DonationController;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;

/**
 * Screen; talks only to DonationController. Any logged-in user (donor or volunteer) can donate food.
 */
public class AddDonationView implements View {
    private DonationController donationController;
    private final User user;
    private final JFrame frame = new JFrame("FoodBridge - Donate Food");
    private final JTextField nameField = new JTextField(20);
    private final JTextField quantityField = new JTextField(20);
    private final JComboBox<Unit> unitBox = new JComboBox<>();
    private final JComboBox<FoodCategory> categoryBox = new JComboBox<>();
    private final JTextField expiryField = new JTextField(20);
    private final JTextField allergensField = new JTextField(20);
    private final JComboBox<Area> areaBox = new JComboBox<>();
    private final JTextField windowField = new JTextField(20);

    public AddDonationView(User user, DonationController donationController) {
        this.user = user;
        this.donationController = donationController;

        JButton donateButton = new JButton("Donate");
        donateButton.addActionListener(e -> donate());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Food item:"));
        form.add(nameField);
        form.add(new JLabel("Quantity:"));
        form.add(quantityField);
        form.add(new JLabel("Unit:"));
        form.add(unitBox);
        form.add(new JLabel("Category:"));
        form.add(categoryBox);
        form.add(new JLabel("Expiry date (yyyy-mm-dd):"));
        form.add(expiryField);
        form.add(new JLabel("Allergens (optional):"));
        form.add(allergensField);
        form.add(new JLabel("Pickup area:"));
        form.add(areaBox);
        form.add(new JLabel("Pickup window (e.g. weekends):"));
        form.add(windowField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(donateButton);
        JPanel content = new JPanel(new java.awt.BorderLayout());
        content.add(form, java.awt.BorderLayout.CENTER);
        content.add(buttons, java.awt.BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        SwingSupport.attempt(frame, () -> {
            donationController.listUnits().forEach(unitBox::addItem);
            donationController.listCategories().forEach(categoryBox::addItem);
            donationController.listAreas().forEach(areaBox::addItem);
            if (user.getArea() != null) {
                for (int i = 0; i < areaBox.getItemCount(); i++) {
                    if (areaBox.getItemAt(i).getId() == user.getArea().getId()) {
                        areaBox.setSelectedIndex(i);
                    }
                }
            }
            frame.setVisible(true);
        });
    }

    private void donate() {
        SwingSupport.attempt(frame, () -> {
            DonationDetails details = buildDetails();
            int id = donationController.createDonation("FOOD", details);
            JOptionPane.showMessageDialog(frame, "Thank you! Donation #" + id + " is posted for pickup.");
            frame.dispose();
        });
    }

    private DonationDetails buildDetails() {
        if (nameField.getText().isBlank()) {
            throw new IllegalArgumentException("Enter the food item.");
        }
        if (categoryBox.getSelectedItem() == null || unitBox.getSelectedItem() == null) {
            throw new IllegalArgumentException("No categories or units found; run database/seed.sql.");
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter the quantity as a number.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be more than 0.");
        }
        LocalDate expiry = parseExpiry();

        FoodItem item = new FoodItem();
        item.setName(nameField.getText().trim());
        item.setQuantity(quantity);
        item.setCategory((FoodCategory) categoryBox.getSelectedItem());
        item.setUnit((Unit) unitBox.getSelectedItem());
        item.setAllergens(allergensField.getText().isBlank() ? null : allergensField.getText().trim());
        item.setExpiryDate(expiry);

        Donor donor = new Donor();
        donor.setId(user.getId());
        donor.setName(user.getName());

        DonationDetails details = new DonationDetails();
        details.setDonor(donor);
        details.setItems(List.of(item));
        details.setExpiryDate(expiry);
        details.setArea((Area) areaBox.getSelectedItem());
        details.setPickupWindow(windowField.getText().isBlank() ? null : windowField.getText().trim());
        return details;
    }

    private LocalDate parseExpiry() {
        if (expiryField.getText().isBlank()) {
            return null;
        }
        try {
            LocalDate expiry = LocalDate.parse(expiryField.getText().trim());
            if (expiry.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("The expiry date is already past.");
            }
            return expiry;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Enter the expiry date as yyyy-mm-dd.");
        }
    }
}
