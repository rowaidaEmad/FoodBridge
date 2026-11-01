package org.nonprofit.view;


import java.util.Scanner;
import org.nonprofit.controller.EventTypeController;
import org.nonprofit.model.EventType;
public class ConsoleView {
    private final EventTypeController events;
    private final Scanner input;
    public ConsoleView(EventTypeController events, Scanner input) { this.events=events;this.input=input; }
    public void start() {
        while(true) {
            System.out.println("\n=== FoodBridge (Phase 1 starter) ===");
            System.out.println("1. Add event type\n2. List event types\n3. Rename event type\n4. Delete event type\n0. Exit");
            System.out.print("Choice: ");
            String choice=input.nextLine().trim();
            try {
                switch(choice) {
                    case "1" -> { System.out.print("Name: "); System.out.println("Created ID: " + events.create(input.nextLine())); }
                    case "2" -> { for(EventType e:events.list())System.out.println(e.getId()+" | "+e.getName()); }
                    case "3" -> { long id=askId(); System.out.print("New name: ");System.out.println(events.rename(id,input.nextLine())?"Updated":"Not found"); }
                    case "4" -> System.out.println(events.delete(askId())?"Deleted":"Not found");
                    case "0" -> { return; }
                    default -> System.out.println("Unknown option");
                }
            } catch(Exception e) { System.out.println("Operation failed: "+e.getMessage()); }
        }
    }
    private long askId() {System.out.print("ID: ");return Long.parseLong(input.nextLine().trim());}
}

