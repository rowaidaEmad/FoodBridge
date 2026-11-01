package org.nonprofit.service;


import java.sql.SQLException;
import java.util.List;
import org.nonprofit.model.EventType;
import org.nonprofit.repository.EventTypeRepository;
public class EventTypeService {
    private final EventTypeRepository repository;
    public EventTypeService(EventTypeRepository repository) {this.repository=repository;}
    private String valid(String name) { if(name==null || name.isBlank())throw new IllegalArgumentException("Name required"); return name.trim(); }
    public long create(String name)throws SQLException {return repository.create(valid(name));}
    public List<EventType> list()throws SQLException {return repository.findAll();}
    public boolean rename(long id,String name)throws SQLException{return repository.update(id,valid(name));}
    public boolean delete(long id)throws SQLException{return repository.delete(id);}
}

