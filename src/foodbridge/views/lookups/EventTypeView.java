package foodbridge.views.lookups;

import java.util.Scanner;

import foodbridge.controllers.LookupController;
import foodbridge.models.lookups.EventType;
import foodbridge.views.View;

/**
 * Screen; talks only to LookupController.
 * Console menu for now (ported from the FoodBridgeColdStart branch).
 */
public class EventTypeView implements View {
    private LookupController lookupController;
    private Scanner input;

    public EventTypeView(LookupController lookupController) {
        this.lookupController = lookupController;
        this.input = new Scanner(System.in);
    }

    @Override
    public void show() {
        while (true) {
            System.out.println("\n=== FoodBridge - Event Types ===");
            System.out.println("1. Add event type\n2. List event types\n3. Rename event type\n4. Delete event type\n0. Exit");
            System.out.print("Choice: ");
            String choice = input.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Name: ");
                        EventType type = new EventType();
                        type.setName(input.nextLine());
                        lookupController.addEventType(type);
                        System.out.println("Created ID: " + type.getId());
                    }
                    case "2" -> {
                        for (EventType type : lookupController.listEventTypes()) {
                            System.out.println(type.getId() + " | " + type.getName());
                        }
                    }
                    case "3" -> {
                        int id = askId();
                        System.out.print("New name: ");
                        lookupController.updateEventType(new EventType(id, input.nextLine()));
                        System.out.println("Updated");
                    }
                    case "4" -> {
                        lookupController.deleteEventType(askId());
                        System.out.println("Deleted");
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Unknown option");
                }
            } catch (RuntimeException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private int askId() {
        System.out.print("ID: ");
        return Integer.parseInt(input.nextLine().trim());
    }
}
