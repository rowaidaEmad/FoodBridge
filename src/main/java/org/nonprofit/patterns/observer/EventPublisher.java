package org.nonprofit.patterns.observer;


import java.util.*;
import org.nonprofit.model.Event;
public class EventPublisher {
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

