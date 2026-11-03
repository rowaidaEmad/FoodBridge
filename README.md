# FoodBridge

Food donation management system for a non-profit (CSE336 Major Task, SP 2026).
Donors give food or money, volunteers pick it up and deliver it, beneficiaries receive it,
and an admin manages users, lookup tables, events and impact reports.

> **Status: implemented.** Auth, donations and payments, receipts, beneficiaries, volunteers,
> pickup tasks, events, certificates, notifications, admin permissions, reports and the lookup tables
> all work through Swing, controllers and PostgreSQL repositories.

## Database (PostgreSQL)

1. Install PostgreSQL and make sure the service is running.
2. Create the database and tables (from this folder). The last command is optional; it adds
   starter lookup data. If `psql` is not recognized, add `C:\Program Files\PostgreSQL\<version>\bin`
   to your PATH and reopen the terminal.
   ```
   psql -U postgres -c "CREATE DATABASE nonprofit_db"
   psql -U postgres -d nonprofit_db -f database/schema.sql
   psql -U postgres -d nonprofit_db -f database/seed.sql
   ```
   To try the Volunteer module, also load its demo tasks and events. Run this once, after the
   commands above. `schema.sql` is safe to run again on an existing database.
   ```
   psql -U postgres -d nonprofit_db -f database/seed_volunteer.sql
   ```
3. Set the connection environment variables (`DB_PASSWORD` is required, the others have defaults):

   | Variable | Default |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://localhost:5432/nonprofit_db` |
   | `DB_USER` | `postgres` |
   | `DB_PASSWORD` | *(none — your postgres password)* |

   PowerShell (current window only): `$env:DB_PASSWORD = "your-password"`.
   Permanently: `setx DB_PASSWORD "your-password"`, then restart VS Code.

`database/schema.sql` is a proposed schema: `User` and `Donation` subclasses use a shared table plus
one subtype table each. Verify it with the team before building the other repositories.
`ImpactReport` is computed and has no table.

## Run

The project uses **Maven** (`pom.xml`) to download the PostgreSQL JDBC driver. Sources stay in `src/`.

**VS Code:** install JDK 17+ and the **Extension Pack for Java**, open this folder, wait for the
Maven import to finish, then open `src/foodbridge/Main.java` → **Run** (or F5 → "Run FoodBridge").

**Command line** (needs Maven installed):
```
mvn compile exec:java
```

Running opens the **Login** window. Use **Create account** to register as a Donor, Volunteer or
Beneficiary (saved in PostgreSQL), then log in. Admin accounts are not created from the screen. To make
one, register a Donor with the email below, then run this in `psql`:
```sql
DELETE FROM donors WHERE user_id = (SELECT id FROM users WHERE email = 'admin@demo.local');
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@demo.local';
INSERT INTO admins(user_id) SELECT id FROM users WHERE email = 'admin@demo.local';
```

## MVC layers

```
View ──▶ Controller ──▶ Service ──▶ Repository ──▶ DatabaseManager (Singleton) ──▶ Database
                    └─▶ Repository (for simple modules with no service)
```
Rule: views only talk to controllers; models never import views or controllers.

Models, patterns and services follow the class diagram in `docs/class-diagram`. Repositories,
controllers and views are the MVC layer around it.
`DonationDetails` and `ReceiptOptions` are small helper classes for the untyped `details`/`request`
and `options` parameters in the diagram. `PickupTaskStatus` is an enum for `PickupTask.status`.

## Packages

