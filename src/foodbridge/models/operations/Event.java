package foodbridge.models.operations;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import foodbridge.models.users.Volunteer;

public class Event {
    private int id;
    private String title;
    private LocalDate date;
    private String location;
    private List<Volunteer> volunteers = new ArrayList<>();
    private List<Volunteer> attendees = new ArrayList<>();

    public void register(Volunteer v) {
        // TODO
    }

    public void markAttendance(Volunteer v) {
        // TODO
    }
}
