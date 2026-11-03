package foodbridge.views.volunteer;

import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.EventController;
import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to EventController. A volunteer registers for upcoming events and is then
 * notified (Observer) whenever the event changes or is cancelled.
 */
public class EventRegistrationView extends ModuleView {
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventController eventController;
    private final int volunteerId;
    private final DefaultTableModel events = tableModel("ID", "Title", "Type", "Date", "Location", "Required skill");
    private final JTable eventTable = table(events);
    private final DefaultTableModel mine = tableModel("Event", "Date", "Status", "Attendance");

    private record Data(List<Event> events, List<EventRegistration> mine) { }

    public EventRegistrationView(int volunteerId, EventController eventController) {
        super("FoodBridge - Events", 900, 560);
        this.eventController = eventController;
        this.volunteerId = volunteerId;

        JScrollPane upcoming = new JScrollPane(eventTable);
        upcoming.setBorder(BorderFactory.createTitledBorder("Upcoming events"));
        JScrollPane registered = new JScrollPane(table(mine));
        registered.setBorder(BorderFactory.createTitledBorder("My registrations"));
        JSplitPane lists = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upcoming, registered);
        lists.setResizeWeight(0.55);

        JPanel actions = new JPanel();
        actions.add(button("Register for selected event", this::register));
        actions.add(button("Refresh", this::refresh));
        content.add(lists, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
    }

    @Override
    protected void refresh() {
        task(() -> new Data(eventController.listEvents(), eventController.listVolunteerRegistrations(volunteerId)),
                data -> {
                    events.setRowCount(0);
                    for (Event e : data.events()) {
                        if (!e.isCancelled()) {
                            events.addRow(new Object[] { e.getId(), e.getTitle(), e.getType().getName(),
                                    e.getDate().format(WHEN), e.getLocation(), e.getRequiredSkill() });
                        }
                    }
                    mine.setRowCount(0);
                    for (EventRegistration r : data.mine()) {
                        mine.addRow(new Object[] { r.getEvent().getTitle(), r.getEvent().getDate().format(WHEN),
                                r.getEvent().getStatus(), r.getAttendanceStatus() });
                    }
                });
    }

    private void register() {
        int row = eventTable.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select an event first.");
        }
        int eventId = ((Number) eventTable.getValueAt(row, 0)).intValue();
        task(() -> {
            eventController.registerVolunteer(eventId, volunteerId);
            return true;
        }, ok -> {
            message("Registered. You will be notified if this event changes.");
            refresh();
        });
    }
}
