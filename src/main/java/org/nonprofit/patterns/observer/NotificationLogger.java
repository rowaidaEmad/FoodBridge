package org.nonprofit.patterns.observer;

import org.nonprofit.model.Event;

public class NotificationLogger implements EventObserver {
    @Override
    public void update(Event event,String message) {System.out.println("[NOTIFICATION] Event "+event.getId()+": "+message);}
}
