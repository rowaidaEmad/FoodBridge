package foodbridge.services;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import foodbridge.models.operations.VolunteerHours;
import foodbridge.models.users.Volunteer;
import foodbridge.repositories.VolunteerRepository;

/**
 * Volunteer profiles and hours.
 */
public class VolunteerService {
    private final VolunteerRepository volunteerRepository = new VolunteerRepository();

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

    private List<String> cleanValues(List<String> values) {
        TreeSet<String> unique = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        values.stream().map(String::trim).filter(value -> !value.isEmpty()).forEach(unique::add);
        return new ArrayList<>(unique);
    }
}
