package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
//this model for a vol to register into an event
//event_id, vol_id

public class EventRegistration {

    private long id;
    private long eventId;
    private long volunteerId;
    private LocalDateTime registeredAt;
    private String attendanceStatus;

    public EventRegistration() {

    }
    //setters and getters
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getEventId() { return eventId; }
    public void setEventId(long value) { this.eventId = value; }
    public long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(long value) { this.volunteerId = value; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime value) { this.registeredAt = value; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String value) { this.attendanceStatus = value; }
}
