package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.operations.Notification;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Notification (connection from DatabaseManager).
 * Table: notifications; status PENDING = unread, READ = read.
 */
public class NotificationRepository implements Repository<Notification> {
    private static final String SELECT_NOTIFICATIONS =
            "SELECT id, recipient_user_id, message, status, created_at FROM notifications";

    private DatabaseManager db;

    public NotificationRepository() {
        db = DatabaseManager.getInstance();
    }

    @Override
    public void add(Notification item) {
        String sql = "INSERT INTO notifications(recipient_user_id, message, status) VALUES (?, ?, ?) RETURNING id";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, item.getRecipientId());
            ps.setString(2, item.getMessage());
            ps.setString(3, item.isRead() ? Notification.READ : Notification.PENDING);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    item.setId(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save notification: " + e.getMessage(), e);
        }
    }

    @Override
    public Notification findById(int id) {
        List<Notification> found = query(" WHERE id = ?", id);
        return found.isEmpty() ? null : found.get(0);
    }

    /** Newest first. */
    public List<Notification> findByRecipient(int userId) {
        return query(" WHERE recipient_user_id = ? ORDER BY created_at DESC, id DESC", userId);
    }

    @Override
    public List<Notification> findAll() {
        return query(" ORDER BY created_at DESC, id DESC", 0);
    }

    /** Only the read flag can change. */
    @Override
    public void update(Notification item) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE notifications SET status = ? WHERE id = ?")) {
            ps.setString(1, item.isRead() ? Notification.READ : Notification.PENDING);
            ps.setInt(2, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update notification: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM notifications WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete notification: " + e.getMessage(), e);
        }
    }

    private List<Notification> query(String filter, int id) {
        List<Notification> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(SELECT_NOTIFICATIONS + filter)) {
            if (filter.contains("?")) {
                ps.setInt(1, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setRecipientId(rs.getInt("recipient_user_id"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(Notification.READ.equals(rs.getString("status")));
                    n.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class)
                            .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load notifications: " + e.getMessage(), e);
        }
        return list;
    }
}
