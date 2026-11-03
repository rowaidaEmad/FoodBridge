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
> role subtype tables).

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
Beneficiary (saved in PostgreSQL), then log in. Admin accounts are not created from the screen.

## MVC layers

```
View ──▶ Controller ──▶ Service (EventService, DonationService, ReceiptService) ──▶ Repository ──▶ DatabaseManager (Singleton) ──▶ Database
                    └─▶ Repository (for modules with no service in the diagram)
```
Rule: views only talk to controllers; models never import views or controllers.

Models, patterns and the three services follow the Lucidchart class diagram exactly (same fields,
same method names). Repositories, controllers and views are the MVC layer around it.
`DonationDetails` and `ReceiptOptions` are small helper classes for the untyped `details`/`request`
and `options` parameters in the diagram. `PickupTaskStatus` is an enum for `PickupTask.status`.

## Packages

| Package | Contents |
|---|---|
| `models.users` | `User` (abstract), `Volunteer`, `Donor`, `Beneficiary`, `Admin` |
| `models.donations` | `Donation` (abstract), `MoneyDonation`, `FoodDonation`, `FoodItem`, `Receipt`, `DonationProcessingResult`, `DonationDetails` |
| `models.operations` | `Event`, `EventRegistration`, `Notification`, `PickupTask`, `Distribution`, `DonationRequest`, `ImpactReport` |
| `models.lookups` | CRUD lookup tables: `Area`, `DonationStatus`, `EventType`, `FoodCategory`, `Unit` |
| `models.enums` | `PickupTaskStatus` |
| `patterns.singleton` | `DatabaseManager` |
| `patterns.factory` | `DonationFactory` (Creator), `MoneyDonationFactory`, `FoodDonationFactory` |
| `patterns.strategy` | `DonationProcessingStrategy`, `CashStrategy`, `CheckStrategy`, `OnlineStrategy` |
| `patterns.observer` | `Subject`, `EventObserver`, `EventPublisher`, `NotificationLogger`, `VolunteerObserver` |
| `patterns.decorator` | `ReceiptComponent`, `BasicReceipt`, `ReceiptDecorator`, `ThankYouDecorator`, `TaxInfoDecorator`, `ReceiptOptions` |
| `services` | `EventService` (Observer client), `ReceiptService` (Decorator client), `DonationService` (Strategy context + Factory client) |
| `repositories` | `Repository<T>` + one repository per model and lookup table |
| `controllers` | `Auth`, `User`, `Donation`, `Pickup`, `Beneficiary`, `Event`, `Notification`, `Report`, `Lookup` |
| `views.*` | `View` interface + screens in `auth`, `donor`, `volunteer`, `beneficiary`, `admin`, `lookups` |

## Phase 1 grading items → where they live

| Item | Location |
|---|---|
| Class diagram | Lucidchart (this code mirrors it) |
| Strategy | `patterns/strategy` + `services/DonationService` |
| Observer | `patterns/observer` + `services/EventService` |
| Decorator | `patterns/decorator` + `services/ReceiptService` |
| Factory | `patterns/factory` + `services/DonationService` |
| Singleton | `patterns/singleton/DatabaseManager` |
| MVC | `models` / `views` / `controllers` |
| CRUD lookup tables | `models/lookups` + their repositories + `controllers/LookupController` + `views/lookups` |

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
