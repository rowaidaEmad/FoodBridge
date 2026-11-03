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
        notificationRepository = new NotificationRepository();
    }

    public List<Notification> listForUser(int userId) {
        return notificationRepository.findByRecipient(userId);
    }

    public void markAsRead(int notificationId) {
        Notification notification = notificationRepository.findById(notificationId);
        if (notification == null) {
            throw new IllegalArgumentException("No notification with id " + notificationId);
        }
        notification.markAsRead();
        notificationRepository.update(notification);
    }
}
