package foodbridge.views.volunteer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import foodbridge.controllers.EventController;
import foodbridge.controllers.NotificationController;
import foodbridge.controllers.PickupController;
import foodbridge.controllers.VolunteerController;
import foodbridge.models.users.Volunteer;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;
import foodbridge.views.auth.NotificationsView;

/**
 * Volunteer dashboard; opens the volunteer screens.
 */
public class VolunteerPanelView implements View {
    private final int volunteerId;
    private final VolunteerController volunteerController;
    private final JFrame frame = new JFrame("FoodBridge - Volunteer Panel");
    private final JLabel summary = new JLabel();

    public VolunteerPanelView(int volunteerId, VolunteerController volunteerController,
            PickupController pickupController, EventController eventController,
            NotificationController notificationController) {
        this.volunteerId = volunteerId;
        this.volunteerController = volunteerController;
        Volunteer volunteer = volunteerController.getVolunteer(volunteerId);

        JPanel buttons = new JPanel(new GridLayout(5, 1, 8, 8));
        buttons.add(button("My Profile", new VolunteerProfileView(volunteerId, volunteerController)));
        buttons.add(button("Pickup Tasks", new PickupTasksView(volunteerId, pickupController)));
        buttons.add(button("Events", new EventRegistrationView(eventController, volunteer)));
        buttons.add(button("Hours & Certificates", new HoursCertificateView(volunteerId, volunteerController)));
        buttons.add(button("Notifications", new NotificationsView(notificationController, volunteer)));

        JPanel panel = new JPanel(new BorderLayout(8, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        panel.add(summary, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.CENTER);

        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refresh();
            }
        });
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(panel);
        refresh();
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        frame.setVisible(true);
    }

    private JButton button(String label, View screen) {
        JButton button = new JButton(label);
        button.addActionListener(e -> screen.show());
        return button;
    }

    private void refresh() {
        SwingSupport.attempt(frame, () -> {
            Volunteer volunteer = volunteerController.getVolunteer(volunteerId);
            summary.setText(String.format("%s - %.2f total hours", volunteer.getName(), volunteer.getTotalHours()));
        });
    }
}
