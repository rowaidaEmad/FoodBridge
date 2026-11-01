package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Notification {
    private long id;
    private long recipientUserId;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    public Notification() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(long value) { this.recipientUserId = value; }
    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
