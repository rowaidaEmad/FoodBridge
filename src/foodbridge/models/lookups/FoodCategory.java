package foodbridge.models.lookups;

/** Lookup value used by beneficiary requests. */
public class FoodCategory {
    private int id;
    private String name;
    public FoodCategory() { }
    public FoodCategory(int id, String name) { this.id = id; setName(name); }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        this.name = name.trim();
    }
    @Override public String toString() { return name; }
}
