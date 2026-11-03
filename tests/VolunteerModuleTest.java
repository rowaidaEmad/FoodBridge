import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import foodbridge.controllers.AuthController;
import foodbridge.controllers.DonationController;
import foodbridge.controllers.EventController;
import foodbridge.controllers.LookupController;
import foodbridge.controllers.NotificationController;
import foodbridge.controllers.PickupController;
import foodbridge.controllers.UserController;
import foodbridge.controllers.VolunteerController;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.enums.PickupTaskStatus;
import foodbridge.models.lookups.Area;
import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.models.operations.PickupTask;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;
import foodbridge.repositories.DistributionRepository;
import foodbridge.repositories.FoodCategoryRepository;
import foodbridge.repositories.UnitRepository;
import foodbridge.repositories.UserRepository;
import foodbridge.views.HomeView;
import foodbridge.views.ModuleView;
import foodbridge.views.admin.ManageUsersView;
import foodbridge.views.admin.VolunteerAssignmentView;
import foodbridge.views.auth.LoginView;
import foodbridge.views.auth.ProfileView;
import foodbridge.views.lookups.AreaView;
import foodbridge.views.volunteer.VolunteerPanelView;

/** The volunteer module working on top of the team's donation, event and notification code. Test database only. */
public class VolunteerModuleTest {
    private static int assertions;
    private static final UserRepository users = new UserRepository();
    private static final VolunteerController volunteers = new VolunteerController();
    private static final PickupController pickups = new PickupController();
    private static final DonationController donations = new DonationController();
    private static final EventController events = new EventController();
    private static final NotificationController notifications = new NotificationController();
    private static final UserController userController = new UserController();
    private static final LookupController lookups = new LookupController();

