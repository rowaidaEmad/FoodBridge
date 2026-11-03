package org.nonprofit.patterns.observer;


import java.util.*;
import org.nonprofit.model.Event;
/*
this publisher has 3 main functions :
    1. sub : to add an observer
    2. unsub : to remove an obs
    3. notify all the observers
 */
public class EventPublisher {
    // map event_id to the list of it's observers
    private final Map<Long,List<EventObserver>> byEvent = new HashMap<>();

    public void subscribe(long eventId, EventObserver observer) {
        byEvent.computeIfAbsent(eventId,k->new ArrayList<>()).add(observer);
    }
    public void unsubscribe(long eventId, EventObserver observer) {
        byEvent.getOrDefault(eventId,Collections.emptyList()).remove(observer);
    }
    //loop over the observers, call their notify func
    public void notifyObservers(Event event,String message) {
        for(EventObserver o:List.copyOf(byEvent.getOrDefault(event.getId(),List.of())))
        {
            o.update(event,message);
        }
    }
}

