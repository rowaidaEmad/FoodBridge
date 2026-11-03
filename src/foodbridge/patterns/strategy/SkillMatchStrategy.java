package foodbridge.patterns.strategy;

import foodbridge.models.users.Volunteer;

/**
 * STRATEGY - ConcreteStrategy. Prefers volunteers who have the required skill.
 */
public class SkillMatchStrategy implements VolunteerMatchingStrategy {
    @Override
    public int score(Volunteer volunteer, MatchCriteria criteria) {
        String skill = criteria.getRequiredSkill();
        return skill == null || volunteer.hasSkill(skill) ? MATCH : NO_MATCH;
    }
}
