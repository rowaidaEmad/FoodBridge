package foodbridge.views.auth;

import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.NotificationController;
import foodbridge.models.operations.Notification;
import foodbridge.models.users.User;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to NotificationController. The logged-in user's notifications, newest first.
 */
public class NotificationsView extends ModuleView {
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationController notificationController;
    private final User user;
    private final DefaultTableModel rows = tableModel("ID", "Date", "Message", "Status");
    private final JTable table = table(rows);

    public NotificationsView(NotificationController notificationController, User user) {
        super("FoodBridge - Notifications", 800, 480);
        this.notificationController = notificationController;
        this.user = user;
        table.getColumnModel().getColumn(2).setPreferredWidth(450);

        JPanel actions = new JPanel();
        actions.add(button("Mark selected as read", this::markRead));
        actions.add(button("Refresh", this::refresh));
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
    }

    @Override
    protected void refresh() {
        task(() -> notificationController.listForUser(user.getId()), list -> {
            rows.setRowCount(0);
            for (Notification n : list) {
                rows.addRow(new Object[] { n.getId(), n.getCreatedAt().format(WHEN), n.getMessage(),
                        n.isRead() ? "Read" : "New" });
            }
        });
    }

    private void markRead() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select a notification first.");
        }
        int id = ((Number) table.getValueAt(row, 0)).intValue();
        task(() -> {
            notificationController.markAsRead(id);
            return true;
        }, ok -> refresh());
    }
}
