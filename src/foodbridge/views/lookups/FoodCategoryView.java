package foodbridge.views.lookups;

import java.util.ArrayList;
import java.util.List;

import foodbridge.controllers.LookupController;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.views.View;

/**
 * Screen; talks only to LookupController. Admin CRUD on food categories.
 */
public class FoodCategoryView implements View {
    private LookupController lookupController;

    public FoodCategoryView(LookupController lookupController) {
        this.lookupController = lookupController;
    }

    @Override
    public void show() {
        new LookupTableDialog("Food categories", "category",
                () -> {
                    List<Object[]> rows = new ArrayList<>();
                    for (FoodCategory c : lookupController.listFoodCategorys()) {
                        rows.add(new Object[] { c.getId(), c.getName() });
                    }
                    return rows;
                },
                name -> lookupController.addFoodCategory(new FoodCategory(name)),
                (id, name) -> lookupController.updateFoodCategory(new FoodCategory(id, name)),
                id -> lookupController.deleteFoodCategory(id)).show();
    }
}
