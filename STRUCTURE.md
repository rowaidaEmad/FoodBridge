# FoodBridge – Java project structure (MVC, matches the class diagram)

```
FoodBridge/
└── src/
    └── foodbridge/
        ├── Main.java
        │
        ├── models/
        │   ├── users/
        │   │   ├── User.java                     (abstract)
        │   │   ├── Volunteer.java                extends User
        │   │   ├── Donor.java                    extends User
        │   │   ├── Beneficiary.java              extends User
        │   │   └── Admin.java                    extends User
        │   ├── donations/
        │   │   ├── Donation.java                 (abstract) Factory: Product
        │   │   ├── MoneyDonation.java            Factory: ConcreteProduct
        │   │   ├── FoodDonation.java             Factory: ConcreteProduct (contains FoodItem)
        │   │   ├── FoodItem.java
        │   │   ├── Receipt.java
        │   │   ├── DonationProcessingResult.java
        │   │   └── DonationDetails.java          helper (not in diagram)
        │   ├── operations/
        │   │   ├── Event.java
        │   │   ├── EventRegistration.java
        │   │   ├── Notification.java
        │   │   ├── PickupTask.java
        │   │   ├── Distribution.java
        │   │   ├── DonationRequest.java
        │   │   └── ImpactReport.java
        │   ├── lookups/                          (CRUD tables, edited by Admin)
        │   │   ├── Area.java
        │   │   ├── DonationStatus.java
        │   │   ├── EventType.java
        │   │   ├── FoodCategory.java
        │   │   └── Unit.java
        │   └── enums/
        │       └── PickupTaskStatus.java
        │
        ├── patterns/
        │   ├── singleton/
        │   │   └── DatabaseManager.java
        │   ├── factory/
        │   │   ├── DonationFactory.java          (abstract) Creator
        │   │   ├── MoneyDonationFactory.java     ConcreteCreator
        │   │   └── FoodDonationFactory.java      ConcreteCreator
        │   ├── strategy/
        │   │   ├── DonationProcessingStrategy.java  (interface)
        │   │   ├── CashStrategy.java
        │   │   ├── CheckStrategy.java
        │   │   └── OnlineStrategy.java
        │   ├── observer/
        │   │   ├── Subject.java                  (interface)
        │   │   ├── EventObserver.java            (interface)
        │   │   ├── EventPublisher.java           ConcreteSubject
        │   │   ├── NotificationLogger.java       ConcreteObserver
        │   │   └── VolunteerObserver.java        ConcreteObserver
        │   └── decorator/
        │       ├── ReceiptComponent.java         (interface)
        │       ├── BasicReceipt.java             ConcreteComponent
        │       ├── ReceiptDecorator.java         (abstract) Decorator
        │       ├── ThankYouDecorator.java        ConcreteDecorator
        │       ├── TaxInfoDecorator.java         ConcreteDecorator
        │       └── ReceiptOptions.java           helper (not in diagram)
        │
        ├── services/
        │   ├── EventService.java                 Observer client
        │   ├── DonationService.java              Strategy context + Factory client
        │   └── ReceiptService.java               Decorator client
        │
        ├── repositories/
        │   ├── Repository.java                   (generic CRUD interface)
        │   ├── UserRepository.java
        │   ├── DonationRepository.java
        │   ├── ReceiptRepository.java
        │   ├── PickupTaskRepository.java
        │   ├── DistributionRepository.java
        │   ├── DonationRequestRepository.java
        │   ├── EventRepository.java
        │   ├── EventRegistrationRepository.java
        │   ├── NotificationRepository.java
        │   ├── AreaRepository.java
        │   ├── DonationStatusRepository.java
        │   ├── EventTypeRepository.java
        │   ├── FoodCategoryRepository.java
        │   └── UnitRepository.java
        │
        ├── controllers/
        │   ├── AuthController.java
        │   ├── UserController.java
        │   ├── DonationController.java
        │   ├── PickupController.java
        │   ├── BeneficiaryController.java
        │   ├── EventController.java
        │   ├── NotificationController.java
        │   ├── ReportController.java
        │   └── LookupController.java
        │
        └── views/
            ├── View.java                         (interface)
            ├── ModuleView.java                   shared Swing worker and table helpers
            ├── auth/
            │   ├── LoginView.java
            │   ├── RegisterView.java
            │   ├── ProfileView.java
            │   └── NotificationsView.java
            ├── donor/
            │   ├── AddDonationView.java
            │   ├── PayDonationView.java
            │   └── ReceiptView.java
            ├── volunteer/
            │   ├── PickupTasksView.java
            │   └── EventRegistrationView.java
            ├── beneficiary/
            │   ├── BeneficiaryProfileView.java
            │   ├── DonationRequestView.java
            │   └── ReceivedDistributionsView.java
            ├── admin/
            │   ├── ManageBeneficiariesView.java
            │   ├── ManageUsersView.java
            │   ├── ManageEventsView.java
            │   └── ImpactReportView.java
            └── lookups/
                ├── AreaView.java
                ├── DonationStatusView.java
                ├── EventTypeView.java
                ├── FoodCategoryView.java
                └── UnitView.java
```

The beneficiary module adds two screens and the shared `ModuleView` helper.
Integration and Swing smoke tests live under `tests/`; generated outputs stay under `target/`.

Package name of each file = its folder, e.g. `src/foodbridge/models/users/Donor.java` starts with
`package foodbridge.models.users;`

Rule: views call controllers, controllers call services/repositories, repositories use
`DatabaseManager.getInstance()`. Models never import views or controllers.

## Volunteer module files

Added to the tree above:

```
src/foodbridge/
├── models/operations/      VolunteerHours, Certificate (abstract), ParticipationCertificate, AchievementCertificate
├── patterns/strategy/      VolunteerMatchingStrategy, SkillMatchStrategy, AvailabilityMatchStrategy, PreferenceMatchStrategy, MatchCriteria
├── patterns/factory/       CertificateFactory (abstract), ParticipationCertificateFactory, AchievementCertificateFactory
├── patterns/decorator/     CertificateComponent, BasicCertificate, CertificateDecorator (abstract), HoursBreakdownDecorator, SignatureDecorator
├── services/               VolunteerMatchingService, CertificateService, VolunteerService, PickupTaskService
├── repositories/           VolunteerRepository, CertificateRepository, JdbcSupport
├── controllers/            VolunteerController
└── views/                  SwingSupport, admin/VolunteerAssignmentView,
                            volunteer/VolunteerPanelView, volunteer/VolunteerProfileView, volunteer/HoursCertificateView
database/seed_volunteer.sql demo data and areas
docs/volunteer-crud-matrix.md
tests/VolunteerModuleTest.java, tests/run-volunteer-tests.py
```
