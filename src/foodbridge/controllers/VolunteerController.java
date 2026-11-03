package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.VolunteerHours;
import foodbridge.models.users.Volunteer;
import foodbridge.services.CertificateService;
import foodbridge.services.VolunteerMatchingService;
import foodbridge.services.VolunteerService;

/**
 * Volunteer profiles, hours, certificates and ranking.
 */
public class VolunteerController {
    private final VolunteerService volunteerService = new VolunteerService();
    private final CertificateService certificateService = new CertificateService();
    private final VolunteerMatchingService matchingService = new VolunteerMatchingService();

    public Volunteer getVolunteer(int id) {
        return volunteerService.getVolunteer(id);
    }

    public List<Volunteer> listVolunteers() {
        return volunteerService.listVolunteers();
    }

    public void updateProfile(int id, String availability, boolean hasTransport, List<String> skills,
            List<String> preferences) {
        volunteerService.updateProfile(id, availability, hasTransport, skills, preferences);
    }

    public void logHours(int volunteerId, double hours, String note) {
        volunteerService.logHours(volunteerId, hours, note);
    }

    public List<VolunteerHours> viewHours(int volunteerId) {
        return volunteerService.viewHours(volunteerId);
    }

    public String requestCertificate(int volunteerId) {
        return certificateService.requestCertificate(volunteerId);
    }

    public List<String> strategyNames() {
        return matchingService.strategyNames();
    }

    public List<Volunteer> rankForTask(int taskId, String strategyName) {
        matchingService.setStrategy(strategyName);
        return matchingService.rankForTask(taskId);
    }

    public List<Volunteer> rankForEvent(int eventId, String strategyName) {
        matchingService.setStrategy(strategyName);
        return matchingService.rankForEvent(eventId);
    }
}
