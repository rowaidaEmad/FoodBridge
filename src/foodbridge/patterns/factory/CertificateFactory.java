package foodbridge.patterns.factory;

import java.time.LocalDateTime;

import foodbridge.models.operations.Certificate;
import foodbridge.models.users.Volunteer;

/**
 * FACTORY METHOD - Creator.
 */
public abstract class CertificateFactory {
    public Certificate issue(Volunteer volunteer) {
        Certificate certificate = createCertificate();
        certificate.setVolunteer(volunteer);
        certificate.setHoursAtIssue(volunteer.getTotalHours());
        certificate.setCertificateNumber("FB-" + volunteer.getId() + "-" + System.currentTimeMillis());
        certificate.setIssuedAt(LocalDateTime.now());
        return certificate;
    }

    protected abstract Certificate createCertificate();
}
