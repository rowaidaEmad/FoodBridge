package foodbridge.views.auth;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import foodbridge.controllers.AuthController;
import foodbridge.models.lookups.Area;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.views.BackBar;
import foodbridge.views.View;

/**
 * Screen; talks only to AuthController.
 * Admin accounts are not created here.
 */
public class RegisterView implements View {
    private static final String DONOR = "Donor";
    private static final String VOLUNTEER = "Volunteer";
    private static final String BENEFICIARY = "Beneficiary";

    private AuthController authController;
    private final JDialog dialog = new JDialog();

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JComboBox<Area> areaBox = new JComboBox<>();
    private final JComboBox<String> roleBox = new JComboBox<>(new String[] { DONOR, VOLUNTEER, BENEFICIARY });

    private final JComboBox<String> donorTypeBox =
            new JComboBox<>(new String[] { "Individual", "Restaurant", "Supermarket/Bakery", "Company" });
    private final JTextField organizationField = new JTextField(20);
    private final JTextField availabilityField = new JTextField(20);
    private final JCheckBox transportBox = new JCheckBox("I have transport");
    private final JSpinner householdSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JTextField needsField = new JTextField(20);

    public RegisterView(AuthController authController) {
        this.authController = authController;

        // Every role has an area: pickup tasks are offered and matched to beneficiaries by area.
        authController.listAreas().forEach(areaBox::addItem);
        JPanel common = grid(8);
        addRow(common, "Name:", nameField);
        addRow(common, "Email:", emailField);
        addRow(common, "Password:", passwordField);
        addRow(common, "Confirm password:", confirmField);
        addRow(common, "Phone:", phoneField);
        addRow(common, "Address:", addressField);
        addRow(common, "Area:", areaBox);
        addRow(common, "I am a:", roleBox);

        JPanel donorPanel = grid(2);
        addRow(donorPanel, "Donor type:", donorTypeBox);
        addRow(donorPanel, "Organization (optional):", organizationField);

        JPanel volunteerPanel = grid(2);
        addRow(volunteerPanel, "Availability (e.g. weekends):", availabilityField);
        addRow(volunteerPanel, "", transportBox);

        JPanel beneficiaryPanel = grid(2);
        addRow(beneficiaryPanel, "Household size:", householdSpinner);
        addRow(beneficiaryPanel, "Needs (optional):", needsField);

        CardLayout cards = new CardLayout();
        JPanel rolePanel = new JPanel(cards);
        rolePanel.setBorder(BorderFactory.createTitledBorder("Role details"));
        rolePanel.add(donorPanel, DONOR);
        rolePanel.add(volunteerPanel, VOLUNTEER);
        rolePanel.add(beneficiaryPanel, BENEFICIARY);
        roleBox.addActionListener(e -> cards.show(rolePanel, (String) roleBox.getSelectedItem()));

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> onRegister());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(registerButton);

        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.add(common, BorderLayout.NORTH);
        form.add(rolePanel, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);

        // "← Back" returns to the login window.
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        content.add(BackBar.create(dialog::dispose), BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);

        dialog.setTitle("FoodBridge - Register");
        dialog.setModal(true);
        dialog.getRootPane().setDefaultButton(registerButton);
        dialog.add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        dialog.setVisible(true);
    }

    private void onRegister() {
        String password = new String(passwordField.getPassword());
        if (!password.equals(new String(confirmField.getPassword()))) {
            showError("Passwords do not match.");
            return;
        }
        try {
            User user = buildUser();
            user.setArea((Area) areaBox.getSelectedItem());
            user.setName(nameField.getText().trim());
            user.changeEmail(emailField.getText().trim());
            user.updateContactInfo(blankToNull(phoneField.getText()), blankToNull(addressField.getText()));
            authController.register(user, password);
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
            return;
        }
        JOptionPane.showMessageDialog(dialog, "Account created. You can now log in.");
        dialog.dispose();
    }

    private User buildUser() {
        String role = (String) roleBox.getSelectedItem();
        if (VOLUNTEER.equals(role)) {
            Volunteer v = new Volunteer();
            v.setAvailability(blankToNull(availabilityField.getText()));
            v.setHasTransport(transportBox.isSelected());
            return v;
        }
        if (BENEFICIARY.equals(role)) {
            Beneficiary b = new Beneficiary();
            b.setHouseholdSize((Integer) householdSpinner.getValue());
            b.updateNeeds(blankToNull(needsField.getText()));
            return b;
        }
        Donor d = new Donor();
        d.setDonorType((String) donorTypeBox.getSelectedItem());
        d.setOrganizationName(blankToNull(organizationField.getText()));
        return d;
    }

    private static JPanel grid(int rows) {
        return new JPanel(new GridLayout(rows, 2, 8, 8));
    }

    private static void addRow(JPanel panel, String label, java.awt.Component field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(dialog, message, "Cannot register", JOptionPane.ERROR_MESSAGE);
    }
}
