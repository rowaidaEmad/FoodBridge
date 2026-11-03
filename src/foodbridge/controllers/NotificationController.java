package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.Notification;
import foodbridge.repositories.NotificationRepository;

/**
 * A user's notifications.
 */
public class NotificationController {
    private final NotificationRepository notificationRepository = new NotificationRepository();

    public List<Notification> listForUser(int userId) {
        return notificationRepository.findByRecipient(userId);
    }

    public void markAsRead(int notificationId) {
        notificationRepository.markRead(notificationId);
    }
}
