package org.nonprofit.bootstrap;

import java.sql.SQLException;
import java.util.Scanner;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.controller.EventTypeController;
import org.nonprofit.controller.EventController;
import org.nonprofit.repository.EventRegistrationRepository;
import org.nonprofit.repository.EventTypeRepository;
import org.nonprofit.repository.EventRepository;
import org.nonprofit.repository.NotificationRepository;
import org.nonprofit.service.EventTypeService;
import org.nonprofit.service.EventService;
import org.nonprofit.patterns.observer.EventPublisher;
import org.nonprofit.view.ConsoleView;

public class ApplicationBootstrap {

    public ConsoleView build(Scanner input) {

        //db conn
        DatabaseManager db = DatabaseManager.getInstance();

        //for each module, create it's repo, service, and controller
        // then pass to the console view

        //EVENT TYPE MODULE
        EventTypeRepository eventTypeRepository = new EventTypeRepository(db);
        EventTypeService eventTypeService = new EventTypeService(eventTypeRepository);
        EventTypeController eventTypeController = new EventTypeController(eventTypeService);

        // EVENT MANAGEMENT MODULE

        EventRepository eventRepository = new EventRepository(db);
        EventRegistrationRepository registrationRepository = new EventRegistrationRepository(db);
        EventPublisher publisher = new EventPublisher();
        EventService eventService = new EventService(eventRepository,publisher, registrationRepository);

        try {
            eventService.loadEventObservers();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to load event observers",
                    e
            );
        }

        EventController eventController = new EventController(eventService);

        // CONSOLE VIEW
        return new ConsoleView(eventTypeController, eventController, input);
    }
}