package foodbridge.views.lookups;

import java.util.ArrayList;
import java.util.List;

import foodbridge.controllers.LookupController;
import foodbridge.models.lookups.EventType;
import foodbridge.views.View;

/**
 * Screen; talks only to LookupController. Admin CRUD on event types.
 */
public class EventTypeView implements View {
    private LookupController lookupController;

    public EventTypeView(LookupController lookupController) {
        this.lookupController = lookupController;
    }

    @Override
    public void show() {
        new LookupTableDialog("Event types", "event type",
                () -> {
                    List<Object[]> rows = new ArrayList<>();
                    for (EventType t : lookupController.listEventTypes()) {
                        rows.add(new Object[] { t.getId(), t.getName() });
                    }
                    return rows;
                },
                name -> {
                    EventType type = new EventType();
                    type.setName(name);
                    lookupController.addEventType(type);
                },
                (id, name) -> lookupController.updateEventType(new EventType(id, name)),
                id -> lookupController.deleteEventType(id)).show();
    }
}
