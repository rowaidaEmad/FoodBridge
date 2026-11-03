package foodbridge.models.operations;

import java.time.LocalDateTime;

/**
 * Core domain. Sent to one User (recipientId).
 */
public class Notification {
    private int id;
    private int recipientId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(int recipientId, String message) {
        this.recipientId = recipientId;
        this.message = message;
    }

    public void markAsRead() {
        read = true;
    }

    public boolean isRead() {
        return read;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
