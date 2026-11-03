package foodbridge.views.volunteer;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import foodbridge.controllers.VolunteerController;
import foodbridge.models.users.Volunteer;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;

/**
 * Screen; talks only to VolunteerController.
 */
public class VolunteerProfileView implements View {
    private final int volunteerId;
    private final VolunteerController volunteerController;
    private final JFrame frame = new JFrame("FoodBridge - My Profile");
    private final JTextField availabilityField = new JTextField(28);
    private final JCheckBox transportBox = new JCheckBox("I have transport");
    private final JTextField skillsField = new JTextField(28);
    private final JTextField preferencesField = new JTextField(28);

    public VolunteerProfileView(int volunteerId, VolunteerController volunteerController) {
        this.volunteerId = volunteerId;
        this.volunteerController = volunteerController;

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Availability (e.g. weekends):"));
        panel.add(availabilityField);
        panel.add(new JLabel("Transport:"));
        panel.add(transportBox);
        panel.add(new JLabel("Skills (comma-separated):"));
        panel.add(skillsField);
        panel.add(new JLabel("Preferences (Pickup or event types):"));
        panel.add(preferencesField);
        panel.add(new JLabel());
        panel.add(saveButton);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        SwingSupport.attempt(frame, () -> {
            load();
            frame.setVisible(true);
        });
    }

    private void load() {
        Volunteer volunteer = volunteerController.getVolunteer(volunteerId);
        availabilityField.setText(SwingSupport.orEmpty(volunteer.getAvailability()));
        transportBox.setSelected(volunteer.hasTransport());
        skillsField.setText(String.join(", ", volunteer.getSkills()));
        preferencesField.setText(String.join(", ", volunteer.getPreferences()));
    }

    private void save() {
        SwingSupport.attempt(frame, () -> {
            volunteerController.updateProfile(volunteerId, availabilityField.getText().trim(), transportBox.isSelected(),
                    SwingSupport.splitValues(skillsField.getText()), SwingSupport.splitValues(preferencesField.getText()));
            load();
            JOptionPane.showMessageDialog(frame, "Profile saved.");
        });
    }
}
