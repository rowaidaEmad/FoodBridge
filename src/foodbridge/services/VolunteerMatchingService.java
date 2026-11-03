package foodbridge.services;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import foodbridge.models.operations.Event;
import foodbridge.models.operations.PickupTask;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.strategy.AvailabilityMatchStrategy;
import foodbridge.patterns.strategy.MatchCriteria;
import foodbridge.patterns.strategy.PreferenceMatchStrategy;
import foodbridge.patterns.strategy.SkillMatchStrategy;
import foodbridge.patterns.strategy.VolunteerMatchingStrategy;
import foodbridge.repositories.EventRepository;
import foodbridge.repositories.PickupTaskRepository;
import foodbridge.repositories.VolunteerRepository;

/**
 * STRATEGY - Context. Ranks volunteers for a task or event with the selected strategy.
 */
public class VolunteerMatchingService {
    private static final String PICKUP_ACTIVITY = "Pickup";

    private final Map<String, VolunteerMatchingStrategy> strategies = new LinkedHashMap<>();
    private final VolunteerRepository volunteerRepository = new VolunteerRepository();
    private final PickupTaskRepository pickupTaskRepository = new PickupTaskRepository();
    private final EventRepository eventRepository = new EventRepository();
    private VolunteerMatchingStrategy strategy;

    public VolunteerMatchingService() {
        strategies.put("Skill", new SkillMatchStrategy());
        strategies.put("Availability", new AvailabilityMatchStrategy());
        strategies.put("Preference", new PreferenceMatchStrategy());
        strategy = strategies.get("Skill");
    }

    public List<String> strategyNames() {
        return new ArrayList<>(strategies.keySet());
    }

    public void setStrategy(String name) {
        VolunteerMatchingStrategy selected = strategies.get(name);
        if (selected == null) {
            throw new IllegalArgumentException("Unknown strategy: " + name);
        }
        strategy = selected;
    }

    public List<Volunteer> rankForTask(int taskId) {
        PickupTask task = pickupTaskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("No task with id " + taskId);
        }
        return rank(new MatchCriteria(PICKUP_ACTIVITY, task.getRequiredSkill(), task.getDonation().getPickupWindow(), true));
    }

    public List<Volunteer> rankForEvent(int eventId) {
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("No event with id " + eventId);
        }
        String skill = volunteerRepository.findEventRequiredSkill(eventId);
        return rank(new MatchCriteria(event.getType().getName(), skill, partOfWeek(event.getDate()), false));
    }

    private List<Volunteer> rank(MatchCriteria criteria) {
        return volunteerRepository.findAllActive().stream()
                .filter(volunteer -> !criteria.needsTransport() || volunteer.hasTransport())
                .sorted(Comparator.comparingInt((Volunteer volunteer) -> strategy.score(volunteer, criteria)).reversed())
                .toList();
    }

    private String partOfWeek(LocalDateTime date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY ? "weekends" : "weekdays";
    }
}
