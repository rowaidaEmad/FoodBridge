package foodbridge.models.lookups;

/**
 * Lookup table (CRUD). Saved and loaded by FoodCategoryRepository.
 */
public class FoodCategory {
    private int id;
    private String name;

    public FoodCategory() {
    }

    public FoodCategory(String name) {
        setName(name);
    }

    public FoodCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    /** Set by FoodCategoryRepository when the category is saved. */
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

    @Override
    public String toString() {
        return name;
    }
}
