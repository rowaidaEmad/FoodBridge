package org.nonprofit.patterns.observer;


import org.nonprofit.model.Event;

//this is observer interface who has update function that receives a notification from the publisher
public interface EventObserver {
    void update(Event event, String message);
}

