package foodbridge.patterns.strategy;

import foodbridge.models.users.Volunteer;

/**
 * STRATEGY - ConcreteStrategy. Prefers volunteers who are free at the needed time.
 */
public class AvailabilityMatchStrategy implements VolunteerMatchingStrategy {
    @Override
    public int score(Volunteer volunteer, MatchCriteria criteria) {
        String availability = volunteer.getAvailability();
        String keyword = criteria.getAvailabilityKeyword();
        if (availability == null || keyword == null) {
            return NO_MATCH;
        }
        return availability.toLowerCase().contains(keyword.toLowerCase()) ? MATCH : NO_MATCH;
    }
}
