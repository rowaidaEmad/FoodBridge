package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.operations.Notification;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Notification (connection from DatabaseManager).
 */
public class NotificationRepository implements Repository<Notification> {
    private static final String STATUS_UNREAD = "PENDING";
    private static final String STATUS_READ = "READ";
    private static final String SELECT_NOTIFICATIONS =
            "SELECT id, recipient_user_id, message, status, created_at FROM notifications";

    private final DatabaseManager db;

    public NotificationRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Notification item) {
        String sql = "INSERT INTO notifications(recipient_user_id, message, status) VALUES (?, ?, ?)";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getRecipientId());
            ps.setString(2, item.getMessage());
            ps.setString(3, statusOf(item));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save notification: " + e.getMessage(), e);
        }
    }

    @Override
    public Notification findById(int id) {
        List<Notification> notifications = query(" WHERE id = ?", id);
        return notifications.isEmpty() ? null : notifications.get(0);
    }

    @Override
    public List<Notification> findAll() {
        return query(" ORDER BY created_at DESC, id DESC");
    }

    public List<Notification> findByRecipient(int userId) {
        return query(" WHERE recipient_user_id = ? ORDER BY created_at DESC, id DESC", userId);
    }

    @Override
    public void update(Notification item) {
        String sql = "UPDATE notifications SET message = ?, status = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getMessage());
            ps.setString(2, statusOf(item));
            ps.setInt(3, item.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No notification with id " + item.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update notification: " + e.getMessage(), e);
        }
    }

    public void markRead(int id) {
        Notification notification = findById(id);
        if (notification == null) {
            throw new IllegalArgumentException("No notification with id " + id);
        }
        notification.markAsRead();
        update(notification);
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM notifications WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No notification with id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete notification: " + e.getMessage(), e);
        }
    }

    private List<Notification> query(String clause, int... params) {
        List<Notification> notifications = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_NOTIFICATIONS + clause)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load notifications: " + e.getMessage(), e);
        }
        return notifications;
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification notification = new Notification(rs.getInt("recipient_user_id"), rs.getString("message"));
        notification.setId(rs.getInt("id"));
        notification.setRead(STATUS_READ.equals(rs.getString("status")));
        notification.setCreatedAt(JdbcSupport.getDateTime(rs, "created_at"));
        return notification;
    }

    private String statusOf(Notification notification) {
        return notification.isRead() ? STATUS_READ : STATUS_UNREAD;
    }
}
