package foodbridge.models.donations;

import java.time.LocalDate;
import java.util.List;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;

public class FoodItem {
    private int id;
    private String name;
    private FoodCategory category;
    private int quantity;
    private Unit unit;
    private List<String> allergens;
    private LocalDate expiryDate;

    public String getInfo() {
        // TODO
        return "";
    }
}
