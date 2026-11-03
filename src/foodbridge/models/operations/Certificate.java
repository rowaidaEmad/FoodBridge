package foodbridge.models.operations;

import java.time.LocalDateTime;

import foodbridge.models.users.Volunteer;

/**
 * FACTORY METHOD - Product. Awarded to a Volunteer for their hours.
 */
public abstract class Certificate {
    private int id;
    private Volunteer volunteer;
    private String certificateNumber;
    private double hoursAtIssue;
    private LocalDateTime issuedAt;

    public abstract String getType();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public double getHoursAtIssue() {
        return hoursAtIssue;
    }

    public void setHoursAtIssue(double hoursAtIssue) {
        this.hoursAtIssue = hoursAtIssue;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
