package foodbridge.views.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import foodbridge.controllers.EventController;
import foodbridge.controllers.PickupController;
import foodbridge.controllers.VolunteerController;
import foodbridge.models.operations.EventRegistration;
import foodbridge.models.users.Volunteer;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;

/**
 * Admin screen. Ranks volunteers for a task or event, assigns them, and manages event attendance.
 */
public class VolunteerAssignmentView implements View {
    private record Target(int id, String label, boolean isEvent) {
        @Override
        public String toString() {
            return label;
        }
    }

    private final VolunteerController volunteerController;
    private final PickupController pickupController;
    private final EventController eventController;
    private final JFrame frame = new JFrame("FoodBridge - Assign Volunteers");
    private final JComboBox<Target> targetBox = new JComboBox<>();
    private final JComboBox<String> strategyBox = new JComboBox<>();
    private final JTable rankedTable = SwingSupport.readOnlyTable(
            "ID", "Rank", "Name", "Availability", "Transport", "Skills", "Preferences");
    private final JTable registrationsTable = SwingSupport.readOnlyTable("ID", "Volunteer", "Attendance");
    private final JButton attendedButton = new JButton("Mark Attended");
    private final JButton announceButton = new JButton("Announce Update");

    public VolunteerAssignmentView(VolunteerController volunteerController, PickupController pickupController,
            EventController eventController) {
        this.volunteerController = volunteerController;
        this.pickupController = pickupController;
        this.eventController = eventController;

        JPanel tables = new JPanel(new GridLayout(2, 1, 8, 8));
        tables.add(rankedSection());
        tables.add(registrationsSection());

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(selectionBar(), BorderLayout.NORTH);
        content.add(tables, BorderLayout.CENTER);

        targetBox.addActionListener(e -> SwingSupport.attempt(frame, this::onTargetChanged));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        SwingSupport.attempt(frame, () -> {
            strategyBox.setModel(new DefaultComboBoxModel<>(volunteerController.strategyNames().toArray(new String[0])));
            loadTargets();
            frame.setVisible(true);
        });
    }

    private JPanel selectionBar() {
        JButton findButton = new JButton("Find Volunteers");
        findButton.addActionListener(e -> SwingSupport.attempt(frame, this::findVolunteers));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(new JLabel("Task or event:"));
        bar.add(targetBox);
        bar.add(new JLabel("Rank by:"));
        bar.add(strategyBox);
        bar.add(findButton);
        return bar;
    }

    private JPanel rankedSection() {
        JButton assignButton = new JButton("Assign");
        assignButton.addActionListener(e -> SwingSupport.attempt(frame, this::assign));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(assignButton);

        JPanel section = new JPanel(new BorderLayout(4, 4));
        section.setBorder(BorderFactory.createTitledBorder("Ranked volunteers"));
        section.add(SwingSupport.scroll(rankedTable, 820, 170), BorderLayout.CENTER);
        section.add(actions, BorderLayout.SOUTH);
        return section;
    }

    private JPanel registrationsSection() {
        attendedButton.addActionListener(e -> SwingSupport.attempt(frame, this::markAttended));
        announceButton.addActionListener(e -> SwingSupport.attempt(frame, this::announce));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(attendedButton);
        actions.add(announceButton);

        JPanel section = new JPanel(new BorderLayout(4, 4));
        section.setBorder(BorderFactory.createTitledBorder("Event registrations"));
        section.add(SwingSupport.scroll(registrationsTable, 820, 130), BorderLayout.CENTER);
        section.add(actions, BorderLayout.SOUTH);
        return section;
    }

    private void loadTargets() {
        List<Target> targets = new ArrayList<>();
        pickupController.listTasks().forEach(task -> targets.add(
                new Target(task.getId(), "Task #" + task.getId() + ": " + task.getItemsSummary(), false)));
        eventController.listEvents().stream().filter(event -> !event.isCancelled()).forEach(event -> targets.add(
                new Target(event.getId(), "Event: " + event.getTitle(), true)));
        targetBox.setModel(new DefaultComboBoxModel<>(targets.toArray(new Target[0])));
        onTargetChanged();
    }

    private void onTargetChanged() {
        Target target = (Target) targetBox.getSelectedItem();
        boolean isEvent = target != null && target.isEvent();
        attendedButton.setEnabled(isEvent);
        announceButton.setEnabled(isEvent);
        SwingSupport.setRows(rankedTable, List.of());
        refreshRegistrations();
    }

    private void findVolunteers() {
        Target target = selectedTarget();
        String strategy = (String) strategyBox.getSelectedItem();
        List<Volunteer> ranked = target.isEvent()
                ? volunteerController.rankForEvent(target.id(), strategy)
                : volunteerController.rankForTask(target.id(), strategy);
        List<Object[]> rows = new ArrayList<>();
        for (Volunteer volunteer : ranked) {
            rows.add(new Object[] { volunteer.getId(), rows.size() + 1, volunteer.getName(),
                    SwingSupport.orEmpty(volunteer.getAvailability()), volunteer.hasTransport() ? "Yes" : "No",
                    String.join(", ", volunteer.getSkills()), String.join(", ", volunteer.getPreferences()) });
        }
        SwingSupport.setRows(rankedTable, rows);
    }

    private void assign() {
        Target target = selectedTarget();
        int volunteerId = SwingSupport.selectedId(rankedTable);
        if (target.isEvent()) {
            volunteerController.requireEventSkill(target.id(), volunteerId);
            eventController.registerVolunteer(target.id(), volunteerId);
            refreshRegistrations();
        } else {
            pickupController.assignVolunteer(target.id(), volunteerId);
            loadTargets();
        }
        JOptionPane.showMessageDialog(frame, "Volunteer assigned.");
    }

    private void markAttended() {
        int registrationId = SwingSupport.selectedId(registrationsTable);
        String hours = JOptionPane.showInputDialog(frame, "Hours attended:");
        if (hours != null) {
            volunteerController.markAttended(registrationId, SwingSupport.parseHours(hours));
            refreshRegistrations();
        }
    }

    private void announce() {
        Target target = selectedTarget();
        String message = JOptionPane.showInputDialog(frame, "Message for registered volunteers:");
        if (message != null) {
            eventController.announceChange(eventController.getEvent(target.id()), message);
            JOptionPane.showMessageDialog(frame, "Update announced.");
        }
    }

    private void refreshRegistrations() {
        Target target = (Target) targetBox.getSelectedItem();
        List<Object[]> rows = new ArrayList<>();
        if (target != null && target.isEvent()) {
            for (EventRegistration registration : eventController.listRegistrations(target.id())) {
                rows.add(new Object[] { registration.getId(), registration.getVolunteer().getName(),
                        registration.getAttendanceStatus() });
            }
        }
        SwingSupport.setRows(registrationsTable, rows);
    }

    private Target selectedTarget() {
        Target target = (Target) targetBox.getSelectedItem();
        if (target == null) {
            throw new IllegalStateException("There are no open tasks or events.");
        }
        return target;
    }
}
