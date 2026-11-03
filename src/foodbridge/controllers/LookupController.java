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
        eventTypeRepository = new EventTypeRepository();
        donationStatusRepository = new DonationStatusRepository();
        foodCategoryRepository = new FoodCategoryRepository();
        unitRepository = new UnitRepository();
        // TODO: create the area repository once it is implemented
    }

    public void addArea(Area item) {
        // TODO: implement
    }

    public List<Area> listAreas() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateArea(Area item) {
        // TODO: implement
    }

    public void deleteArea(int id) {
        // TODO: implement
    }

    public void addDonationStatus(DonationStatus item) {
        item.setName(requireName(item.getName()));
        donationStatusRepository.add(item);
    }

    public List<DonationStatus> listDonationStatuss() {
        return donationStatusRepository.findAll();
    }

    public void updateDonationStatus(DonationStatus item) {
        item.setName(requireName(item.getName()));
        donationStatusRepository.update(item);
    }

    public void deleteDonationStatus(int id) {
        donationStatusRepository.delete(id);
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
        item.setName(requireName(item.getName()));
        foodCategoryRepository.add(item);
    }

    public List<FoodCategory> listFoodCategorys() {
        return foodCategoryRepository.findAll();
    }

    public void updateFoodCategory(FoodCategory item) {
        item.setName(requireName(item.getName()));
        foodCategoryRepository.update(item);
    }

    public void deleteFoodCategory(int id) {
        foodCategoryRepository.delete(id);
    }

    public void addUnit(Unit item) {
        item.setName(requireName(item.getName()));
        unitRepository.add(item);
    }

    public List<Unit> listUnits() {
        return unitRepository.findAll();
    }

    public void updateUnit(Unit item) {
        item.setName(requireName(item.getName()));
        unitRepository.update(item);
    }

    public void deleteUnit(int id) {
        unitRepository.delete(id);
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        return name.trim();
    }
}
