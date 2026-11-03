# FoodBridge

Food donation management system for a non-profit (CSE336 Major Task, SP 2026).
Donors give food or money, volunteers pick it up and deliver it, beneficiaries receive it,
and an admin manages users, lookup tables, events and impact reports.

> **Status: skeleton only.** Every class has its fields and method signatures.
> Method bodies are `// TODO: implement` (non-void methods throw `UnsupportedOperationException`)
> so the project compiles while each team member implements their own module.
> **Done so far:** `DatabaseManager` (PostgreSQL) and the full Event Type CRUD
> (`EventType` → `EventTypeRepository` → `LookupController` → `EventTypeView`), merged from the
> `FoodBridgeColdStart` branch; and Auth: Swing login/register screens (`LoginView`, `RegisterView`,
> `HomeView`) → `AuthController` (salted PBKDF2 password hashes) → `UserRepository` (`users` +
> role subtype tables); and the Volunteer module: profiles, skills and preferences, pickup tasks,
> event registration and attendance, hours, certificates, notifications, and admin assignment.

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
