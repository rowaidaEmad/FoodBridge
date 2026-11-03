package foodbridge.patterns.decorator;

import java.time.format.DateTimeFormatter;

import foodbridge.models.operations.AchievementCertificate;
import foodbridge.models.operations.Certificate;

/**
 * DECORATOR - ConcreteComponent. Formats a Certificate.
 */
public class BasicCertificate implements CertificateComponent {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Certificate certificate;

    public BasicCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    @Override
    public String generate() {
        return title() + "\n\n"
                + "Awarded to " + certificate.getVolunteer().getName() + "\n"
                + String.format("for %.2f hours of volunteering with FoodBridge%n%n", certificate.getHoursAtIssue())
                + "Certificate number: " + certificate.getCertificateNumber() + "\n"
                + "Issued: " + certificate.getIssuedAt().format(DATE_FORMAT);
    }

    private String title() {
        return AchievementCertificate.TYPE.equals(certificate.getType())
                ? "CERTIFICATE OF ACHIEVEMENT"
                : "CERTIFICATE OF PARTICIPATION";
    }
}
