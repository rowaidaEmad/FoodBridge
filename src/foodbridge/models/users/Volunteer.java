package foodbridge.models.users;

/**
 * Core domain. Performs pickup tasks, delivers distributions, registers for events.
 */
public class Volunteer extends User {
    private String availability;
    private boolean hasTransport;
    private double totalHours;

    public Volunteer() {
        // TODO: implement
    }

    public void setAvailability(String value) {
        // TODO: implement
    }

    public void setHasTransport(boolean flag) {
        // TODO: implement
    }

    public void addHours(double hours) {
        // TODO: implement
    }

    public double getTotalHours() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
