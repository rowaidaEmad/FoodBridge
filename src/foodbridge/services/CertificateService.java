package foodbridge.services;

import java.util.List;

import foodbridge.models.operations.Certificate;
import foodbridge.models.operations.VolunteerHours;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.decorator.BasicCertificate;
import foodbridge.patterns.decorator.CertificateComponent;
import foodbridge.patterns.decorator.HoursBreakdownDecorator;
import foodbridge.patterns.decorator.SignatureDecorator;
import foodbridge.patterns.factory.AchievementCertificateFactory;
import foodbridge.patterns.factory.CertificateFactory;
import foodbridge.patterns.factory.ParticipationCertificateFactory;
import foodbridge.repositories.CertificateRepository;
import foodbridge.repositories.VolunteerRepository;

/**
 * FACTORY METHOD and DECORATOR - Client. Issues certificates and builds their text.
 */
public class CertificateService {
    private static final double PARTICIPATION_HOURS = 10;
    private static final double ACHIEVEMENT_HOURS = 50;

    private final VolunteerRepository volunteerRepository = new VolunteerRepository();
    private final CertificateRepository certificateRepository = new CertificateRepository();

    public String requestCertificate(int volunteerId) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId);
        if (volunteer == null) {
            throw new IllegalArgumentException("No volunteer with id " + volunteerId);
        }
        double hours = volunteer.getTotalHours();
        if (hours < PARTICIPATION_HOURS) {
            throw new IllegalArgumentException(
                    String.format("You need %.2f more hours for a certificate.", PARTICIPATION_HOURS - hours));
        }
        Certificate certificate = findOrIssue(factoryFor(hours), volunteer);
        CertificateComponent text = new SignatureDecorator(
                new HoursBreakdownDecorator(new BasicCertificate(certificate), hoursAtIssue(certificate)));
        return text.generate();
    }

    private CertificateFactory factoryFor(double hours) {
        return hours >= ACHIEVEMENT_HOURS ? new AchievementCertificateFactory() : new ParticipationCertificateFactory();
    }

    private Certificate findOrIssue(CertificateFactory factory, Volunteer volunteer) {
        Certificate issued = factory.issue(volunteer);
        Certificate existing = certificateRepository.findByVolunteerAndType(volunteer.getId(), issued.getType());
        if (existing != null) {
            existing.setVolunteer(volunteer);
            return existing;
        }
        certificateRepository.add(issued);
        return issued;
    }

    private List<VolunteerHours> hoursAtIssue(Certificate certificate) {
        return volunteerRepository.findHours(certificate.getVolunteer().getId()).stream()
                .filter(entry -> !entry.getLoggedAt().isAfter(certificate.getIssuedAt()))
                .toList();
    }
}
