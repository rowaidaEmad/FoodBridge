package foodbridge.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import foodbridge.models.operations.EventRegistration;
import foodbridge.models.operations.VolunteerHours;
import foodbridge.models.users.Volunteer;
import foodbridge.repositories.EventRegistrationRepository;
import foodbridge.repositories.VolunteerRepository;

/**
 * Volunteer profiles, hours and event attendance.
 */
public class VolunteerService {
    public static final String ATTENDED = "ATTENDED";
    public static final String ABSENT = "ABSENT";

    private final VolunteerRepository volunteerRepository = new VolunteerRepository();
    private final EventRegistrationRepository registrationRepository = new EventRegistrationRepository();

    public Volunteer getVolunteer(int id) {
        Volunteer volunteer = volunteerRepository.findById(id);
        if (volunteer == null) {
            throw new IllegalArgumentException("No volunteer with id " + id);
        }
        return volunteer;
    }

    public List<Volunteer> listVolunteers() {
        return volunteerRepository.findAllActive();
    }

    public void updateProfile(int id, String availability, boolean hasTransport, Integer areaId, List<String> skills,
            List<String> preferences) {
        volunteerRepository.updateProfile(id, availability, hasTransport, areaId, cleanValues(skills),
                cleanValues(preferences));
    }

    public void logHours(int volunteerId, double hours, String note) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be more than 0.");
        }
        volunteerRepository.addHours(volunteerId, hours, VolunteerHours.MANUAL, note);
    }

    public List<VolunteerHours> viewHours(int volunteerId) {
        return volunteerRepository.findHours(volunteerId);
    }

    /** Attended hours are credited once; a registration already marked attended is left unchanged. */
    public void markAttendance(int registrationId, String status, double hours) {
        if (!Set.of(EventRegistration.REGISTERED, ATTENDED, ABSENT).contains(status)) {
            throw new IllegalArgumentException("Unknown attendance status: " + status);
        }
        EventRegistration registration = registrationRepository.findById(registrationId);
        if (registration == null) {
            throw new IllegalArgumentException("No registration with id " + registrationId);
        }
        if (ATTENDED.equals(registration.getAttendanceStatus())) {
            return;
        }
        if (!ATTENDED.equals(status)) {
            registration.updateAttendance(status);
            registrationRepository.update(registration);
            return;
        }
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be more than 0.");
        }
        volunteerRepository.saveAttended(registrationId, status, registration.getVolunteer().getId(), hours,
                registration.getEvent().getTitle());
    }

    /** Throws if the event needs a skill this volunteer does not have. */
    public void requireEventSkill(int eventId, int volunteerId) {
        String skill = volunteerRepository.findEventRequiredSkill(eventId);
        Volunteer volunteer = getVolunteer(volunteerId);
        if (skill != null && !volunteer.hasSkill(skill)) {
            throw new IllegalArgumentException(volunteer.getName() + " does not have the required skill " + skill + ".");
        }
    }

    private List<String> cleanValues(List<String> values) {
        TreeSet<String> unique = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        values.stream().map(String::trim).filter(value -> !value.isEmpty()).forEach(unique::add);
        return new ArrayList<>(unique);
    }
}
