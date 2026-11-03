package foodbridge.views.admin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.EventController;
import foodbridge.models.lookups.EventType;
import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.views.DatePicker;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to EventController. Admin creates, edits and cancels events; registered volunteers
 * are notified of every change (Observer). Replaces the FoodBridgeColdStart console event menu.
 */
public class ManageEventsView extends ModuleView {
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventController eventController;
    private final DefaultTableModel events = tableModel("ID", "Title", "Type", "Date", "Location", "Status");
    private final JTable eventTable = table(events);
    private final DefaultTableModel registrations = tableModel("Volunteer", "Registered at", "Attendance");
    private final JTextField title = new JTextField();
    private final JTextField description = new JTextField();
    private final DatePicker date = new DatePicker();
    private final JSpinner time = new JSpinner(new SpinnerDateModel());
    private final JTextField location = new JTextField();
    private final JComboBox<EventType> type = new JComboBox<>();
    private List<Event> loaded = List.of();

    private record Data(List<Event> events, List<EventType> types) { }

    public ManageEventsView(EventController eventController) {
        super("FoodBridge - Manage events", 1000, 640);
        this.eventController = eventController;
        date.setMinDate(LocalDate.now());
        time.setEditor(new JSpinner.DateEditor(time, "HH:mm"));
        type.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                    boolean focus) {
                return super.getListCellRendererComponent(list, value instanceof EventType t ? t.getName() : value,
                        index, selected, focus);
            }
        });

        JPanel form = form();
        field(form, "Title", title);
        field(form, "Description", description);
        field(form, "Date", date);
        field(form, "Time", time);
        field(form, "Location", location);
        field(form, "Type", type);

        JPanel actions = new JPanel();
        actions.add(button("Create event", this::create));
        actions.add(button("Load selected", this::loadSelected));
        actions.add(button("Update selected", this::update));
        actions.add(button("Cancel selected", this::cancel));
        actions.add(button("Announce to volunteers", this::announce));
        actions.add(button("Refresh", this::refresh));

        eventTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showRegistrations();
            }
        });
        JScrollPane registrationList = new JScrollPane(table(registrations));
        registrationList.setBorder(javax.swing.BorderFactory.createTitledBorder("Registered volunteers"));
        JSplitPane lists = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(eventTable), registrationList);
        lists.setResizeWeight(0.7);

        content.add(form, BorderLayout.NORTH);
        content.add(lists, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
    }

    @Override
    protected void refresh() {
        task(() -> new Data(eventController.listEvents(), eventController.listEventTypes()), data -> {
            loaded = data.events();
            events.setRowCount(0);
            for (Event e : loaded) {
                events.addRow(new Object[] { e.getId(), e.getTitle(), e.getType().getName(), e.getDate().format(WHEN),
                        e.getLocation(), e.getStatus() });
            }
            choices(type, data.types());
            registrations.setRowCount(0);
        });
    }

    private void create() {
        Event event = fromForm();
        task(() -> eventController.createEvent(event), id -> {
            message("Event #" + id + " created.");
            clearForm();
            refresh();
        });
    }

    private void loadSelected() {
        Event e = selected();
        title.setText(e.getTitle());
        description.setText(e.getDescription() == null ? "" : e.getDescription());
        date.setDate(e.getDate().toLocalDate());
        time.setValue(Date.from(e.getDate().atZone(ZoneId.systemDefault()).toInstant()));
        location.setText(e.getLocation() == null ? "" : e.getLocation());
        for (int i = 0; i < type.getItemCount(); i++) {
            if (type.getItemAt(i).getId() == e.getType().getId()) {
                type.setSelectedIndex(i);
            }
        }
    }

    private void update() {
        Event current = selected();
        if (current.isCancelled()) {
            throw new IllegalArgumentException("Cancelled events can't be updated.");
        }
        Event data = fromForm();
        task(() -> {
            eventController.updateEvent(current.getId(), data);
            return true;
        }, ok -> {
            message("Event updated. Registered volunteers were notified.");
            refresh();
        });
    }

    private void cancel() {
        Event current = selected();
        if (JOptionPane.showConfirmDialog(frame, "Cancel \"" + current.getTitle() + "\"?", "FoodBridge",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        task(() -> eventController.cancelEvent(current.getId()), cancelled -> {
            message(cancelled ? "Event cancelled. Registered volunteers were notified." : "Event was already cancelled.");
            refresh();
        });
    }

    private void announce() {
        Event current = selected();
        String text = JOptionPane.showInputDialog(frame, "Message to volunteers of \"" + current.getTitle() + "\":");
        if (text == null) {
            return;
        }
        task(() -> {
            eventController.announceChange(current, text);
            return true;
        }, ok -> message("Message sent."));
    }

    private void showRegistrations() {
        int row = eventTable.getSelectedRow();
        registrations.setRowCount(0);
        if (row < 0) {
            return;
        }
        int id = ((Number) eventTable.getValueAt(row, 0)).intValue();
        task(() -> eventController.listRegistrations(id), list -> {
            for (EventRegistration r : list) {
                registrations.addRow(new Object[] { r.getVolunteer().getName(), r.getRegisteredAt().format(WHEN),
                        r.getAttendanceStatus() });
            }
        });
    }

    private Event selected() {
        int row = eventTable.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select an event first.");
        }
        int id = ((Number) eventTable.getValueAt(row, 0)).intValue();
        return loaded.stream().filter(e -> e.getId() == id).findFirst().orElseThrow();
    }

    private Event fromForm() {
        if (date.getDate() == null) {
            throw new IllegalArgumentException("Pick the event date.");
        }
        LocalTime at = ((Date) time.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0)
                .withNano(0);
        Event event = new Event();
        event.setTitle(title.getText());
        event.setDescription(description.getText());
        event.reschedule(LocalDateTime.of(date.getDate(), at));
        event.setLocation(location.getText());
        event.setType((EventType) type.getSelectedItem());
        return event;
    }

    private void clearForm() {
        title.setText("");
        description.setText("");
        date.setDate(null);
        location.setText("");
    }
}
