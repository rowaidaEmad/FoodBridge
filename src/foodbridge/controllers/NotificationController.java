package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.Notification;
import foodbridge.repositories.NotificationRepository;

/**
 * A user's notifications.
 */
public class NotificationController {
    private NotificationRepository notificationRepository;

    public NotificationController() {
        // TODO: implement
    }

    public List<Notification> listForUser(int userId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAsRead(int notificationId) {
        // TODO: implement
    }
}
