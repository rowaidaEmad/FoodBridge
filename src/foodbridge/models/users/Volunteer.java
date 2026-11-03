package foodbridge.models.users;

/**
 * Core domain. Performs pickup tasks, delivers distributions, registers for events.
 */
public class Volunteer extends User {
    private String availability;
    private boolean hasTransport;
    private double totalHours;

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
}
