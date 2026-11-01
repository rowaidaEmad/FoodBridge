package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.List;
import org.nonprofit.model.EventType;
import org.nonprofit.service.EventTypeService;
public class EventTypeController {
    private final EventTypeService service;
    public EventTypeController(EventTypeService service) {this.service=service;}
    public long create(String name)throws SQLException{return service.create(name);}
    public List<EventType> list()throws SQLException{return service.list();}
    public boolean rename(long id,String name)throws SQLException{return service.rename(id,name);}
    public boolean delete(long id)throws SQLException{return service.delete(id);}
}

