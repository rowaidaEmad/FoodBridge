import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import foodbridge.controllers.AdminController;
import foodbridge.controllers.AuthController;
import foodbridge.controllers.DonationController;
import foodbridge.controllers.EventController;
import foodbridge.controllers.LookupController;
import foodbridge.controllers.NotificationController;
import foodbridge.controllers.PickupController;
import foodbridge.controllers.UserController;
import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.lookups.EventType;
import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.models.operations.PickupTask;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;
import foodbridge.repositories.FoodCategoryRepository;
import foodbridge.repositories.UnitRepository;
import foodbridge.repositories.UserRepository;
import foodbridge.views.HomeView;
import foodbridge.views.ModuleView;
import foodbridge.views.admin.AdminPermissionsView;
import foodbridge.views.admin.ManageEventsView;
import foodbridge.views.admin.ManageUsersView;
import foodbridge.views.auth.LoginView;
import foodbridge.views.auth.NotificationsView;
import foodbridge.views.auth.ProfileView;
import foodbridge.views.lookups.AreaView;
import foodbridge.views.volunteer.EventRegistrationView;

/** Integration tests for the workflows that join the volunteer and donation code. Never run against application data. */
public class MergedWorkflowTest {
    private static int assertions;
    private static final UserRepository users = new UserRepository();
    private static final UserController userController = new UserController();
    private static final DonationController donations = new DonationController();
    private static final PickupController pickups = new PickupController();
    private static final EventController events = new EventController();
    private static final LookupController lookups = new LookupController();
    private static final NotificationController notifications = new NotificationController();

