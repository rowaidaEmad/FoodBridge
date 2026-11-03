package foodbridge.models.operations;

import java.time.LocalDateTime;

import foodbridge.models.lookups.EventType;

/**
 * Core domain. Of one EventType; has 0..* EventRegistration.
 */
public class Event {
    private int id;
    private String title;
    private String description;
    private LocalDateTime date;
    private String location;
    private EventType type;

    public Event() {
        // TODO: implement
    }

    public String getTitle() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void reschedule(LocalDateTime date) {
        // TODO: implement
    }

    public void setLocation(String location) {
        // TODO: implement
    }
}
