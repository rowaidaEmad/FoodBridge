package foodbridge.patterns.strategy;

import foodbridge.models.users.Volunteer;

/**
 * STRATEGY - ConcreteStrategy. Prefers volunteers who chose this kind of activity.
 */
public class PreferenceMatchStrategy implements VolunteerMatchingStrategy {
    @Override
    public int score(Volunteer volunteer, MatchCriteria criteria) {
        return volunteer.prefers(criteria.getActivity()) ? MATCH : NO_MATCH;
    }
}
