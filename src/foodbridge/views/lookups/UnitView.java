package foodbridge.views.lookups;

import java.util.ArrayList;
import java.util.List;

import foodbridge.controllers.LookupController;
import foodbridge.models.lookups.Unit;
import foodbridge.views.View;

/**
 * Screen; talks only to LookupController. Admin CRUD on units.
 */
public class UnitView implements View {
    private LookupController lookupController;

    public UnitView(LookupController lookupController) {
        this.lookupController = lookupController;
    }

    @Override
    public void show() {
        new LookupTableDialog("Units", "unit",
                () -> {
                    List<Object[]> rows = new ArrayList<>();
                    for (Unit u : lookupController.listUnits()) {
                        rows.add(new Object[] { u.getId(), u.getName() });
                    }
                    return rows;
                },
                name -> lookupController.addUnit(new Unit(name)),
                (id, name) -> lookupController.updateUnit(new Unit(id, name)),
                id -> lookupController.deleteUnit(id)).show();
    }
}
