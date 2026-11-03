package foodbridge.models.users;

import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.Area;

/**
 * Core domain. Performs pickup tasks, delivers distributions, registers for events.
 */
public class Volunteer extends User {
    private String availability;
    private boolean hasTransport;
    private double totalHours;
    private List<String> skills = new ArrayList<>();
    private List<String> preferences = new ArrayList<>();

    public Volunteer() {
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String value) {
        this.availability = value;
    }

    public boolean hasTransport() {
        return hasTransport;
    }

    public void setHasTransport(boolean flag) {
        this.hasTransport = flag;
    }

    public void addHours(double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }
        totalHours += hours;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = new ArrayList<>(skills);
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = new ArrayList<>(preferences);
    }

    public boolean hasSkill(String skill) {
        return containsIgnoreCase(skills, skill);
    }

    public boolean prefers(String activity) {
        return containsIgnoreCase(preferences, activity);
    }

    private static boolean containsIgnoreCase(List<String> values, String target) {
        return values.stream().anyMatch(value -> value.equalsIgnoreCase(target));
    }
}
