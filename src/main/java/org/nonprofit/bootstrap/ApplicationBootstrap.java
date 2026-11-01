package org.nonprofit.bootstrap;


import java.util.Scanner;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.controller.EventTypeController;
import org.nonprofit.repository.EventTypeRepository;
import org.nonprofit.service.EventTypeService;
import org.nonprofit.view.ConsoleView;
public class ApplicationBootstrap {
    public ConsoleView build(Scanner input) {
        //get db instance
        DatabaseManager db=DatabaseManager.getInstance();

        EventTypeRepository repository=new EventTypeRepository(db);
        EventTypeService service=new EventTypeService(repository);
        return new ConsoleView(new EventTypeController(service),input);
    }
}

