package foodbridge.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import foodbridge.controllers.AuthController;
import foodbridge.controllers.BeneficiaryController;
import foodbridge.controllers.DonationController;
import foodbridge.controllers.LookupController;
import foodbridge.controllers.ReportController;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.views.admin.ImpactReportView;
import foodbridge.views.admin.ManageBeneficiariesView;
import foodbridge.views.auth.LoginView;
import foodbridge.views.beneficiary.BeneficiaryProfileView;
import foodbridge.views.beneficiary.DonationRequestView;
import foodbridge.views.beneficiary.ReceivedDistributionsView;
import foodbridge.views.donor.AddDonationView;
import foodbridge.views.donor.PayDonationView;
import foodbridge.views.lookups.DonationStatusView;
import foodbridge.views.lookups.FoodCategoryView;
import foodbridge.views.lookups.UnitView;

/**
 * Post-login navigation by role. Module windows are closed when the session ends.
 * Donor: donations. Beneficiary: profile, requests, deliveries. Admin: beneficiaries, reports, lookup tables.
 * TODO: volunteer screens.
 */
public class HomeView implements View {
    private final JFrame frame = new JFrame("FoodBridge - Home");
    private final Map<String, ModuleView> pages = new LinkedHashMap<>();

    public HomeView(User user, AuthController authController, LoginView loginView) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.add(new JLabel("Welcome, " + user.getName() + " (" + user.getClass().getSimpleName() + ")"),
                BorderLayout.NORTH);

        JPanel navigation = new JPanel(new GridLayout(0, 1, 8, 8));
        if (user instanceof Donor donor) {
            DonationController donations = new DonationController();
            navigation.add(dialogButton("Add donation", () -> new AddDonationView(donations, donor).show()));
            navigation.add(dialogButton("My donations (pay / cancel)", () -> new PayDonationView(donations, donor).show()));
        } else if (user instanceof Beneficiary) {
            BeneficiaryController beneficiaries = new BeneficiaryController(authController);
            navigation.add(moduleButton("My profile", () -> new BeneficiaryProfileView(beneficiaries)));
            navigation.add(moduleButton("My requests", () -> new DonationRequestView(beneficiaries)));
            navigation.add(moduleButton("Received distributions", () -> new ReceivedDistributionsView(beneficiaries)));
        } else if (user instanceof Admin) {
            BeneficiaryController beneficiaries = new BeneficiaryController(authController);
            LookupController lookups = new LookupController();
            navigation.add(moduleButton("Manage beneficiaries", () -> new ManageBeneficiariesView(beneficiaries)));
            navigation.add(moduleButton("Impact reports", () -> new ImpactReportView(new ReportController(authController))));
            navigation.add(dialogButton("Donation statuses", () -> new DonationStatusView(lookups).show()));
            navigation.add(dialogButton("Food categories", () -> new FoodCategoryView(lookups).show()));
            navigation.add(dialogButton("Units", () -> new UnitView(lookups).show()));
        } else {
            navigation.add(new JLabel("Screens for this role are coming soon."));
        }
        panel.add(navigation, BorderLayout.CENTER);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            authController.logout();
            pages.values().forEach(ModuleView::close);
            pages.clear();
            frame.dispose();
            loginView.show();
        });
        panel.add(logout, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        frame.setVisible(true);
    }

    /** Opens (or re-shows) a ModuleView window; its Back closes it and brings Home to the front. Closed on logout. */
    private JButton moduleButton(String title, Supplier<ModuleView> create) {
        JButton button = new JButton(title);
        button.addActionListener(e -> {
            try {
                ModuleView page = pages.computeIfAbsent(title, key -> create.get());
                page.setOnBack(frame::toFront);
                page.show();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "FoodBridge", JOptionPane.ERROR_MESSAGE);
            }
        });
        return button;
    }

    /** Opens a modal dialog screen; its Back closes it and returns to Home. */
    private JButton dialogButton(String title, Runnable open) {
        JButton button = new JButton(title);
        button.addActionListener(e -> {
            try {
                open.run();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "FoodBridge", JOptionPane.ERROR_MESSAGE);
            }
        });
        return button;
    }
}
