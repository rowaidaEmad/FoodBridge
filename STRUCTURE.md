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
        │   │   ├── ImpactReport.java
        │   │   ├── VolunteerHours.java
        │   │   ├── Certificate.java              (abstract) Factory: Product
        │   │   ├── ParticipationCertificate.java Factory: ConcreteProduct
        │   │   └── AchievementCertificate.java   Factory: ConcreteProduct
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
        │   │   ├── FoodDonationFactory.java      ConcreteCreator
        │   │   ├── CertificateFactory.java       (abstract) Creator
        │   │   ├── ParticipationCertificateFactory.java  ConcreteCreator
        │   │   └── AchievementCertificateFactory.java    ConcreteCreator
        │   ├── strategy/
        │   │   ├── DonationProcessingStrategy.java  (interface)
        │   │   ├── CashStrategy.java
        │   │   ├── CheckStrategy.java
        │   │   ├── OnlineStrategy.java
        │   │   ├── VolunteerMatchingStrategy.java   (interface)
        │   │   ├── SkillMatchStrategy.java
        │   │   ├── AvailabilityMatchStrategy.java
        │   │   ├── PreferenceMatchStrategy.java
        │   │   └── MatchCriteria.java            helper
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
        │       ├── ReceiptOptions.java           helper
        │       ├── CertificateComponent.java     (interface)
        │       ├── BasicCertificate.java         ConcreteComponent
        │       ├── CertificateDecorator.java     (abstract) Decorator
        │       ├── HoursBreakdownDecorator.java  ConcreteDecorator
        │       └── SignatureDecorator.java       ConcreteDecorator
        │
        ├── services/
        │   ├── EventService.java                 Observer client
        │   ├── DonationService.java              Strategy context + Factory client
        │   ├── ReceiptService.java               Decorator client
        │   ├── VolunteerMatchingService.java     Strategy context
        │   ├── CertificateService.java           Factory + Decorator client
        │   ├── VolunteerService.java
        │   └── PickupTaskService.java
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
        │   ├── UnitRepository.java
        │   ├── VolunteerRepository.java
        │   ├── CertificateRepository.java
        │   └── JdbcSupport.java                  shared JDBC helpers
        │
        ├── controllers/
        │   ├── AuthController.java
        │   ├── UserController.java
        │   ├── VolunteerController.java
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
            ├── HomeView.java
            ├── SwingSupport.java                 shared Swing helpers
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
            │   ├── VolunteerPanelView.java
            │   ├── VolunteerProfileView.java
            │   ├── PickupTasksView.java
            │   ├── EventRegistrationView.java
            │   └── HoursCertificateView.java
            ├── beneficiary/
            │   ├── BeneficiaryProfileView.java
            │   ├── DonationRequestView.java
            │   └── ReceivedDistributionsView.java
            ├── admin/
            │   ├── ManageBeneficiariesView.java
            │   ├── ManageUsersView.java
            │   ├── ManageEventsView.java
            │   ├── ImpactReportView.java
            │   └── VolunteerAssignmentView.java
            └── lookups/
                ├── AreaView.java
                ├── DonationStatusView.java
                ├── EventTypeView.java
                ├── FoodCategoryView.java
                └── UnitView.java
```

Counts: 29 models + 32 pattern classes + 7 services + 18 repositories + 10 controllers + 32 views + Main = 129 files.
Integration and Swing smoke tests live under `tests/`; generated outputs stay under `target/`.

Package name of each file = its folder, e.g. `src/foodbridge/models/users/Donor.java` starts with
`package foodbridge.models.users;`

Rule: views call controllers, controllers call services/repositories, repositories use
`DatabaseManager.getInstance()`. Models never import views or controllers.
