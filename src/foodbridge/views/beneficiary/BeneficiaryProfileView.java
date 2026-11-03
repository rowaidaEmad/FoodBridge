package foodbridge.views.beneficiary;

import java.awt.BorderLayout;
import javax.swing.*;
import foodbridge.controllers.BeneficiaryController;
import foodbridge.views.ModuleView;

/** Beneficiary self-service for the UML's householdSize and needs fields. */
public class BeneficiaryProfileView extends ModuleView {
    private final BeneficiaryController controller;
    private final JLabel name=new JLabel();
    private final JTextField household=new JTextField();
    private final JTextArea needs=new JTextArea(5,25);
    public BeneficiaryProfileView(BeneficiaryController controller) {
        super("FoodBridge - My beneficiary profile",560,360); this.controller=controller;
        needs.setLineWrap(true); needs.setWrapStyleWord(true);
        JPanel form=form(); field(form,"Name",name); field(form,"Household size",household); field(form,"Needs",new JScrollPane(needs));
        content.add(form,BorderLayout.CENTER);
        JPanel actions=new JPanel(); actions.add(button("Save",this::save)); actions.add(button("Refresh",this::refresh)); content.add(actions,BorderLayout.SOUTH);
    }
    @Override protected void refresh() {
        task(controller::getProfile,b -> { name.setText(b.getName()); household.setText(b.getHouseholdSize()>0 ? String.valueOf(b.getHouseholdSize()) : "1"); needs.setText(b.getNeeds()); });
    }
    private void save() {
        int size=Integer.parseInt(household.getText().trim()); String text=needs.getText();
        task(() -> { controller.updateProfile(size,text); return true; },saved -> message("Profile saved."));
    }
}
