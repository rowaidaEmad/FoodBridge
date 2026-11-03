package foodbridge.views.lookups;

import java.util.ArrayList;
import java.util.List;

import foodbridge.controllers.LookupController;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.views.View;

/**
 * Screen; talks only to LookupController. Admin CRUD on donation statuses.
 */
public class DonationStatusView implements View {
    private LookupController lookupController;

    public DonationStatusView(LookupController lookupController) {
        this.lookupController = lookupController;
    }

    @Override
    public void show() {
        new LookupTableDialog("Donation statuses", "status",
                () -> {
                    List<Object[]> rows = new ArrayList<>();
                    for (DonationStatus s : lookupController.listDonationStatuss()) {
                        rows.add(new Object[] { s.getId(), s.getName() });
                    }
                    return rows;
                },
                name -> lookupController.addDonationStatus(new DonationStatus(name)),
                (id, name) -> lookupController.updateDonationStatus(new DonationStatus(id, name)),
                id -> lookupController.deleteDonationStatus(id)).show();
    }
}
