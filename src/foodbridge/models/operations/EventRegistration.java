package foodbridge.models.operations;

import java.time.LocalDateTime;

import foodbridge.models.users.Volunteer;

/**
 * Core domain. A Volunteer registered for an Event.
 */
public class EventRegistration {
    private int id;
    private Event event;
    private Volunteer volunteer;
    private LocalDateTime registeredAt;
    private String attendanceStatus;

    public EventRegistration() {
        // TODO: implement
    }

    public LocalDateTime getRegisteredAt() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateAttendance(String status) {
        // TODO: implement
    }
}
