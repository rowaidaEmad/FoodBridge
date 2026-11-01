package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.Notification;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class NotificationRepository {
    private final DatabaseManager db;
    public NotificationRepository(DatabaseManager db) {this.db=db;}
    public long save(Notification value) throws SQLException {throw new UnsupportedOperationException("TODO: NotificationRepository.save JDBC");}
    public Optional<Notification> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: NotificationRepository.findById JDBC");}
    public List<Notification> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: NotificationRepository.findAll JDBC");}
}

