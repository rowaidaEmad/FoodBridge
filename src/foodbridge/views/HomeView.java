package foodbridge.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.*;
import foodbridge.controllers.AuthController;
import foodbridge.controllers.BeneficiaryController;
import foodbridge.controllers.ReportController;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.User;
import foodbridge.views.admin.ImpactReportView;
import foodbridge.views.admin.ManageBeneficiariesView;
import foodbridge.views.auth.LoginView;
import foodbridge.views.beneficiary.BeneficiaryProfileView;
import foodbridge.views.beneficiary.DonationRequestView;
import foodbridge.views.beneficiary.ReceivedDistributionsView;

/** Post-login navigation. Module windows are closed when the session ends. */
public class HomeView implements View {
    private final JFrame frame=new JFrame("FoodBridge - Home");
    private final Map<String,ModuleView> pages=new LinkedHashMap<>();
    public HomeView(User user,AuthController authController,LoginView loginView) {
        JPanel panel=new JPanel(new BorderLayout(12,12));
        panel.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        panel.add(new JLabel("Welcome, "+user.getName()+" ("+user.getClass().getSimpleName()+")"),BorderLayout.NORTH);
        JPanel navigation=new JPanel(new GridLayout(0,1,8,8));
        BeneficiaryController beneficiaries=new BeneficiaryController(authController);
        if(user instanceof Beneficiary) {
            navigation.add(moduleButton("My profile",() -> new BeneficiaryProfileView(beneficiaries)));
            navigation.add(moduleButton("My requests",() -> new DonationRequestView(beneficiaries)));
            navigation.add(moduleButton("Received distributions",() -> new ReceivedDistributionsView(beneficiaries)));
        } else if(user instanceof Admin) {
            navigation.add(moduleButton("Manage beneficiaries",() -> new ManageBeneficiariesView(beneficiaries)));
            navigation.add(moduleButton("Impact reports",() -> new ImpactReportView(new ReportController(authController))));
        }
        panel.add(navigation,BorderLayout.CENTER);
        JButton logout=new JButton("Logout");
        logout.addActionListener(e -> {
            authController.logout(); pages.values().forEach(ModuleView::close); pages.clear(); frame.dispose(); loginView.show();
        });
        panel.add(logout,BorderLayout.SOUTH); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel); frame.pack(); frame.setLocationRelativeTo(null);
    }
    private JButton moduleButton(String title,Supplier<ModuleView> create) {
        JButton button=new JButton(title);
        button.addActionListener(e -> {
            try { pages.computeIfAbsent(title,key -> create.get()).show(); }
            catch(RuntimeException ex) { JOptionPane.showMessageDialog(frame,ex.getMessage(),"FoodBridge",JOptionPane.ERROR_MESSAGE); }
        });
        return button;
    }
    @Override public void show() { frame.setVisible(true); }
}
