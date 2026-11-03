package foodbridge.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import foodbridge.controllers.AdminController;
import foodbridge.controllers.AuthController;
import foodbridge.controllers.BeneficiaryController;
import foodbridge.controllers.DonationController;
import foodbridge.controllers.EventController;
import foodbridge.controllers.LookupController;
import foodbridge.controllers.NotificationController;
import foodbridge.controllers.PickupController;
import foodbridge.controllers.ReportController;
import foodbridge.controllers.UserController;
import foodbridge.controllers.VolunteerController;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.views.admin.AdminPermissionsView;
import foodbridge.views.admin.ImpactReportView;
import foodbridge.views.admin.ManageBeneficiariesView;
import foodbridge.views.admin.ManageEventsView;
import foodbridge.views.admin.ManageUsersView;
import foodbridge.views.admin.VolunteerAssignmentView;
import foodbridge.views.auth.LoginView;
import foodbridge.views.auth.NotificationsView;
import foodbridge.views.auth.ProfileView;
import foodbridge.views.beneficiary.BeneficiaryProfileView;
import foodbridge.views.beneficiary.DonationRequestView;
import foodbridge.views.beneficiary.ReceivedDistributionsView;
import foodbridge.views.donor.AddDonationView;
import foodbridge.views.donor.PayDonationView;
import foodbridge.views.lookups.AreaView;
import foodbridge.views.lookups.DonationStatusView;
import foodbridge.views.lookups.EventTypeView;
import foodbridge.views.lookups.FoodCategoryView;
import foodbridge.views.lookups.UnitView;
import foodbridge.views.volunteer.VolunteerPanelView;

/**
 * Post-login navigation by role. Every window is closed when the session ends.
 * Donor: donations. Volunteer: volunteer panel, donations. Beneficiary: profile, requests, deliveries.
 * Admin: users, events, assignments, beneficiaries, reports, lookup tables, permissions.
 * Everyone: notifications and contact details.
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
        if (user instanceof Volunteer) {
            navigation.add(dialogButton("Volunteer panel", () -> new VolunteerPanelView(user.getId(),
                    new VolunteerController(), new PickupController(), new EventController(),
                    new NotificationController()).show()));
        }
        if (user instanceof Donor || user instanceof Volunteer) {
            Donor donor = asDonor(user);
            DonationController donations = new DonationController();
            navigation.add(dialogButton("Add donation", () -> new AddDonationView(donations, donor).show()));
            navigation.add(dialogButton("My donations (pay / cancel)", () -> new PayDonationView(donations, donor).show()));
        }
        if (user instanceof Beneficiary) {
            BeneficiaryController beneficiaries = new BeneficiaryController(authController);
            navigation.add(moduleButton("My profile", () -> new BeneficiaryProfileView(beneficiaries)));
            navigation.add(moduleButton("My requests", () -> new DonationRequestView(beneficiaries)));
            navigation.add(moduleButton("Received distributions", () -> new ReceivedDistributionsView(beneficiaries)));
        }
        if (user instanceof Admin admin) {
            BeneficiaryController beneficiaries = new BeneficiaryController(authController);
            LookupController lookups = new LookupController();
            navigation.add(moduleButton("Manage users", () -> new ManageUsersView(new UserController(), user.getId())));
            navigation.add(moduleButton("Manage events", () -> new ManageEventsView(new EventController())));
            navigation.add(dialogButton("Assign volunteers", () -> new VolunteerAssignmentView(new VolunteerController(),
                    new PickupController(), new EventController()).show()));
            navigation.add(moduleButton("Manage beneficiaries", () -> new ManageBeneficiariesView(beneficiaries)));
            navigation.add(moduleButton("Impact reports", () -> new ImpactReportView(new ReportController(authController))));
            navigation.add(moduleButton("Areas", () -> new AreaView(lookups)));
            navigation.add(dialogButton("Donation statuses", () -> new DonationStatusView(lookups).show()));
            navigation.add(dialogButton("Event types", () -> new EventTypeView(lookups).show()));
            navigation.add(dialogButton("Food categories", () -> new FoodCategoryView(lookups).show()));
            navigation.add(dialogButton("Units", () -> new UnitView(lookups).show()));
            navigation.add(moduleButton("My permissions", () -> new AdminPermissionsView(new AdminController(), admin)));
        }
        navigation.add(moduleButton("Notifications", () -> new NotificationsView(user.getId(), new NotificationController())));
        navigation.add(moduleButton("Contact details", () -> new ProfileView(new UserController(), user)));
        panel.add(navigation, BorderLayout.CENTER);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            authController.logout();
            pages.values().forEach(ModuleView::close);
            pages.clear();
            for (Window window : Window.getWindows()) {
                window.dispose();
            }
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

    /** A volunteer can donate too; DonationService gives them a donor profile when they do. */
    private Donor asDonor(User user) {
        if (user instanceof Donor donor) {
            return donor;
        }
        Donor donor = new Donor();
        donor.setId(user.getId());
        donor.setName(user.getName());
        donor.setArea(user.getArea());
        return donor;
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

    /** Opens a screen that manages its own window, and reports any failure to open it. */
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
