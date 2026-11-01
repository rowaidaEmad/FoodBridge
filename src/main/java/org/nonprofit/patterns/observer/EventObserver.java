package org.nonprofit.patterns.observer;


import org.nonprofit.model.Event;
public interface EventObserver { void update(Event event, String message); }