| Package | Contents |
|---|---|
| `models.users` | `User` (abstract), `Volunteer`, `Donor`, `Beneficiary`, `Admin` |
| `models.donations` | `Donation` (abstract), `MoneyDonation`, `FoodDonation`, `FoodItem`, `Receipt`, `DonationProcessingResult`, `DonationDetails` |
| `models.operations` | `Event`, `EventRegistration`, `Notification`, `PickupTask`, `Distribution`, `DonationRequest`, `ImpactReport`, `VolunteerHours`, `Certificate` (abstract), `ParticipationCertificate`, `AchievementCertificate` |
| `models.lookups` | CRUD lookup tables: `Area`, `DonationStatus`, `EventType`, `FoodCategory`, `Unit` |
| `models.enums` | `PickupTaskStatus` |
| `patterns.singleton` | `DatabaseManager` |
| `patterns.factory` | `DonationFactory` (Creator), `MoneyDonationFactory`, `FoodDonationFactory`; `CertificateFactory` (Creator), `ParticipationCertificateFactory`, `AchievementCertificateFactory` |
| `patterns.strategy` | `DonationProcessingStrategy`, `CashStrategy`, `CheckStrategy`, `OnlineStrategy`; `VolunteerMatchingStrategy`, `SkillMatchStrategy`, `AvailabilityMatchStrategy`, `PreferenceMatchStrategy`, `MatchCriteria` |
| `patterns.observer` | `Subject`, `EventObserver`, `EventPublisher`, `NotificationLogger`, `VolunteerObserver` |
| `patterns.decorator` | `ReceiptComponent`, `BasicReceipt`, `ReceiptDecorator`, `ThankYouDecorator`, `TaxInfoDecorator`, `ReceiptOptions`; `CertificateComponent`, `BasicCertificate`, `CertificateDecorator`, `HoursBreakdownDecorator`, `SignatureDecorator` |
| `services` | `EventService` (Observer client), `ReceiptService` (Decorator client), `DonationService` (Strategy context + Factory client), `VolunteerMatchingService` (Strategy context), `CertificateService` (Factory + Decorator client), `VolunteerService`, `PickupTaskService` |
| `repositories` | `Repository<T>` + one repository per model and lookup table, `VolunteerRepository`, `CertificateRepository`, `JdbcSupport` (shared JDBC helpers) |
| `controllers` | `Auth`, `User`, `Volunteer`, `Donation`, `Pickup`, `Beneficiary`, `Event`, `Notification`, `Report`, `Lookup` |
| `views.*` | `View` interface, `HomeView`, `SwingSupport` (shared Swing helpers) + screens in `auth`, `donor`, `volunteer`, `beneficiary`, `admin`, `lookups` |

## Phase 1 grading items → where they live

| Item | Location |
|---|---|
| Class diagram | `docs/class-diagram/foodbridge-class-diagram.png` |
| Strategy | `patterns/strategy` + `services/DonationService`, `services/VolunteerMatchingService` |
| Observer | `patterns/observer` + `services/EventService` |
| Decorator | `patterns/decorator` + `services/ReceiptService`, `services/CertificateService` |
| Factory | `patterns/factory` + `services/DonationService`, `services/CertificateService` |
| Singleton | `patterns/singleton/DatabaseManager` |
| MVC | `models` / `views` / `controllers` |
| CRUD lookup tables | `docs/volunteer-crud-matrix.md` |

Phase 2 patterns (Command, Adapter, Facade, Template Method, Iterator, State, Proxy) get their
own sub-package under `patterns/` when we start Phase 2.

## Suggested module split

