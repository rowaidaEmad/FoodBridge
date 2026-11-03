package foodbridge.views.volunteer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import foodbridge.controllers.EventController;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;

/**
 * Screen; talks only to EventController.
 */
public class EventRegistrationView implements View {
    private final int volunteerId;
    private final EventController eventController;
    private final JFrame frame = new JFrame("FoodBridge - Events");
    private final JTable eventsTable =
            SwingSupport.readOnlyTable("ID", "Title", "Type", "Date", "Location", "Required skill");
    private final JTable registrationsTable =
            SwingSupport.readOnlyTable("Event", "Date", "Location", "Attendance", "Registered");

    public EventRegistrationView(int volunteerId, EventController eventController) {
        this.volunteerId = volunteerId;
        this.eventController = eventController;

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> register());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(registerButton);

        JPanel content = new JPanel(new GridLayout(2, 1, 8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(section("Events", eventsTable, actions));
        content.add(section("My registrations", registrationsTable, null));

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        SwingSupport.attempt(frame, () -> {
            refresh();
            frame.setVisible(true);
        });
    }

    private JPanel section(String title, JTable table, JComponent actions) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(SwingSupport.scroll(table, 680, 150), BorderLayout.CENTER);
        if (actions != null) {
            panel.add(actions, BorderLayout.SOUTH);
        }
        return panel;
    }

    private void register() {
        SwingSupport.attempt(frame, () -> {
            eventController.registerVolunteer(SwingSupport.selectedId(eventsTable), volunteerId);
            refresh();
        });
    }

    private void refresh() {
        SwingSupport.setRows(eventsTable, eventRows());
        SwingSupport.setRows(registrationsTable, registrationRows());
    }

    private List<Object[]> eventRows() {
        return eventController.listEvents().stream()
                .map(event -> new Object[] { event.getId(), event.getTitle(), event.getType().getName(),
                        SwingSupport.formatDateTime(event.getDate()), event.getLocation(),
                        SwingSupport.orEmpty(event.getRequiredSkill()) })
                .toList();
    }

    private List<Object[]> registrationRows() {
        return eventController.listRegistrations(volunteerId).stream()
                .map(registration -> new Object[] { registration.getEvent().getTitle(),
                        SwingSupport.formatDateTime(registration.getEvent().getDate()),
                        registration.getEvent().getLocation(), registration.getAttendanceStatus(),
                        SwingSupport.formatDateTime(registration.getRegisteredAt()) })
                .toList();
    }
}
