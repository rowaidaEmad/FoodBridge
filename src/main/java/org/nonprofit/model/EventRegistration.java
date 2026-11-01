package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class EventRegistration {
    private long id;
    private long eventId;
    private long volunteerId;
    private LocalDateTime registeredAt;
    private String attendanceStatus;
    public EventRegistration() { }
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
