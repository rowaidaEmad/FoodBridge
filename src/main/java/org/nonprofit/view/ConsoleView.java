package org.nonprofit.view;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.nonprofit.controller.EventController;
import org.nonprofit.controller.EventTypeController;
import org.nonprofit.model.Event;
import org.nonprofit.model.EventType;
import org.nonprofit.patterns.observer.EventObserver;

public class ConsoleView {

    private final EventTypeController eventTypes;
    private final EventController events;
    private final Scanner input;

    private final Map<Long, EventObserver> testObservers = new HashMap<>();

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ConsoleView(EventTypeController eventTypes, EventController events,Scanner input){
        this.eventTypes = eventTypes;
        this.events = events;
        this.input = input;
    }

    // ==========================================
    // MAIN MENU
    // ==========================================

    public void start() {

        while (true) {

            System.out.println("\n========== FOODBRIDGE ==========");
            System.out.println("1. Event Type Management");
            System.out.println("2. Event Management");
            System.out.println("0. Exit");
            System.out.print("Choice: ");

            String choice = input.nextLine().trim();

            switch (choice) {

                case "1" -> eventTypeMenu();

                case "2" -> eventMenu();

                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }

                default -> System.out.println("Unknown option.");
            }
        }
    }

    // ==========================================
    // EVENT TYPE MENU
    // ==========================================

    private void eventTypeMenu() {

        while (true) {

            System.out.println("\n===== EVENT TYPE MANAGEMENT =====");
            System.out.println("1. Add Event Type");
            System.out.println("2. List Event Types");
            System.out.println("3. Rename Event Type");
            System.out.println("4. Delete Event Type");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            String choice = input.nextLine().trim();

            try {

                switch (choice) {

                    case "1" -> createEventType();

                    case "2" -> listEventTypes();

                    case "3" -> renameEventType();

                    case "4" -> deleteEventType();

                    case "0" -> {
                        return;
                    }

                    default -> System.out.println("Unknown option.");
                }

            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private void createEventType() throws SQLException {

        String name = askRequired("Event type name: ");

        long id = eventTypes.create(name);

        System.out.println("Created Event Type ID: " + id);
    }

    private void listEventTypes() throws SQLException {

        List<EventType> types = eventTypes.list();

        if (types.isEmpty()) {
            System.out.println("No event types found.");
            return;
        }

        System.out.println("\n--- Event Types ---");

        for (EventType type : types) {
            System.out.println(
                    type.getId() + " | " + type.getName()
            );
        }
    }

    private void renameEventType() throws SQLException {

        long id = askId();

        String name = askRequired("New name: ");

        boolean updated = eventTypes.rename(id, name);

        System.out.println(
                updated ? "Event type updated." : "Event type not found."
        );
    }

    private void deleteEventType() throws SQLException {

        long id = askId();

        boolean deleted = eventTypes.delete(id);

        System.out.println(
                deleted ? "Event type deleted." : "Event type not found."
        );
    }

    // ==========================================
    // EVENT MANAGEMENT MENU
    // ==========================================

    private void eventMenu() {

        while (true) {

            System.out.println("\n===== EVENT MANAGEMENT =====");
            System.out.println("1. Create Event");
            System.out.println("2. List All Events");
            System.out.println("3. Find Event by ID");
            System.out.println("4. Update Event");
            System.out.println("5. Cancel Event");
            System.out.println("6. Subscribe Observer");
            System.out.println("7. Unsubscribe Observer");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            String choice = input.nextLine().trim();

            try {

                switch (choice) {

                    case "1" -> createEvent();

                    case "2" -> listEvents();

                    case "3" -> findEvent();

                    case "4" -> updateEvent();

                    case "5" -> cancelEvent();

                    case "6" -> subscribeObserver();

                    case "7" -> unsubscribeObserver();

                    case "0" -> {
                        return;
                    }

                    default -> System.out.println("Unknown option.");
                }

            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // CREATE EVENT
    // ==========================================

    private void createEvent() throws SQLException {

        System.out.println("\n--- Create New Event ---");

        Event event = new Event();

        event.setTitle(askRequired("Title: "));

        event.setDescription(askString("Description: "));

        event.setDate(askDate());

        event.setLocation(askString("Location: "));

        System.out.println("\nAvailable Event Types:");
        listEventTypes();

        event.setEventTypeId(askLong("Event Type ID: "));

        long id = events.create(event);

        System.out.println("\nEvent created successfully!");
        System.out.println("Event ID: " + id);
    }

    // ==========================================
    // LIST EVENTS
    // ==========================================

    private void listEvents() throws SQLException {

        List<Event> eventList = events.list();

        if (eventList.isEmpty()) {
            System.out.println("No events found.");
            return;
        }

        System.out.println("\n--- All Events ---");

        for (Event event : eventList) {
            printEvent(event);
        }
    }

    // ==========================================
    // FIND EVENT
    // ==========================================

    private void findEvent() throws SQLException {

        long id = askId();

        Optional<Event> result = events.get(id);

        if (result.isEmpty()) {
            System.out.println("Event not found.");
            return;
        }

        printEvent(result.get());
    }

    // ==========================================
    // UPDATE EVENT
    // ==========================================

    private void updateEvent() throws SQLException {

        long id = askId();

        Optional<Event> result = events.get(id);

        if (result.isEmpty()) {
            System.out.println("Event not found.");
            return;
        }

        Event event = result.get();

        if ("CANCELLED".equals(event.getStatus())) {
            System.out.println("Cannot update a cancelled event.");
            return;
        }

        System.out.println("\nCurrent Event Details:");
        printEvent(event);

        System.out.println("\nEnter new values.");
        System.out.println("Leave blank to keep the current value.");

        // UPDATE TITLE

        String title = askString("New Title: ");

        if (!title.isBlank()) {
            event.setTitle(title);
        }

        // UPDATE DESCRIPTION

        String description = askString("New Description: ");

        if (!description.isBlank()) {
            event.setDescription(description);
        }

        // UPDATE LOCATION

        String location = askString("New Location: ");

        if (!location.isBlank()) {
            event.setLocation(location);
        }

        // UPDATE DATE

        String dateInput = askString(
                "New Date (yyyy-MM-dd HH:mm): "
        );

        if (!dateInput.isBlank()) {

            LocalDateTime newDate =
                    LocalDateTime.parse(dateInput, DATE_FORMAT);

            event.setDate(newDate);
        }

        // UPDATE EVENT TYPE

        String typeInput = askString("New Event Type ID: ");

        if (!typeInput.isBlank()) {
            event.setEventTypeId(Long.parseLong(typeInput));
        }

        // SAVE CHANGES

        boolean updated = events.update(event);

        if (updated) {
            System.out.println("Event updated successfully!");
        } else {
            System.out.println("Event could not be updated.");
        }
    }

    // ==========================================
    // CANCEL EVENT
    // ==========================================

    private void cancelEvent() throws SQLException {

        long id = askId();

        Optional<Event> result = events.get(id);

        if (result.isEmpty()) {
            System.out.println("Event not found.");
            return;
        }

        Event event = result.get();

        System.out.println("\nEvent to cancel:");
        printEvent(event);

        String confirm = askString(
                "Are you sure you want to cancel this event? (y/n): "
        );

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        boolean cancelled = events.cancelEvent(id);

        if (cancelled) {
            System.out.println("Event cancelled successfully!");
        } else {
            System.out.println(
                    "Event could not be cancelled (it may already be cancelled)."
            );
        }
    }

    // ==========================================
    // SUBSCRIBE OBSERVER
    // ==========================================

    private void subscribeObserver() throws SQLException {

        long eventId = askId();

        if (events.get(eventId).isEmpty()) {
            System.out.println("Event not found.");
            return;
        }

        if (testObservers.containsKey(eventId)) {
            System.out.println(
                    "Test observer is already subscribed to this event."
            );
            return;
        }

        EventObserver observer = (event, message) -> {

            System.out.println("\n========== OBSERVER NOTIFICATION ==========");
            System.out.println("Event ID: " + event.getId());
            System.out.println("Event: " + event.getTitle());
            System.out.println("Message: " + message);
            System.out.println("===========================================");
        };

        events.subscribe(eventId, observer);


        testObservers.put(eventId, observer);

        System.out.println(
                "Test observer subscribed to Event #" + eventId
        );
    }

    // ==========================================
    // UNSUBSCRIBE OBSERVER
    // ==========================================

    private void unsubscribeObserver() {

        long eventId = askId();

        EventObserver observer = testObservers.get(eventId);

        if (observer == null) {
            System.out.println(
                    "No test observer is subscribed to this event."
            );
            return;
        }

        events.unsubscribe(eventId, observer);

        testObservers.remove(eventId);

        System.out.println(
                "Test observer unsubscribed from Event #" + eventId
        );
    }

    // ==========================================
    // DISPLAY EVENT
    // ==========================================

    private void printEvent(Event event) {

        System.out.println("\n-------------------------------");

        System.out.println("ID: " + event.getId());

        System.out.println("Title: " + event.getTitle());

        System.out.println("Description: " + event.getDescription());

        System.out.println("Date: " + event.getDate());

        System.out.println("Location: " + event.getLocation());

        System.out.println("Event Type ID: " + event.getEventTypeId());

        System.out.println("Status: " + event.getStatus());

        System.out.println("-------------------------------");
    }

    // ==========================================
    // INPUT HELPERS
    // ==========================================

    private String askString(String message) {

        System.out.print(message);

        return input.nextLine().trim();
    }

    private String askRequired(String message) {

        String value = askString(message);

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "This field cannot be empty."
            );
        }

        return value;
    }

    private long askLong(String message) {

        String value = askRequired(message);

        return Long.parseLong(value);
    }

    private long askId() {

        return askLong("ID: ");
    }

    private LocalDateTime askDate() {

        String value = askRequired(
                "Date (yyyy-MM-dd HH:mm): "
        );

        return LocalDateTime.parse(value, DATE_FORMAT);
    }
}