package foodbridge;

import foodbridge.controllers.LookupController;
import foodbridge.views.lookups.EventTypeView;

/**
 * Entry point. Wire the views, controllers and services here once modules are implemented.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("FoodBridge started");
        // TODO: show LoginView and route to the right panel by role.
        // Until then, open the Event Types screen (the first module wired to the database).
        new EventTypeView(new LookupController()).show();
    }
}
