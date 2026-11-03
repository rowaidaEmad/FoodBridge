package foodbridge.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import foodbridge.models.operations.AchievementCertificate;
import foodbridge.models.operations.Certificate;
import foodbridge.models.operations.ParticipationCertificate;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Certificate (connection from DatabaseManager).
 */
public class CertificateRepository {
    private final DatabaseManager db;

    public CertificateRepository() {
        db = DatabaseManager.getInstance();
    }

    public void add(Certificate certificate) {
        String sql = "INSERT INTO certificates(volunteer_id, certificate_number, certificate_type, hours_at_issue, issued_at)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, certificate.getVolunteer().getId());
            ps.setString(2, certificate.getCertificateNumber());
            ps.setString(3, certificate.getType());
            ps.setDouble(4, certificate.getHoursAtIssue());
            ps.setTimestamp(5, JdbcSupport.toTimestamp(certificate.getIssuedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    certificate.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save certificate: " + e.getMessage(), e);
        }
    }

    public Certificate findByVolunteerAndType(int volunteerId, String type) {
        String sql = "SELECT id, certificate_number, hours_at_issue, issued_at FROM certificates"
                + " WHERE volunteer_id = ? AND certificate_type = ? ORDER BY issued_at DESC LIMIT 1";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, volunteerId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs, type) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load certificate: " + e.getMessage(), e);
        }
    }

    private Certificate map(ResultSet rs, String type) throws SQLException {
        Certificate certificate = AchievementCertificate.TYPE.equals(type)
                ? new AchievementCertificate()
                : new ParticipationCertificate();
        certificate.setId(rs.getInt("id"));
        certificate.setCertificateNumber(rs.getString("certificate_number"));
        certificate.setHoursAtIssue(rs.getDouble("hours_at_issue"));
        certificate.setIssuedAt(JdbcSupport.getDateTime(rs, "issued_at"));
        return certificate;
    }
}
