package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Event {
    private long id;
    private String title;
    private String description;
    private LocalDateTime date;
    private String location;
    private long eventTypeId;
    private String status = "SCHEDULED";
    public Event() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime value) { this.date = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }
    public long getEventTypeId() { return eventTypeId; }
    public void setEventTypeId(long value) { this.eventTypeId = value; }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
