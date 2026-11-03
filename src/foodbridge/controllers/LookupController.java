package foodbridge.controllers;

import java.util.List;

import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.lookups.EventType;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.repositories.AreaRepository;
import foodbridge.repositories.DonationStatusRepository;
import foodbridge.repositories.EventTypeRepository;
import foodbridge.repositories.FoodCategoryRepository;
import foodbridge.repositories.UnitRepository;

/**
 * Admin CRUD on the 5 lookup tables.
 */
public class LookupController {
    private AreaRepository areaRepository;
    private DonationStatusRepository donationStatusRepository;
    private EventTypeRepository eventTypeRepository;
    private FoodCategoryRepository foodCategoryRepository;
    private UnitRepository unitRepository;

    public LookupController() {
        areaRepository = new AreaRepository();
        eventTypeRepository = new EventTypeRepository();
        // TODO: create the other lookup repositories once they are implemented
    }

    public void addArea(Area item) {
        areaRepository.add(item);
    }

    public List<Area> listAreas() {
        return areaRepository.findAll();
    }

    public void updateArea(Area item) {
        areaRepository.update(item);
    }

    public void deleteArea(int id) {
        areaRepository.delete(id);
    }

    public void addDonationStatus(DonationStatus item) {
        // TODO: implement
    }

    public List<DonationStatus> listDonationStatuss() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateDonationStatus(DonationStatus item) {
        // TODO: implement
    }

    public void deleteDonationStatus(int id) {
        // TODO: implement
    }

    public void addEventType(EventType item) {
        item.setName(requireName(item.getName()));
        eventTypeRepository.add(item);
    }

    public List<EventType> listEventTypes() {
        return eventTypeRepository.findAll();
    }

    public void updateEventType(EventType item) {
        item.setName(requireName(item.getName()));
        eventTypeRepository.update(item);
    }

    public void deleteEventType(int id) {
        eventTypeRepository.delete(id);
    }

    public void addFoodCategory(FoodCategory item) {
        // TODO: implement
    }

    public List<FoodCategory> listFoodCategorys() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateFoodCategory(FoodCategory item) {
        // TODO: implement
    }

    public void deleteFoodCategory(int id) {
        // TODO: implement
    }

    public void addUnit(Unit item) {
        // TODO: implement
    }

    public List<Unit> listUnits() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateUnit(Unit item) {
        // TODO: implement
    }

    public void deleteUnit(int id) {
        // TODO: implement
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        return name.trim();
    }
}
