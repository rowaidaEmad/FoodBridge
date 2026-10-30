# FoodBridge – Java project structure (MVC)

```
FoodBridge/
└── src/
    └── foodbridge/
        ├── Main.java
        │
        ├── models/
        │   ├── users/
        │   │   ├── User.java              (abstract)
        │   │   ├── Donor.java             extends User
        │   │   ├── Volunteer.java         extends User
        │   │   ├── Beneficiary.java       extends User
        │   │   └── Admin.java             extends User
        │   │
        │   ├── donations/
        │   │   ├── Donation.java          (abstract)
        │   │   ├── FoodDonation.java      extends Donation  (has many FoodItem)
        │   │   ├── MoneyDonation.java     extends Donation
        │   │   ├── FoodItem.java
        │   │   └── Receipt.java
        │   │
        │   ├── operations/
        │   │   ├── DonationRequest.java
        │   │   ├── PickupTask.java
        │   │   ├── Distribution.java
        │   │   ├── ImpactReport.java
        │   │   └── Event.java
        │   │
        │   ├── lookups/                   (CRUD tables, edited by Admin)
        │   │   ├── FoodCategory.java
        │   │   ├── Unit.java
        │   │   ├── Area.java
        │   │   └── DonationStatus.java
        │   │
        │   └── enums/                     (fixed values, not tables – optional)
        │       ├── Role.java
        │       ├── DonorType.java
        │       ├── BeneficiaryType.java
        │       └── PaymentMethod.java
        │
        ├── controllers/
        │   ├── AuthController.java
        │   ├── UserController.java
        │   ├── DonationController.java
        │   ├── PickupController.java
        │   ├── BeneficiaryController.java
        │   ├── ReportController.java
        │   ├── EventController.java
        │   └── LookupController.java
        │
        └── views/
            ├── auth/
            │   ├── LoginView.java
            │   └── RegisterView.java
            ├── donor/
            │   ├── MyDonationsView.java
            │   ├── AddDonationView.java
            │   ├── TrackStatusView.java
            │   └── MyReceiptsView.java
            ├── volunteer/
            │   ├── AvailablePickupsView.java
            │   ├── MyTasksView.java
            │   └── MyHoursView.java
            ├── beneficiary/
            │   ├── RequestFoodView.java
            │   └── ReceivedDonationsView.java
            ├── admin/
            │   ├── ImpactReportView.java
            │   └── EventsView.java
            └── lookups/
                ├── FoodCategoryView.java
                ├── UnitView.java
                ├── AreaView.java
                └── DonationStatusView.java
```

Counts: 15 models + 4 lookups + 8 controllers + 17 view files in 6 groups + 4 enums + Main = 49 files.

Package name of each file = its folder, e.g. `src/foodbridge/models/users/Donor.java` starts with
`package foodbridge.models.users;`

Rule of thumb: views call controllers, controllers use models, models never import views or controllers.
