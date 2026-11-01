package org.nonprofit.patterns.observer;


import org.nonprofit.model.Event;
public class VolunteerObserver implements EventObserver {
    private final long volunteerId;
    public VolunteerObserver(long volunteerId) {this.volunteerId=volunteerId;}
    public void update(Event event,String message) {System.out.println("Volunteer "+volunteerId+", event "+event.getId()+": "+message);}
}