| Module | Files |
|---|---|
| Users & auth | `models/users`, `UserRepository`, `AuthController`, `UserController`, `views/auth` (Login, Register, Profile), `ManageUsersView` |
| Donations (Factory + Strategy) | `models/donations` (minus Receipt), `patterns/factory`, `patterns/strategy`, `DonationService`, `DonationRepository`, `DonationController`, `AddDonationView`, `PayDonationView` |
| Receipts (Decorator) | `Receipt`, `patterns/decorator`, `ReceiptService`, `ReceiptRepository`, `ReceiptView` |
| Events & notifications (Observer) | `Event`, `EventRegistration`, `Notification`, `patterns/observer`, `EventService`, their repositories, `EventController`, `NotificationController`, event/notification views |
| Pickups, beneficiaries & reports | `PickupTask`, `Distribution`, `DonationRequest`, `ImpactReport`, their repositories, `PickupController`, `BeneficiaryController`, `ReportController`, `views/volunteer/PickupTasksView`, `views/beneficiary`, `ImpactReportView` |
| Lookups & database (Singleton) | `models/lookups`, `DatabaseManager`, `Repository<T>`, lookup repositories, `LookupController`, `views/lookups` |
| Volunteers (Strategy, Factory, Decorator, Observer) | `Volunteer`, `PickupTask`, `Event`, `EventRegistration`, `Notification`, `VolunteerHours`, the certificate classes, their patterns, services, repositories and controllers, `views/volunteer`, `VolunteerAssignmentView`, `NotificationsView` |

## Receipts (Decorator)

Donors can open **My donations (pay / cancel)**, select a donation and click **Receipt**.
Money donations must be paid first; cancelled donations cannot receive a receipt. Food donations
include their item details. Opening a receipt again reuses its saved number and issue date.

The receipt window lets the donor independently select a thank-you message and tax information.
`ReceiptService` wraps `BasicReceipt` in `ThankYouDecorator`, `TaxInfoDecorator`, or both, according
to `ReceiptOptions`. Formatting choices do not modify the saved receipt. Tax information is a
general recordkeeping note; the application has no tax deduction or food valuation rules.
Receipt database operations run on a Swing worker.

Run the workflows that join donations, pickups, events and accounts (same prerequisites as beneficiary verification):

```powershell
py tests/run-merged-tests.py
py tests/run-merged-tests.py --gui
```

Run the isolated receipt database tests (same prerequisites):

```powershell
py tests/run-receipt-tests.py
py tests/run-receipt-tests.py --gui
```

For only the decorator checks, after compiling the application to `target/receipt-classes`:

```powershell
javac --release 17 -cp target/receipt-classes -d target/receipt-classes tests/ReceiptDecoratorTest.java
java -cp target/receipt-classes ReceiptDecoratorTest
```

## Beneficiary module

After login, beneficiaries can open **My profile**, **My requests**, and **Received distributions**.
Admins can open **Manage beneficiaries** (beneficiary list, request approval/rejection, delivery
recording) and **Impact reports**. Admin accounts must already exist; registration does not create them.
Load `database/seed.sql` for the food categories and units used by request forms.

The module keeps the existing schema and domain relationships. Requests start as `PENDING` and
admins can decide each request once as `APPROVED` or `REJECTED`. Approval and delivery are independent:
recording a distribution does not automatically fulfil a request. Delivery forms select existing food
donations and optionally a volunteer. Beneficiaries can only read their own requests and deliveries.
Database operations in these screens run on Swing workers, and logout closes module windows.

Distribution quantity remains the existing donation-level scalar. Item/unit allocation and inventory
deduction are not implemented; mixed-item donations remain supported by `FoodDonation`. Reports count
distinct beneficiary accounts as households served and count delivery records per volunteer over an
inclusive date range. Unassigned deliveries count in delivery totals but have no volunteer row.
Meals display as **N/A**, because no meal-count/conversion rule has been defined.

### Beneficiary verification

With Python 3.9+, JDK 17+, PostgreSQL tools and the Maven-cached PostgreSQL JDBC jar available, run:

```powershell
python tests/run-beneficiary-tests.py
python tests/run-beneficiary-tests.py --gui
```

The runner creates a fresh PostgreSQL cluster under `target/beneficiary-verification`, loads the
existing schema and lookup seeds, runs integration assertions, then stops and deletes that cluster.
It overrides database settings only for its Java child process and never uses the application database.
`--gui` also exercises real Swing screens and writes GUI captures under that target directory.
Use `--postgres-bin` or `--jdbc-jar` to supply tools installed elsewhere; `--help` lists the options.