    static void check(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message);
    }

    static void rejects(Runnable action, String message) {
        assertions++;
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (!"foodbridge_merged_test".equals(c.getCatalog())) {
                throw new IllegalStateException("Refusing to modify a non-test database");
            }
        }
        Donor donor = add(new Donor(), "donor");
        Volunteer volunteer = new Volunteer();
        volunteer.setHasTransport(true);
        add(volunteer, "volunteer");
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setHouseholdSize(3);
        add(beneficiary, "beneficiary");
        Admin admin = add(new Admin(), "admin");

        moneyDonations(donor, volunteer);
        foodDonationBecomesDeliveredPickup(donor, volunteer, beneficiary);
        contactDetails(donor, beneficiary, volunteer);
        eventLifecycle(volunteer);
        areas();
        adminPermissions(admin);
        System.out.println("PASS: " + assertions + " merged workflow/database assertions.");
        if (args.length > 0 && "--gui".equals(args[0])) {
            int before = assertions;
            screens(donor, volunteer, beneficiary, admin);
            System.out.println("PASS: " + (assertions - before) + " merged Swing GUI assertions.");
        }
        System.exit(0);
    }

    private static <T extends User> T add(T user, String name) {
        user.setName("Test " + name);
        user.changeEmail(name + "@example.test");
        user.setPasswordHash("test-only");
        users.add(user);
        return user;
    }

    private static DonationDetails money(Donor who, String amount, String method) {
        DonationDetails details = new DonationDetails();
        details.setDonor(who);
        details.setAmount(new BigDecimal(amount));
        details.setMethod(method);
        return details;
    }

    private static DonationDetails food(Donor who, Area area) {
        FoodItem item = new FoodItem();
        item.setName("Rice");
        item.setQuantity(4);
        item.setCategory(new FoodCategoryRepository().findAll().get(0));
        item.setUnit(new UnitRepository().findAll().get(0));
        DonationDetails details = new DonationDetails();
        details.setDonor(who);
        details.setItems(List.of(item));
        details.setArea(area);
        return details;
    }

    private static Donor presentAsDonor(User user) {
        Donor donor = new Donor();
        donor.setId(user.getId());
        donor.setName(user.getName());
        return donor;
    }

    private static void moneyDonations(Donor donor, Volunteer volunteer) {
        for (String method : List.of("cash", "check", "online")) {
            int id = donations.createDonation("money", money(donor, "120.50", method));
            DonationProcessingResult result = donations.payMoneyDonation(id);
            check(result.isSuccessful(), method + " payment succeeds");
            check(result.getReference() != null, method + " payment has a reference");
            Donation saved = donations.getDonation(id);
            check(DonationStatus.PAID.equals(saved.getStatus().getName()), method + " donation is Paid");
            check(!donations.cancelDonation(id), "A paid donation cannot be cancelled");
        }
        int unpaid = donations.createDonation("money", money(donor, "10", "cash"));
        check(donations.cancelDonation(unpaid), "A posted donation can be cancelled");
        check(DonationStatus.CANCELLED.equals(donations.getDonation(unpaid).getStatus().getName()), "Cancel is saved");

        // The join between the two branches: a volunteer has no donor row until they donate.
        int fromVolunteer = donations.createDonation("money", money(presentAsDonor(volunteer), "75", "cash"));
        check(donations.payMoneyDonation(fromVolunteer).isSuccessful(), "A volunteer can pay a money donation");
        check(donations.listDonations(volunteer.getId()).size() == 1, "A volunteer sees their own donation");

        rejects(() -> donations.createDonation("money", new DonationDetails()), "A donation without a donor is rejected");
        rejects(() -> donations.createDonation("nonsense", money(donor, "1", "cash")), "An unknown type is rejected");
    }

    private static void foodDonationBecomesDeliveredPickup(Donor donor, Volunteer volunteer, Beneficiary beneficiary)
            throws Exception {
        Area area = lookups.listAreas().get(0);
        int before = pickups.listTasks().size();
        // The view passes "food"; the case must not matter to the factory lookup.
        int id = donations.createDonation("FOOD", food(donor, area));
        check(pickups.listTasks().size() == before + 1, "A food donation opens a pickup task");
        check(donations.createDonation("money", money(donor, "5", "cash")) > 0 && pickups.listTasks().size() == before + 1,
                "A money donation opens no pickup task");

        PickupTask task = null;
        for (PickupTask t : pickups.listAvailable(volunteer.getId(), area.getId())) {
            if (t.getDonation().getId() == id) {
                task = t;
            }
        }
        check(task != null, "The task is offered to volunteers in the donation's area");
        Volunteer onFoot = add(new Volunteer(), "volunteer-on-foot");
        check(pickups.listAvailable(onFoot.getId(), area.getId()).isEmpty(), "A volunteer without transport is not offered pickups");
        check(pickups.acceptTask(task.getId(), volunteer.getId()), "A volunteer can accept the task");
        pickups.markPickedUp(task.getId(), volunteer.getId());
        pickups.markDelivered(task.getId(), volunteer.getId());
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT beneficiary_id, volunteer_id, quantity FROM distributions WHERE pickup_task_id = ?")) {
            ps.setInt(1, task.getId());
            try (ResultSet rs = ps.executeQuery()) {
                check(rs.next(), "Delivery records a distribution");
                check(rs.getInt("beneficiary_id") == beneficiary.getId(), "The distribution names the beneficiary");
                check(rs.getInt("volunteer_id") == volunteer.getId(), "The distribution names the volunteer");
                check(rs.getBigDecimal("quantity").compareTo(new BigDecimal("4")) == 0, "Quantity comes from the items");
            }
        }
        check(new foodbridge.repositories.DistributionRepository().findByBeneficiary(beneficiary.getId()).size() == 1,
                "The beneficiary module reads the volunteer's delivery");
        check(((Volunteer) users.findById(volunteer.getId())).getTotalHours() > 0, "The delivery credits volunteer hours");
    }

    /** Donors and beneficiaries here already have rows that reference them; the old update() broke on those. */
    private static void contactDetails(Donor donor, Beneficiary beneficiary, Volunteer volunteer) {
        for (User user : List.of(donor, beneficiary, volunteer)) {
            int donationsBefore = donations.listDonations(user.getId()).size();
            userController.updateContactInfo(user.getId(), "0100", "12 Test Street");
            User saved = users.findById(user.getId());
            check("0100".equals(saved.getPhone()) && "12 Test Street".equals(saved.getAddress()), "Contact details save");
            check(donations.listDonations(user.getId()).size() == donationsBefore, "Saving keeps the user's donations");
        }
        check(((Beneficiary) users.findById(beneficiary.getId())).getHouseholdSize() == 3, "Saving keeps the household size");
        check(((Volunteer) users.findById(volunteer.getId())).getTotalHours() > 0, "Saving keeps volunteer hours");

        userController.changeEmail(donor.getId(), "new-donor@example.test");
        check("new-donor@example.test".equals(users.findById(donor.getId()).getEmail()), "Email change saves");
        userController.changeEmail(donor.getId(), "new-donor@example.test");
        check(true, "Saving your own unchanged email is allowed");
        rejects(() -> userController.changeEmail(donor.getId(), volunteer.getEmail()), "A taken email is rejected");
        rejects(() -> userController.changeEmail(donor.getId(), " "), "A blank email is rejected");

        userController.setActive(donor.getId(), false);
        check(!users.findById(donor.getId()).isActive(), "Deactivating saves");
        userController.setActive(donor.getId(), true);
        check(users.findById(donor.getId()).isActive(), "Reactivating saves");
        check(userController.listUsers().size() >= 4, "Users are listed");
        rejects(() -> userController.setActive(Integer.MAX_VALUE, true), "A missing user is rejected");
    }

    private static void eventLifecycle(Volunteer volunteer) {
        EventType type = events.listEventTypes().get(0);
        Event event = new Event();
        event.setTitle("Food drive");
        event.setDescription("Sorting");
        event.setDate(LocalDateTime.now().plusDays(7).withNano(0));
        event.setLocation("Cairo");
        event.setType(type);
        event.setRequiredSkill("First aid");
        int id = Math.toIntExact(events.createEvent(event));
        check(id > 0, "An event gets an id");
        check(events.listEvents().stream().anyMatch(e -> e.getId() == id && "Food drive".equals(e.getTitle())), "The event is listed");

        Event noTitle = new Event();
        noTitle.setDate(LocalDateTime.now());
        noTitle.setType(type);
        rejects(() -> events.createEvent(noTitle), "An event without a title is rejected");

        check("First aid".equals(events.getEvent(id).getRequiredSkill()), "The required skill is saved");
        rejects(() -> events.registerVolunteer(id, volunteer.getId()), "A volunteer without the skill is rejected");
        volunteer.setSkills(List.of("First aid"));
        new foodbridge.repositories.VolunteerRepository().updateProfile(volunteer.getId(), volunteer.getAvailability(),
                volunteer.hasTransport(), null, List.of("First aid"), List.of());
        events.registerVolunteer(id, volunteer.getId());
        assertions++;
        try {
            events.registerVolunteer(id, volunteer.getId());
            throw new AssertionError("Registering twice is rejected");
        } catch (IllegalStateException expected) {
            // the message names the volunteer and the event
        }
        check(events.listVolunteerRegistrations(volunteer.getId()).stream().anyMatch(r -> r.getEvent().getId() == id),
                "The registration is listed by volunteer");
        List<EventRegistration> forEvent = events.listRegistrationsForEvent(id);
        check(forEvent.size() == 1 && forEvent.get(0).getVolunteer().getId() == volunteer.getId(),
                "The registration is listed by event");
        double hoursBefore = ((Volunteer) users.findById(volunteer.getId())).getTotalHours();
        events.markAttendance(forEvent.get(0).getId(), EventRegistration.ATTENDED, 2.5);
        check(((Volunteer) users.findById(volunteer.getId())).getTotalHours() == hoursBefore + 2.5, "Attendance credits hours");
        events.markAttendance(forEvent.get(0).getId(), EventRegistration.ATTENDED, 2.5);
        check(((Volunteer) users.findById(volunteer.getId())).getTotalHours() == hoursBefore + 2.5, "Hours are credited once");
        int notes = notifications.listForUser(volunteer.getId()).size();

        Event changed = new Event();
        changed.setTitle("Bigger food drive");
        changed.setDate(event.getDate().plusDays(1));
        changed.setLocation("Giza");
        changed.setType(type);
        changed.setRequiredSkill("First aid");
        events.updateEvent(id, changed);
        Event saved = events.listEvents().stream().filter(e -> e.getId() == id).findFirst().orElseThrow();
        check("Bigger food drive".equals(saved.getTitle()) && "Giza".equals(saved.getLocation()), "Event changes save");
        check(notifications.listForUser(volunteer.getId()).size() == notes + 1, "An update notifies registered volunteers");

        events.announceChange(id, "Bring gloves");
        check(notifications.listForUser(volunteer.getId()).size() == notes + 2, "An announcement notifies registered volunteers");

        check(events.cancelEvent(id), "An event can be cancelled");
        check(events.getEvent(id).isCancelled(), "A cancelled event keeps its row, marked cancelled");
        check(events.listVolunteerRegistrations(volunteer.getId()).stream().anyMatch(r -> r.getEvent().isCancelled()),
                "The volunteer's registration shows the cancelled status");
        check(notifications.listForUser(volunteer.getId()).size() == notes + 3, "Cancelling notifies who was registered");
        check(!events.cancelEvent(id), "Cancelling twice returns false");
        check(!events.cancelEvent(Integer.MAX_VALUE), "Cancelling a missing event returns false");
        assertions += 2;
        try {
            events.registerVolunteer(id, volunteer.getId());
            throw new AssertionError("Registering for a cancelled event is rejected");
        } catch (IllegalStateException expected) {
            // cancelled
        }
        try {
            events.updateEvent(id, changed);
            throw new AssertionError("Updating a cancelled event is rejected");
        } catch (IllegalStateException expected) {
            // cancelled
        }
    }

    private static void areas() {
        Area area = new Area(0, " Luxor ", " East Bank ");
        lookups.addArea(area);
        check(area.getId() > 0, "An area gets an id");
        check(lookups.listAreas().stream().anyMatch(a -> a.getId() == area.getId() && "Luxor".equals(a.getCity())), "Names are trimmed");
        lookups.updateArea(new Area(area.getId(), "Luxor", "West Bank"));
        check(lookups.listAreas().stream().anyMatch(a -> a.getId() == area.getId() && "West Bank".equals(a.getDistrict())), "Area changes save");
        rejects(() -> lookups.addArea(new Area(0, " ", "x")), "A blank city is rejected");
        lookups.deleteArea(area.getId());
        check(lookups.listAreas().stream().noneMatch(a -> a.getId() == area.getId()), "An area can be deleted");
    }

    private static void adminPermissions(Admin saved) {
        Admin admin = new Admin();
        check(!admin.hasPermission("reports"), "No permissions to begin with");
        admin.addPermission(" reports ");
        check(admin.hasPermission("reports"), "A permission can be added");
        admin.removePermission("reports");
        check(!admin.hasPermission("reports"), "A permission can be removed");
        rejects(() -> admin.addPermission(" "), "A blank permission is rejected");

        AdminController controller = new AdminController();
        check(controller.addPermission(saved.getId(), "reports"), "A permission can be stored");
        check(!controller.addPermission(saved.getId(), "REPORTS"), "A stored permission is not added twice");
        check(controller.hasPermission(saved.getId(), "Reports"), "A stored permission is found");
        check(controller.removePermission(saved.getId(), "reports"), "A stored permission can be removed");
        check(controller.getPermissionCount(saved.getId()) == 0, "No stored permissions remain");
    }

    // --- Screens: each opens without an error dialog, and Home offers each role the right buttons. ---------

    private static void screens(Donor donor, Volunteer volunteer, Beneficiary beneficiary, Admin admin) throws Exception {
        AuthController auth = new AuthController();
        check(buttons(donor, auth).containsAll(Set.of("Add donation", "My donations (pay / cancel)", "Notifications", "Contact details", "Logout")), "Donor buttons");
        check(buttons(volunteer, auth).containsAll(Set.of("Volunteer panel", "Add donation", "My donations (pay / cancel)", "Contact details")), "Volunteer buttons");
        check(buttons(beneficiary, auth).containsAll(Set.of("My profile", "My requests", "Received distributions", "Contact details")), "Beneficiary buttons");
        check(buttons(admin, auth).containsAll(Set.of("Manage users", "Manage events", "Assign volunteers", "Manage beneficiaries",
                "Impact reports", "Areas", "Donation statuses", "Event types", "Food categories", "Units", "My permissions",
                "Notifications", "Contact details")), "Admin buttons");
        check(!buttons(donor, auth).contains("Manage users"), "A donor cannot reach admin screens");

        opens(() -> new ManageUsersView(userController, admin.getId()));
        opens(() -> new ManageEventsView(events));
        opens(() -> new AreaView(lookups));
        opens(() -> new NotificationsView(volunteer.getId(), notifications));
        opens(() -> new EventRegistrationView(volunteer.getId(), events));
        opens(() -> new AdminPermissionsView(new AdminController(), admin));
        opens(() -> new ProfileView(userController, users.findById(donor.getId())));
    }

    private static Set<String> buttons(User user, AuthController auth) throws Exception {
        Set<String> names = new TreeSet<>();
        SwingUtilities.invokeAndWait(() -> {
            List<Window> before = List.of(Window.getWindows());
            new HomeView(user, auth, new LoginView(auth)).show();
            for (Window w : Window.getWindows()) {
                if (!before.contains(w) && w.isVisible()) {
                    collect(w, names);
                    w.dispose();
                }
            }
        });
        return names;
    }

    private static void collect(Component component, Set<String> names) {
        if (component instanceof JButton button) {
            names.add(button.getText());
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                collect(child, names);
            }
        }
    }

    private interface Screen {
        ModuleView create();
    }

    private static void opens(Screen screen) throws Exception {
        List<ModuleView> opened = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            ModuleView view = screen.create();
            view.show();
            opened.add(view);
        });
        Thread.sleep(1500); // the screen's first load runs on a Swing worker
        boolean[] errorShown = { false };
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog dialog && dialog.isVisible()) {
                    errorShown[0] = true;
                    dialog.dispose();
                }
            }
            opened.get(0).close();
        });
        check(!errorShown[0], "A screen opens without an error dialog");
    }
}
