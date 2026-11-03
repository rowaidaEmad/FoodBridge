package foodbridge.models.operations;

import java.time.LocalDateTime;

import foodbridge.models.lookups.EventType;

/**
 * Core domain. Of one EventType; has 0..* EventRegistration.
 * status (SCHEDULED / CANCELLED) comes from the FoodBridgeColdStart branch: cancelled events can't be edited.
 */
public class Event {
    public static final String SCHEDULED = "SCHEDULED";
    public static final String CANCELLED = "CANCELLED";

    private int id;
    private String title;
    private String description;
    private LocalDateTime date;
    private String location;
    private EventType type;
    private String status = SCHEDULED;

    public Event() {
    }

    public int getId() {
        return id;
    }

    /** Set by EventRepository when the event is saved or loaded. */
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Event title is required");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null || description.isBlank() ? null : description.trim();
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void reschedule(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null || location.isBlank() ? null : location.trim();
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        if (type == null) {
            throw new IllegalArgumentException("Event type is required");
        }
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    /** Set by EventRepository when loading. */
    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCancelled() {
        return CANCELLED.equals(status);
    }

    @Override
    public String toString() {
        return "#" + id + " - " + title;
    }
}