    static void check(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message);
    }

    interface Action {
        void run();
    }

    static void rejects(Action action, String message) {
        assertions++;
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (!"foodbridge_volunteer_test".equals(c.getCatalog())) {
                throw new IllegalStateException("Refusing to modify a non-test database");
            }
        }
        Donor donor = add(new Donor(), "donor");
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setHouseholdSize(3);
        add(beneficiary, "beneficiary");
        Admin admin = add(new Admin(), "admin");
        Volunteer driver = new Volunteer();
        driver.setHasTransport(true);
        driver.setAvailability("weekends");
        add(driver, "driver");
        Volunteer walker = new Volunteer();
        walker.setAvailability("weekdays");
        add(walker, "walker");

        Area area = new Area(0, "Cairo", "Maadi");
        lookups.addArea(area);

        profiles(driver, area);
        pickupModel(driver);
        donationsBecomeTasks(donor, driver, walker, beneficiary, area);
        taskSkills(donor, driver);
        eventsAndAttendance(driver, walker);
        ranking(donor, driver, walker);
        certificates(walker);
        accounts(donor, beneficiary, driver);
        demoData();
        System.out.println("PASS: " + assertions + " volunteer module assertions.");
        if (args.length > 0 && "--gui".equals(args[0])) {
            int before = assertions;
            screens(donor, driver, beneficiary, admin);
            System.out.println("PASS: " + (assertions - before) + " volunteer Swing GUI assertions.");
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

    private static int food(Donor who, Area where) {
        FoodItem item = new FoodItem();
        item.setName("Rice");
        item.setQuantity(4);
        item.setCategory(new FoodCategoryRepository().findAll().get(0));
        item.setUnit(new UnitRepository().findAll().get(0));
        DonationDetails details = new DonationDetails();
        details.setDonor(who);
        details.setItems(List.of(item));
        details.setArea(where);
        details.setPickupWindow("weekends");
        return donations.createDonation("food", details);
    }

    private static PickupTask taskFor(List<PickupTask> tasks, int donationId) {
        return tasks.stream().filter(t -> t.getDonation().getId() == donationId).findFirst().orElse(null);
    }

    private static void sql(String statement, int id) throws Exception {
        try (Connection c = DatabaseManager.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(statement)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static void profiles(Volunteer volunteer, Area area) {
        volunteers.updateProfile(volunteer.getId(), "weekends", true, area.getId(),
                List.of("Driving", " driving ", "Logistics"), List.of("Pickup", "Workshop"));
        Volunteer saved = volunteers.getVolunteer(volunteer.getId());
        check(saved.hasSkill("driving") && saved.hasSkill("Logistics"), "Skills save and match in any case");
        check(saved.getSkills().size() == 2, "Repeated skills are removed");
        check(saved.prefers("workshop"), "Preferences save");
        check(saved.getArea() != null && saved.getArea().getId() == area.getId(), "The area saves");
        check(pickups.volunteerArea(volunteer.getId()).getId() == area.getId(), "Pickups can read the volunteer's area");
        check(saved.hasTransport() && "weekends".equals(saved.getAvailability()), "Availability and transport save");
    }

    /** The two signatures the final class diagram fixes. */
    private static void pickupModel(Volunteer volunteer) {
        PickupTask task = new PickupTask();
        task.assignVolunteer(volunteer);
        check(task.getStatus() == PickupTaskStatus.ASSIGNED && task.getVolunteer() == volunteer, "assignVolunteer assigns");
        rejects(() -> task.assignVolunteer(volunteer), "A task can only be assigned while pending");
        rejects(task::markDelivered, "Delivery cannot come before pickup");
        task.markPickedUp();
        rejects(task::markDelivered, "Delivery needs a beneficiary");
        task.setBeneficiary(new Beneficiary());
        task.markDelivered();
        check(task.getStatus() == PickupTaskStatus.DELIVERED, "markDelivered delivers");
        check(task.getHoursSpent() >= 0.25, "A delivery counts at least 15 minutes");
    }

    private static void donationsBecomeTasks(Donor donor, Volunteer driver, Volunteer walker, Beneficiary beneficiary, Area area) {
        int before = pickups.listTasks().size();
        int id = food(donor, area);
        List<PickupTask> pending = pickups.listTasks();
        check(pending.size() == before + 1, "A food donation from the donation module opens a task");
        check(pickups.listTasks().size() == before + 1, "It only opens one task");
        check(taskFor(pending, id).getBeneficiary().getId() == beneficiary.getId(), "The task has a beneficiary");

        check(taskFor(pickups.listAvailable(driver.getId(), area.getId()), id) != null, "Offered in the donation's area");
        check(pickups.listAvailable(walker.getId(), area.getId()).isEmpty(), "Not offered without transport");
        int noArea = food(donor, null);
        check(taskFor(pickups.listAvailable(driver.getId(), area.getId()), noArea) != null, "A donation with no area is offered anyway");

        int cancelled = food(donor, area);
        check(taskFor(pickups.listTasks(), cancelled) != null, "A posted donation is offered");
        check(donations.cancelDonation(cancelled), "The donation module can cancel it");
        check(taskFor(pickups.listTasks(), cancelled) == null, "A cancelled donation is no longer offered");

        PickupTask task = taskFor(pickups.listTasks(), id);
        check(pickups.acceptTask(task.getId(), driver.getId()), "A volunteer can accept");
        check(!pickups.acceptTask(task.getId(), driver.getId()), "A taken task cannot be accepted again");
        pickups.declineTask(task.getId(), driver.getId());
        check(taskFor(pickups.listTasks(), id) != null, "A declined task is offered again");
        pickups.assignVolunteer(task.getId(), driver.getId());
        check(notifications.listForUser(driver.getId()).size() == 1, "Assignment notifies the volunteer");
        rejects(() -> pickups.assignVolunteer(task.getId(), driver.getId()), "A task cannot be assigned twice");
        rejects(() -> pickups.markDelivered(task.getId(), driver.getId()), "Steps stay in order");
        rejects(() -> pickups.markPickedUp(task.getId(), walker.getId()), "Only the assigned volunteer can act");
        pickups.markPickedUp(task.getId(), driver.getId());
        pickups.markDelivered(task.getId(), driver.getId());
        check(volunteers.getVolunteer(driver.getId()).getTotalHours() >= 0.25, "Delivery credits hours");
        check(volunteers.viewHours(driver.getId()).stream().anyMatch(h -> "PICKUP".equals(h.getSource())), "Hours record their source");

        var delivered = new DistributionRepository().findByBeneficiary(beneficiary.getId());
        check(delivered.size() == 1, "The beneficiary module reads the delivery");
        check(delivered.get(0).getQuantity().compareTo(new BigDecimal("4")) == 0, "The quantity comes from the items");
        check(delivered.get(0).getVolunteer().getId() == driver.getId(), "The delivery names the volunteer");
    }

    private static void taskSkills(Donor donor, Volunteer driver) throws Exception {
        int id = food(donor, null);
        PickupTask task = taskFor(pickups.listTasks(), id);
        sql("UPDATE pickup_tasks SET required_skill = 'Forklift' WHERE id = ?", task.getId());
        check(taskFor(pickups.listAvailable(driver.getId(), null), id) == null, "A task needing another skill is not offered");
        rejects(() -> pickups.assignVolunteer(task.getId(), driver.getId()), "An admin cannot assign an unqualified volunteer");
        sql("UPDATE pickup_tasks SET required_skill = 'Driving' WHERE id = ?", task.getId());
        check(taskFor(pickups.listAvailable(driver.getId(), null), id) != null, "A task needing their skill is offered");
    }

    private static int newEvent(String title, String skill, LocalDateTime when) throws Exception {
        Event event = new Event();
        event.setTitle(title);
        event.reschedule(when);
        event.setType(events.listEventTypes().stream().filter(t -> "Workshop".equals(t.getName())).findFirst().orElseThrow());
        int id = (int) events.createEvent(event);
        if (skill != null) {
            try (Connection c = DatabaseManager.getInstance().getConnection();
                    PreparedStatement ps = c.prepareStatement("UPDATE events SET required_skill = ? WHERE id = ?")) {
                ps.setString(1, skill);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        }
        return id;
    }

    private static void eventsAndAttendance(Volunteer driver, Volunteer walker) throws Exception {
        int id = newEvent("Sorting day", "Logistics", LocalDateTime.now().plusDays(7).withNano(0));
        volunteers.requireEventSkill(id, driver.getId());
        check(true, "A volunteer with the skill passes the check");
        rejects(() -> volunteers.requireEventSkill(id, walker.getId()), "A volunteer without the skill fails the check");

        events.registerVolunteer(id, driver.getId());
        List<EventRegistration> registered = events.listRegistrations(id);
        check(registered.size() == 1 && registered.get(0).getVolunteer().getId() == driver.getId(), "The team's event code registers the volunteer");
        int registration = registered.get(0).getId();

        double hours = volunteers.getVolunteer(driver.getId()).getTotalHours();
        rejects(() -> volunteers.markAttended(registration, 0), "Attendance needs hours above 0");
        volunteers.markAttended(registration, 2.5);
        check(volunteers.getVolunteer(driver.getId()).getTotalHours() == hours + 2.5, "Attendance credits hours");
        check("ATTENDED".equals(events.listRegistrations(id).get(0).getAttendanceStatus()), "Attendance is saved");
        volunteers.markAttended(registration, 2.5);
        check(volunteers.getVolunteer(driver.getId()).getTotalHours() == hours + 2.5, "Hours are credited once");
        check(volunteers.viewHours(driver.getId()).stream().anyMatch(h -> "EVENT".equals(h.getSource()) && "Sorting day".equals(h.getNote())), "The event is named in the hours");

        int notes = notifications.listForUser(driver.getId()).size();
        events.announceChange(events.getEvent(id), "Bring gloves");
        check(notifications.listForUser(driver.getId()).size() == notes + 1, "The team's Observer notifies the volunteer");
        check(events.cancelEvent(id), "The team's cancel works");
        check(events.getEvent(id).isCancelled(), "The event is marked cancelled");
        check(notifications.listForUser(driver.getId()).size() == notes + 2, "Cancelling notifies the volunteer");
    }

    private static void ranking(Donor donor, Volunteer driver, Volunteer walker) throws Exception {
        check(volunteers.strategyNames().containsAll(List.of("Skill", "Availability", "Preference")), "All three strategies are offered");
        rejects(() -> volunteers.rankForEvent(1, "Luck"), "An unknown strategy is rejected");
        rejects(() -> volunteers.rankForEvent(Integer.MAX_VALUE, "Skill"), "An unknown event is rejected");

        LocalDateTime saturday = LocalDateTime.now().plusDays(7).withNano(0);
        while (saturday.getDayOfWeek() != java.time.DayOfWeek.SATURDAY) {
            saturday = saturday.plusDays(1);
        }
        int skilled = newEvent("Needs logistics", "Logistics", saturday);
        check(volunteers.rankForEvent(skilled, "Skill").get(0).getId() == driver.getId(), "Skill ranks the skilled volunteer first");
        int weekend = newEvent("Weekend", null, saturday);
        check(volunteers.rankForEvent(weekend, "Availability").get(0).getId() == driver.getId(), "Availability uses the event's day");
        check(volunteers.rankForEvent(weekend, "Preference").get(0).getId() == driver.getId(), "Preference uses the event type");

        int id = food(donor, null);
        PickupTask task = taskFor(pickups.listTasks(), id);
        List<Volunteer> ranked = volunteers.rankForTask(task.getId(), "Skill");
        check(ranked.stream().noneMatch(v -> v.getId() == walker.getId()), "Pickup ranking leaves out volunteers without transport");
    }

    private static void certificates(Volunteer volunteer) {
        rejects(() -> volunteers.requestCertificate(volunteer.getId()), "Under 10 hours gets no certificate");
        rejects(() -> volunteers.logHours(volunteer.getId(), 0, "none"), "Logged hours must be above 0");
        volunteers.logHours(volunteer.getId(), 10, "Packing");
        String first = volunteers.requestCertificate(volunteer.getId());
        check(first.contains("CERTIFICATE OF PARTICIPATION"), "10 hours gives Participation (Factory Method)");
        check(first.contains("Hours breakdown") && first.contains("MANUAL"), "The breakdown decorator adds its section");
        check(first.contains("Signed"), "The signature decorator adds its section");
        check(number(first).equals(number(volunteers.requestCertificate(volunteer.getId()))), "Asking again returns the same certificate");
        volunteers.logHours(volunteer.getId(), 40, "Packing");
        check(volunteers.requestCertificate(volunteer.getId()).contains("ACHIEVEMENT"), "50 hours gives Achievement");
    }

    private static String number(String certificate) {
        return certificate.lines().filter(l -> l.startsWith("Certificate number")).findFirst().orElseThrow();
    }

    /** UserController fills a stub, and must not disturb the rows other modules reference. */
    private static void accounts(Donor donor, Beneficiary beneficiary, Volunteer volunteer) {
        int donated = donations.listDonations(donor.getId()).size();
        check(donated > 0, "The donor has donations");
        for (User user : List.of(donor, beneficiary, volunteer)) {
            userController.updateContactInfo(user.getId(), "0100", "12 Test Street");
            User saved = users.findById(user.getId());
            check("0100".equals(saved.getPhone()) && "12 Test Street".equals(saved.getAddress()), "Contact details save");
        }
        check(donations.listDonations(donor.getId()).size() == donated, "Donations are kept");
        check(((Beneficiary) users.findById(beneficiary.getId())).getHouseholdSize() == 3, "The household size is kept");
        check(volunteers.getVolunteer(volunteer.getId()).hasSkill("Driving"), "Skills are kept");
        rejects(() -> userController.changeEmail(donor.getId(), volunteer.getEmail()), "A taken email is rejected");
        userController.setActive(donor.getId(), false);
        check(!users.findById(donor.getId()).isActive(), "Deactivating saves");
        userController.setActive(donor.getId(), true);
    }

    private static void demoData() throws Exception {
        int before = pickups.listTasks().size();
        int eventsBefore = events.listEvents().size();
        String script = Files.readString(Path.of("database/seed_volunteer.sql"));
        try (Connection c = DatabaseManager.getInstance().getConnection(); Statement st = c.createStatement()) {
            st.execute(script);
        }
        check(pickups.listTasks().size() == before + 3, "The demo data adds 3 tasks");
        check(events.listEvents().size() == eventsBefore + 2, "The demo data adds 2 events");
        check(lookups.listAreas().size() >= 4, "The demo data adds the areas");
        try (Connection c = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT required_skill FROM events WHERE title = 'Food Drive'");
                ResultSet rs = ps.executeQuery()) {
            check(rs.next() && "Logistics".equals(rs.getString(1)), "The demo event has a required skill");
        }
    }

    // --- Screens ----------------------------------------------------------------------------------------

    private static void screens(Donor donor, Volunteer volunteer, Beneficiary beneficiary, Admin admin) throws Exception {
        AuthController auth = new AuthController();
        check(buttons(donor, auth).containsAll(Set.of("Add donation", "My donations (pay / cancel)", "Notifications", "Contact details")), "Donor buttons");
        check(buttons(volunteer, auth).containsAll(Set.of("Volunteer panel", "Events", "Notifications", "Contact details")), "Volunteer buttons");
        check(buttons(beneficiary, auth).containsAll(Set.of("My profile", "My requests", "Received distributions")), "Beneficiary buttons");
        check(buttons(admin, auth).containsAll(Set.of("Manage beneficiaries", "Impact reports", "Donation statuses", "Food categories", "Units",
                "Manage events", "Event types", "My permissions", "Assign volunteers", "Manage users", "Areas")), "Admin buttons");

        opens(() -> new ManageUsersView(userController, admin.getId()));
        opens(() -> new AreaView(lookups));
        opens(() -> new ProfileView(userController, users.findById(donor.getId())));

        windows("FoodBridge - Volunteer Panel", () -> new VolunteerPanelView(volunteer.getId(), volunteers, pickups, events, notifications).show());
        windows("FoodBridge - Assign Volunteers", () -> new VolunteerAssignmentView(volunteers, pickups, events).show());
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

    private static void windows(String title, Runnable open) throws Exception {
        SwingUtilities.invokeLater(open);
        Thread.sleep(1500);
        boolean[] found = { false };
        boolean[] errorShown = { false };
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog dialog && dialog.isVisible()) {
                    errorShown[0] = true;
                    dialog.dispose();
                }
                if (w instanceof JFrame frame && title.equals(frame.getTitle()) && frame.isVisible()) {
                    found[0] = true;
                    frame.dispose();
                }
            }
        });
        check(found[0], title + " opens");
        check(!errorShown[0], title + " opens without an error dialog");
    }
}
