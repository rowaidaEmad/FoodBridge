package foodbridge.models.lookups;

/**
 * Lookup table (CRUD). Saved and loaded by UnitRepository.
 */
public class Unit {
    private int id;
    private String name;

    public Unit() {
    }

    public Unit(String name) {
        setName(name);
    }

    public Unit(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    /** Set by UnitRepository when the unit is saved. */
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name.trim();
    }
}
