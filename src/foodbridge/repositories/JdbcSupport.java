package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import foodbridge.patterns.singleton.DatabaseManager;

/**
 * JDBC helpers shared by the repositories.
 */
final class JdbcSupport {
    @FunctionalInterface
    interface SqlWork {
        void run(Connection connection) throws SQLException;
    }

    private JdbcSupport() {
    }

    static void inTransaction(DatabaseManager db, String failureMessage, SqlWork work) {
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                work.run(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(failureMessage + ": " + e.getMessage(), e);
        }
    }

    static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    static Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
