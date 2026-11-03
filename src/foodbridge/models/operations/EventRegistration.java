package foodbridge.models.operations;

import java.time.LocalDateTime;
import java.util.Set;

import foodbridge.models.users.Volunteer;

/**
 * Core domain. A Volunteer registered for an Event.
 */
public class EventRegistration {
    public static final String REGISTERED = "REGISTERED";
    public static final String ATTENDED = "ATTENDED";
    public static final String ABSENT = "ABSENT";
    private static final Set<String> VALID_STATUSES = Set.of(REGISTERED, ATTENDED, ABSENT);

    private int id;
    private Event event;
    private Volunteer volunteer;
    private LocalDateTime registeredAt;
    private String attendanceStatus = REGISTERED;

    public EventRegistration() {
    }

    public EventRegistration(Event event, Volunteer volunteer) {
        if (event == null || volunteer == null) {
            throw new IllegalArgumentException("Event and volunteer are required");
        }
        this.event = event;
        this.volunteer = volunteer;
        this.registeredAt = LocalDateTime.now();
    }

    public void updateAttendance(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unknown attendance status: " + status);
        }
        this.attendanceStatus = normalized;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}
