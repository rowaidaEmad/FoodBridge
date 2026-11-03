package foodbridge.models.operations;

import java.time.LocalDateTime;

import foodbridge.models.users.Volunteer;

/**
 * Core domain. A Volunteer registered for an Event.
 */
public class EventRegistration {
    public static final String REGISTERED = "REGISTERED";

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

    public int getId() {
        return id;
    }

    /** Set by EventRegistrationRepository when the registration is saved or loaded. */
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

    /** e.g. REGISTERED, ATTENDED, ABSENT. */
    public void updateAttendance(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Attendance status is required");
        }
        this.attendanceStatus = status.trim().toUpperCase();
    }
}
