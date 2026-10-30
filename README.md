# FoodBridge

FoodBridge is a food donation management system for a food bank charity. It connects **donors**
(people and businesses with surplus food) to **beneficiaries** (families and charities in need),
with **volunteers** picking up and delivering the food. The goal is to reduce food waste and get
food to the people who need it, fairly and on time.

> Status: project skeleton. Every class has its fields and empty method stubs (`// TODO`).
> The code compiles, but the logic still needs to be written.

## Users

| Role        | What they do                                              |
|-------------|-----------------------------------------------------------|
| Donor       | Gives food or money (individual, restaurant, supermarket, company) |
| Volunteer   | Gives time and transport; picks up and delivers food      |
| Beneficiary | Receives food (family or charity)                         |
| Admin       | Runs the system: users, lookup tables, reports, events    |

A volunteer who also wants to donate food uses the donor features from the same account.

## Life of a food donation

1. A donor posts 20 meals. Status: **Posted** (no receipt yet).
2. Volunteers in that area are notified.
3. A volunteer accepts and picks up the food. Status: **Picked up**. The donor gets a receipt.
4. The volunteer delivers to a family. Status: **Delivered**. A `Distribution` record is created
   and the volunteer's hours are logged.
5. The donor gets a "your meals reached a family" update.

A money donation gets its receipt right after payment succeeds.

## Requirements

- **JDK 17 or newer** (JDK 21 LTS recommended, e.g. Temurin from adoptium.net)
- **VS Code** with the **Extension Pack for Java** (Microsoft), or any Java IDE

Check your JDK:

```
java -version
javac -version
```

## How to run

### VS Code

1. File → Open Folder → choose `foodbridge-java` (the folder that contains `src`).
2. Wait for the Java extension to finish loading (bottom status bar).
3. Open `src/foodbridge/Main.java` and click **Run**, or press **F5** and pick "Run FoodBridge".

The `.vscode` folder already sets `src` as the source folder and `foodbridge.Main` as the main class.

### Command line

From the `foodbridge-java` folder:

```
# Windows (PowerShell)
javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp bin foodbridge.Main

# macOS / Linux
javac -d bin $(find src -name "*.java")
java -cp bin foodbridge.Main
```

You should see `FoodBridge started`.

## Project structure (MVC)

```
src/foodbridge/
├── Main.java
├── models/
│   ├── users/        User (abstract), Donor, Volunteer, Beneficiary, Admin
│   ├── donations/    Donation (abstract), FoodDonation, MoneyDonation, FoodItem, Receipt
│   ├── operations/   DonationRequest, PickupTask, Distribution, ImpactReport, Event
│   ├── lookups/      FoodCategory, Unit, Area, DonationStatus
│   └── enums/        Role, DonorType, BeneficiaryType, PaymentMethod
├── controllers/      AuthController, UserController, DonationController, PickupController,
│                     BeneficiaryController, ReportController, EventController, LookupController
└── views/
    ├── auth/         LoginView, RegisterView
    ├── donor/        MyDonationsView, AddDonationView, TrackStatusView, MyReceiptsView
    ├── volunteer/    AvailablePickupsView, MyTasksView, MyHoursView
    ├── beneficiary/  RequestFoodView, ReceivedDonationsView
    ├── admin/        ImpactReportView, EventsView
    └── lookups/      FoodCategoryView, UnitView, AreaView, DonationStatusView
```

See [STRUCTURE.md](STRUCTURE.md) for the full file tree.

**How the layers talk:** views call controllers, controllers use models, and models never import
views or controllers.

- **Models** hold the data and the rules about it (15 classes + 4 lookup tables).
- **Controllers** handle a user action and update the models (8 classes).
- **Views** show screens and pass input to controllers (6 groups).
- **Lookups** are admin-editable tables (categories, units, areas, statuses).
- **Enums** are fixed values that never change at runtime (optional; can be replaced by Strings).

## Key relationships

- `Donor`, `Volunteer`, `Beneficiary` and `Admin` extend `User`.
- `FoodDonation` and `MoneyDonation` extend `Donation`.
- A `Donor` has many `Donation`s; a `FoodDonation` is composed of many `FoodItem`s.
- Each `Donation` has one `Receipt`, issued at pickup (food) or after payment (money).
- A `PickupTask` links one `FoodDonation`, one `Volunteer` and one `Beneficiary`.
- Marking a `PickupTask` delivered creates one `Distribution` and logs the volunteer's hours.
- `ImpactReport` is calculated from `Distribution` records; it stores no new data.
- `Donation`, `FoodItem` and `DonationRequest` reference the lookup tables.

## Functional requirements

1. Users can register, log in and edit their profile.
2. Donors can be individuals, restaurants, supermarkets/bakeries or companies.
3. Donors can post surplus food with quantity, category and expiry date.
4. Donors can set a pickup time window for each donation.
5. Donors can label food items with allergens.
6. Donors can make money donations online, by cash at pickup or by bank transfer.
7. Donors can track donation status: posted, picked up, delivered.
8. Donors receive a receipt when their food is picked up, or right after a money payment.
9. Donors are notified when their food reaches a family.
10. Volunteers can register with their availability, area and transport.
11. Volunteers can view available pickups filtered by area.
12. Volunteers can accept or decline a pickup task.
13. Volunteers can mark a task as picked up and then delivered.
14. The system logs volunteer hours and generates certificates.
15. A volunteer can also donate food using the donor features.
16. Beneficiaries can submit food requests with quantity and category.
17. Beneficiaries can view the donations they received.
18. The system creates a distribution record whenever a task is marked delivered.
19. Staff can view impact reports: meals per family, families served per month, deliveries per volunteer.
20. Staff can mark donations that expired before pickup.
21. Staff can create food drive events and manage registration and attendance.
22. Users receive email, SMS or in-app notifications when a donation or task they are part of changes.
23. Admins can manage food categories, units, areas and donation statuses.
