package foodbridge.views.admin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import foodbridge.controllers.BeneficiaryController;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.operations.DonationRequest;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.views.ModuleView;

/** Admin management: request decisions and manual delivery records. */
public class ManageBeneficiariesView extends ModuleView {
    private final BeneficiaryController controller;
    private final DefaultTableModel beneficiaries=tableModel("ID","Name","Email","Household size","Needs","Active");
    private final DefaultTableModel requests=tableModel("ID","Beneficiary","Date","Category","Quantity","Unit","Status");
    private final JTable requestTable=table(requests);
    private final JComboBox<Beneficiary> recipient=new JComboBox<>();
    private final JComboBox<FoodDonation> donation=new JComboBox<>();
    private final JComboBox<Volunteer> volunteer=new JComboBox<>();
    private final JTextField date=new JTextField(LocalDate.now().toString());
    private final JTextField quantity=new JTextField();
    private record Data(List<Beneficiary> beneficiaries,List<DonationRequest> requests,List<FoodDonation> donations,List<Volunteer> volunteers) { }
    public ManageBeneficiariesView(BeneficiaryController controller) {
        super("FoodBridge - Manage beneficiaries",1050,620); this.controller=controller;
        recipient.setRenderer(new UserRenderer()); volunteer.setRenderer(new UserRenderer());
        JTabbedPane tabs=new JTabbedPane(); tabs.addTab("Beneficiaries",new JScrollPane(table(beneficiaries)));
        JPanel requestPanel=new JPanel(new BorderLayout(8,8)); requestPanel.add(new JScrollPane(requestTable),BorderLayout.CENTER);
        JPanel decisions=new JPanel(); decisions.add(button("Approve selected",() -> decide("APPROVED"))); decisions.add(button("Reject selected",() -> decide("REJECTED")));
        requestPanel.add(decisions,BorderLayout.SOUTH); tabs.addTab("Requests",requestPanel);
        JPanel delivery=new JPanel(new BorderLayout(8,8)); JPanel form=form();
        field(form,"Beneficiary",recipient); field(form,"Food donation",donation); field(form,"Volunteer (optional)",volunteer);
        field(form,"Delivery date (yyyy-MM-dd)",date); field(form,"Recorded quantity",quantity);
        delivery.add(form,BorderLayout.NORTH); delivery.add(button("Record distribution",this::record),BorderLayout.SOUTH); tabs.addTab("Record distribution",delivery);
        content.add(tabs,BorderLayout.CENTER); content.add(button("Refresh",this::refresh),BorderLayout.SOUTH);
    }
    @Override protected void refresh() {
        task(() -> new Data(controller.listBeneficiaries(),controller.listAllRequests(),controller.listFoodDonations(),controller.listVolunteers()),data -> {
            beneficiaries.setRowCount(0); requests.setRowCount(0); recipient.removeAllItems();
            for(Beneficiary b:data.beneficiaries()) {
                beneficiaries.addRow(new Object[]{b.getId(),b.getName(),b.getEmail(),b.getHouseholdSize(),b.getNeeds(),b.isActive()});
                if(b.isActive()) recipient.addItem(b);
            }
            for(DonationRequest r:data.requests()) requests.addRow(new Object[]{r.getId(),r.getBeneficiary().getName(),r.getDate(),r.getCategory().getName(),r.getQuantity(),r.getUnit().getName(),r.getStatus()});
            choices(donation,data.donations()); volunteer.removeAllItems(); volunteer.addItem(null); data.volunteers().forEach(volunteer::addItem); volunteer.setSelectedIndex(0);
        });
    }
    private void decide(String status) {
        int row=requestTable.getSelectedRow();
        if(row<0) throw new IllegalArgumentException("Select a request first.");
        int id=((Number)requestTable.getValueAt(row,0)).intValue();
        if(!"PENDING".equals(requestTable.getValueAt(row,6))) throw new IllegalArgumentException("Only pending requests can be approved or rejected.");
        task(() -> { controller.updateRequestStatus(id,status); return true; },saved -> { message("Request "+status.toLowerCase()+"."); refresh(); });
    }
    private void record() {
        Beneficiary b=(Beneficiary)recipient.getSelectedItem(); FoodDonation food=(FoodDonation)donation.getSelectedItem();
        Volunteer v=(Volunteer)volunteer.getSelectedItem();
        if(b==null || food==null) throw new IllegalArgumentException("Select a beneficiary and an existing food donation. Create food donations in the donation module first.");
        LocalDate delivered=LocalDate.parse(date.getText().trim()); BigDecimal amount=new BigDecimal(quantity.getText().trim());
        task(() -> { controller.recordDistribution(b.getId(),food.getId(),v==null ? null : v.getId(),delivered,amount); return true; },saved -> {
            quantity.setText(""); message("Distribution recorded."); refresh();
        });
    }
    private static class UserRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
            super.getListCellRendererComponent(list,value,index,selected,focus);
            setText(value instanceof User user ? "#"+user.getId()+" - "+user.getName() : "Not recorded"); return this;
        }
    }
}
