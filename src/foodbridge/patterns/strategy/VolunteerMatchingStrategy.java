package foodbridge.patterns.strategy;

import foodbridge.models.users.Volunteer;

/**
 * STRATEGY - Strategy. Scores how well a volunteer fits; a higher score ranks first.
 */
public interface VolunteerMatchingStrategy {
    int MATCH = 100;
    int NO_MATCH = 0;

    int score(Volunteer volunteer, MatchCriteria criteria);
}
