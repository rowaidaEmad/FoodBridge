# FoodBridge

Food donation management system for a non-profit (CSE336 Major Task, SP 2026).
Donors give food or money, volunteers pick it up and deliver it, beneficiaries receive it,
and an admin manages users, lookup tables, events and impact reports.

> **Status: skeleton only.** Every class has its fields and method signatures.
> Method bodies are `// TODO: implement` (non-void methods throw `UnsupportedOperationException`)
> so the project compiles while each team member implements their own module.

## Run (VS Code)

1. Install JDK 17+ and the **Extension Pack for Java**.
2. File → Open Folder → this folder (the one that contains `src`).
3. Open `src/foodbridge/Main.java` → **Run** (or F5 → "Run FoodBridge").

Command line:
```
javac -d bin $(find src -name "*.java")      # PowerShell: javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp bin foodbridge.Main
```

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
