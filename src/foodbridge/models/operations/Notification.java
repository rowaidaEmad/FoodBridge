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
        // TODO: implement
    }

    public void markAsRead() {
        // TODO: implement
    }

    public boolean isRead() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
