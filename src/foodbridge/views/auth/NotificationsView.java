package foodbridge.views.auth;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import foodbridge.controllers.NotificationController;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;

/**
 * Screen; talks only to NotificationController.
 */
public class NotificationsView implements View {
    private static final int MESSAGE_COLUMN_WIDTH = 400;

    private final int userId;
    private final NotificationController notificationController;
    private final JFrame frame = new JFrame("FoodBridge - Notifications");
    private final JTable table = SwingSupport.readOnlyTable("ID", "Message", "Status", "Date");

    public NotificationsView(int userId, NotificationController notificationController) {
        this.userId = userId;
        this.notificationController = notificationController;

        table.getColumnModel().getColumn(1).setPreferredWidth(MESSAGE_COLUMN_WIDTH);

        JButton readButton = new JButton("Mark as Read");
        readButton.addActionListener(e -> markAsRead());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(readButton);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(SwingSupport.scroll(table, 620, 280), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        SwingSupport.attempt(frame, () -> {
            refresh();
            frame.setVisible(true);
        });
    }

    private void markAsRead() {
        SwingSupport.attempt(frame, () -> {
            notificationController.markAsRead(SwingSupport.selectedId(table));
            refresh();
        });
    }

    private void refresh() {
        SwingSupport.setRows(table, rows());
    }

    private List<Object[]> rows() {
        return notificationController.listForUser(userId).stream()
                .map(notification -> new Object[] { notification.getId(), notification.getMessage(),
                        notification.isRead() ? "Read" : "Unread", SwingSupport.formatDateTime(notification.getCreatedAt()) })
                .toList();
    }
}
