package foodbridge.views.admin;

import java.awt.BorderLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.EventController;
import foodbridge.models.lookups.EventType;
import foodbridge.models.operations.Event;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to EventController. Creates, edits and cancels events and announces changes to volunteers.
 */
public class ManageEventsView extends ModuleView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventController eventController;
    private final DefaultTableModel rows = tableModel("ID", "Title", "Type", "Date", "Location", "Required skill", "Description");
    private final JTable table = table(rows);
    private final JTextField title = new JTextField();
    private final JComboBox<EventType> type = new JComboBox<>();
    private final JTextField date = new JTextField();
    private final JTextField location = new JTextField();
    private final JTextField skill = new JTextField();
    private final JTextField description = new JTextField();

    private record Data(List<EventType> types, List<Event> events) {
    }

    public ManageEventsView(EventController eventController) {
        super("FoodBridge - Manage events", 980, 620);
        this.eventController = eventController;

        JPanel form = form();
        field(form, "Title", title);
        field(form, "Type", type);
        field(form, "Date (yyyy-mm-dd hh:mm)", date);
        field(form, "Location", location);
        field(form, "Required skill (optional)", skill);
        field(form, "Description", description);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.add(button("Add event", this::addEvent));
        actions.add(button("Save changes", this::saveChanges));
        actions.add(button("Cancel event", this::cancelEvent));
        actions.add(button("Announce", this::announce));
        actions.add(button("Refresh", this::refresh));
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(form, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> fillForm());
    }

    @Override
    protected void refresh() {
        task(() -> new Data(eventController.listEventTypes(), eventController.listEvents()), data -> {
            choices(type, data.types());
            showEvents(data.events());
        });
    }

    private void showEvents(List<Event> events) {
        rows.setRowCount(0);
        for (Event e : events) {
            rows.addRow(new Object[] { e.getId(), e.getTitle(), e.getType().getName(),
                    e.getDate() == null ? "" : e.getDate().format(DATE_FORMAT), e.getLocation(),
                    e.getRequiredSkill(), e.getDescription() });
        }
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        title.setText(text(modelRow, 1));
        for (int i = 0; i < type.getItemCount(); i++) {
            if (type.getItemAt(i).getName().equals(rows.getValueAt(modelRow, 2))) {
                type.setSelectedIndex(i);
            }
        }
        date.setText(text(modelRow, 3));
        location.setText(text(modelRow, 4));
        skill.setText(text(modelRow, 5));
        description.setText(text(modelRow, 6));
    }

    private String text(int modelRow, int column) {
        Object value = rows.getValueAt(modelRow, column);
        return value == null ? "" : value.toString();
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select an event first.");
        }
        return (Integer) rows.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private Event readForm() {
        Event event = new Event();
        event.setTitle(title.getText().trim());
        event.setType((EventType) type.getSelectedItem());
        try {
            event.setDate(LocalDateTime.parse(date.getText().trim(), DATE_FORMAT));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Enter the date as yyyy-mm-dd hh:mm.");
        }
        event.setLocation(location.getText().isBlank() ? null : location.getText().trim());
        event.setRequiredSkill(skill.getText().isBlank() ? null : skill.getText().trim());
        event.setDescription(description.getText().isBlank() ? null : description.getText().trim());
        return event;
    }

    private void addEvent() {
        Event event = readForm();
        task(() -> {
            eventController.createEvent(event);
            return eventController.listEvents();
        }, this::showEvents);
    }

    private void saveChanges() {
        int id = selectedId();
        Event event = readForm();
        task(() -> {
            eventController.updateEvent(id, event);
            return eventController.listEvents();
        }, this::showEvents);
    }

    private void cancelEvent() {
        int id = selectedId();
        if (JOptionPane.showConfirmDialog(frame, "Cancel this event and notify its registered volunteers?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        task(() -> {
            eventController.cancelEvent(id);
            return eventController.listEvents();
        }, this::showEvents);
    }

    private void announce() {
        int id = selectedId();
        String text = JOptionPane.showInputDialog(frame, "Message for the registered volunteers:");
        if (text == null) {
            return;
        }
        task(() -> {
            eventController.announceChange(id, text);
            return true;
        }, sent -> message("Announcement sent."));
    }
}
