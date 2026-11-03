package foodbridge.views.beneficiary;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import foodbridge.controllers.BeneficiaryController;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.operations.DonationRequest;
import foodbridge.views.ModuleView;

/** Submit requests and display only the logged-in beneficiary's request history. */
public class DonationRequestView extends ModuleView {
    private final BeneficiaryController controller;
    private final JComboBox<FoodCategory> category=new JComboBox<>();
    private final JComboBox<Unit> unit=new JComboBox<>();
    private final JTextField quantity=new JTextField();
    private final DefaultTableModel rows=tableModel("ID","Date","Category","Quantity","Unit","Status");
    private record Data(List<FoodCategory> categories,List<Unit> units,List<DonationRequest> requests) { }
    public DonationRequestView(BeneficiaryController controller) {
        super("FoodBridge - My donation requests",850,500); this.controller=controller;
        JPanel form=form(); field(form,"Food category",category); field(form,"Quantity",quantity); field(form,"Unit",unit);
        content.add(form,BorderLayout.NORTH); content.add(new JScrollPane(table(rows)),BorderLayout.CENTER);
        JPanel actions=new JPanel(); actions.add(button("Submit request",this::submit)); actions.add(button("Refresh",this::refresh)); content.add(actions,BorderLayout.SOUTH);
    }
    @Override protected void refresh() {
        task(() -> new Data(controller.listCategories(),controller.listUnits(),controller.listMyRequests()),data -> {
            choices(category,data.categories()); choices(unit,data.units()); rows.setRowCount(0);
            for(DonationRequest r:data.requests()) rows.addRow(new Object[]{r.getId(),r.getDate(),r.getCategory().getName(),r.getQuantity(),r.getUnit().getName(),r.getStatus()});
        });
    }
    private void submit() {
        FoodCategory selectedCategory=(FoodCategory)category.getSelectedItem(); Unit selectedUnit=(Unit)unit.getSelectedItem();
        if(selectedCategory==null || selectedUnit==null) throw new IllegalArgumentException("No categories or units are available. Ask an admin to load lookup data, then refresh.");
        BigDecimal amount=new BigDecimal(quantity.getText().trim());
        task(() -> {
            DonationRequest request=new DonationRequest(); request.setCategory(selectedCategory); request.setUnit(selectedUnit); request.setQuantity(amount);
            controller.submitRequest(request); return true;
        },saved -> { quantity.setText(""); message("Request submitted."); refresh(); });
    }
}
