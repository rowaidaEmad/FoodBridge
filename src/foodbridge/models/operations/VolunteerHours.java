package foodbridge.models.operations;

import java.time.LocalDateTime;

/**
 * Core domain. One entry in a Volunteer's hours history.
 */
public class VolunteerHours {
    public static final String PICKUP = "PICKUP";
    public static final String EVENT = "EVENT";
    public static final String MANUAL = "MANUAL";

    private int id;
    private int volunteerId;
    private double hours;
    private String source;
    private String note;
    private LocalDateTime loggedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
