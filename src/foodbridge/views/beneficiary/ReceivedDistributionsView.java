package foodbridge.views.beneficiary;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import foodbridge.controllers.BeneficiaryController;
import foodbridge.models.operations.Distribution;
import foodbridge.views.ModuleView;

/** Read-only delivery history, scoped by the controller to the current beneficiary. */
public class ReceivedDistributionsView extends ModuleView {
    private final BeneficiaryController controller;
    private final DefaultTableModel rows=tableModel("ID","Date","Food donation ID","Recorded quantity","Volunteer");
    public ReceivedDistributionsView(BeneficiaryController controller) {
        super("FoodBridge - Received distributions",850,420); this.controller=controller;
        content.add(new JLabel("Recorded delivery quantities follow the donation record; item-level allocation is not tracked."),BorderLayout.NORTH);
        content.add(new JScrollPane(table(rows)),BorderLayout.CENTER); content.add(button("Refresh",this::refresh),BorderLayout.SOUTH);
    }
    @Override protected void refresh() {
        task(controller::listMyDistributions,list -> {
            rows.setRowCount(0);
            for(Distribution d:list) rows.addRow(new Object[]{d.getId(),d.getDate(),d.getDonation().getId(),d.getQuantity(),d.getVolunteer()==null ? "Not recorded" : d.getVolunteer().getName()});
        });
    }
}
