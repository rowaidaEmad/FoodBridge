# FoodBridge: Finalization

This records how the volunteer module was merged into `donation-beneficiary`, what was changed and why, how the
result was checked, and what the team should discuss.

| Item | Value |
|---|---|
| Target branch | `donation-beneficiary`, starting at `67c25fa` |
| Merged in | `Merged` at `498765b` |
| Final class diagram | `docs/class-diagram/FoodBridge Class Diagram.svg` |
| Rule followed | Keep the team's logic, classes and design patterns in everything except the volunteer module |

## Contents

1. [Summary](#1-summary)
2. [Where the volunteer module ends](#2-where-the-volunteer-module-ends)
3. [How the merge was done](#3-how-the-merge-was-done)
4. [Every change to a file on the team's branch](#4-every-change-to-a-file-on-the-teams-branch)
5. [What changed in the volunteer module](#5-what-changed-in-the-volunteer-module)
6. [Behaviour that differs from the `Merged` branch](#6-behaviour-that-differs-from-the-merged-branch)
7. [Check against the final class diagram](#7-check-against-the-final-class-diagram)
8. [Test results](#8-test-results)
9. [Points for the team to discuss](#9-points-for-the-team-to-discuss)
10. [Setting up an existing database](#10-setting-up-an-existing-database)
11. [How to re-check this yourself](#11-how-to-re-check-this-yourself)

---

## 1. Summary

The team's work is unchanged, and the volunteer module now sits on top of it.

| Check | Result |
|---|---|
| Files on the team's branch | 119 |
| Byte-identical to the team's version (before this document's README and STRUCTURE additions) | 103 |
| Changed by adding lines only | 6, including README and STRUCTURE (section 4) |
| Empty stubs filled in; no written logic replaced | 6 (section 4) |
| Volunteer-owned files that replaced a stub | 6 (section 4) |
| Team files removed, or changed in any other way | **0** |
| The team's own tests | Unchanged, and all pass |
| Volunteer classes matching the final diagram | 21 of 21 |
| Team classes matching the final diagram | 39 of 42; the other 3 are for the team to decide (section 7) |

**One feature was dropped:** volunteers can no longer donate. See [section 6](#6-behaviour-that-differs-from-the-merged-branch).

---

## 2. Where the volunteer module ends

The boundary comes from the module table in `README.md`, `VOLUNTEER_MODULE_DOCUMENTATION.md` and the volunteer
section of the final diagram.

| Owner | Classes |
|---|---|
| **Volunteer module** | `Volunteer`, `PickupTask`, `PickupTaskStatus`, `VolunteerHours`; Strategy #2 (`VolunteerMatchingService`, `VolunteerMatchingStrategy`, the 3 match strategies, `MatchCriteria`); Factory #2 and Decorator #2 (`CertificateService`, the certificate factories, products and decorators); `VolunteerService`, `PickupTaskService`, `VolunteerRepository`, `PickupTaskRepository`, `CertificateRepository`, `JdbcSupport`, `VolunteerController`, `PickupController`, `SwingSupport`, and the volunteer and assignment screens |
| **The team** | Users and auth; donations, payments and receipts; beneficiaries and reports; **events and notifications, including the Observer classes**; lookups; `DatabaseManager`; admin permissions |

**Why events are on the team's side:** the README lists "Events & notifications (Observer)" as its own module, and a
teammate ported that work from `FoodBridgeColdStart`. The volunteer module *uses* events, but does not own them.

**Pickups:** the README lists `PickupTask` with pickups and reports, but on the team's branch `PickupTask`,
`PickupController`, `PickupTaskRepository` and `PickupTasksView` were empty stubs. They carry the volunteer workflow,
so they are treated as volunteer-owned. Nothing a teammate had written was replaced.

---

## 3. How the merge was done

`Merged` already contained all of `donation-beneficiary`, so a normal merge would have **fast-forwarded** and replaced
the team's code with the combined versions made earlier. That is exactly what was not wanted.

Instead:

1. A local `donation-beneficiary` branch was created from `origin/donation-beneficiary`.
2. `Merged` was merged with `--no-ff --no-commit`, which keeps both histories and stops before committing.
3. Every team file was restored to the team's exact version.
4. The volunteer module was changed to use the team's classes, instead of the team's classes being changed for it.
5. Only the few additions that could not be avoided went into team files.

The `Merged` branch was not changed, so anything left out here can still be recovered from it.

---

## 4. Every change to a file on the team's branch

### Lines added only: no existing line changed

| File | Added | Why |
|---|---|---|
| `database/schema.sql` | 7 lines | Volunteer tables, and the `required_skill` columns. Safe to run again. |
| `src/foodbridge/models/lookups/Area.java` | `setId`, `toString` | `AreaRepository` sets the new id, and area dropdowns show the name |
| `src/foodbridge/repositories/UserRepository.java` | `updateAccount` | Lets `UserController` save contact details without using `update` (see [section 9](#9-points-for-the-team-to-discuss)) |
| `src/foodbridge/views/HomeView.java` | 21 lines | Menu entries: Volunteer panel, Assign volunteers, Manage users, Areas, Contact details; logout also closes the volunteer panel's windows |
| `README.md` | Volunteer sections | Documentation |
| `STRUCTURE.md` | Volunteer file list | Documentation |

### Empty stubs filled in: only `TODO` bodies were replaced

These had no logic to preserve. They were filled in on request earlier, so they were kept. They can be reverted
individually if their owner would rather write them.

| File | Now does |
|---|---|
| `controllers/LookupController.java` | Area add, list, update and delete |
| `controllers/UserController.java` | Contact details, email, activate and deactivate, list users |
| `repositories/AreaRepository.java` | Area SQL |
| `views/admin/ManageUsersView.java` | Admin user management screen |
| `views/auth/ProfileView.java` | Contact details screen for every role |
| `views/lookups/AreaView.java` | Area management screen |

### Volunteer-owned files that replaced a stub

| File | Note |
|---|---|
| `controllers/PickupController.java` | Stub replaced |
| `models/operations/PickupTask.java` | Stub replaced; signatures follow the diagram |
| `repositories/PickupTaskRepository.java` | Stub replaced |
| `views/volunteer/PickupTasksView.java` | Stub replaced |
| `models/users/Volunteer.java` | Their code kept; 39 lines added |
| `models/enums/PickupTaskStatus.java` | `AVAILABLE` became `PENDING`, because the team's `schema.sql` requires `PENDING` |

### Restored to the team's exact version

These had been changed on `Merged`, and are now byte-identical to the team's branch:

`AuthController`, `DonationController`, `EventController`, `DonationDetails`, `EventType`, `Distribution`, `Event`,
`EventRegistration`, `User`, `DonationRepository`, `EventRegistrationRepository`, `EventRepository`, `DonationService`,
`EventService`, `ManageEventsView`, `NotificationsView`, `RegisterView`, `AddDonationView`, `EventRegistrationView`,
`database/seed.sql`.

---

## 5. What changed in the volunteer module

### To match the final diagram

| Class | Before, on `Merged` | Now |
|---|---|---|
| `PickupTask` | `assign(v): boolean` | `assignVolunteer(v): void`; throws if the task is not pending |
| `PickupTask` | `markDelivered(): Distribution` | `markDelivered(): void`; `PickupTaskRepository.completeDelivery` already writes the distribution |

### To use the team's code without changing it

| Need | Before, on `Merged` | Now |
|---|---|---|
| Food donations become pickup tasks | `DonationService` called `addForDonation` | `PickupTaskRepository.openTasksForNewFoodDonations()` opens a task for each Posted food donation without one. It runs when tasks are listed. `DonationService` is untouched. |
| Attendance with hours | `EventService.markAttendance`, `EventRegistrationRepository.saveAttended` | `VolunteerService.markAttendance`, `VolunteerRepository.saveAttended`, reached through `VolunteerController.markAttended` |
| Required skill on events | `Event.requiredSkill`, `EventRepository` | `VolunteerRepository.findEventRequiredSkill` reads the volunteer module's own column. `Event` is untouched. |
| Checking that skill | `EventService.registerVolunteer` | `VolunteerService.requireEventSkill`, used by the admin Assign screen |
| Volunteer's area | `RegisterView`, `AuthController`, `UserRepository`, `User.setArea` | Chosen on `VolunteerProfileView`, saved by `VolunteerRepository`; `Volunteer.setArea` added |
| Area demo data | `database/seed.sql` | `database/seed_volunteer.sql` |
| Event and notification screens | Changed to take an id | `VolunteerPanelView` passes the `Volunteer` that the team's constructors expect |
| Announcing and listing registrations | `announceChange(int, ...)`, `listRegistrationsForEvent` | The team's `announceChange(Event, ...)` and `listRegistrations(eventId)` |

### Tests

`tests/MergedWorkflowTest.java` and `tests/run-merged-tests.py` tested the old combined code, so they were replaced by
`tests/VolunteerModuleTest.java` and `tests/run-volunteer-tests.py`.

---

## 6. Behaviour that differs from the `Merged` branch

| # | Behaviour | Why | If you want it back |
|---|---|---|---|
| 1 | **Volunteers can no longer donate** | It needs `DonationRepository.loadDonor` to accept non-donors, which changes the team's logic. It is also not on the diagram, where a `Donor` makes a `Donation`. | Discuss with the donation owner; the code is on `Merged` |
| 2 | **Volunteers choose their area on their profile**, not when registering | Registration is the auth module's code | Discuss with the auth owner |
| 3 | **An event's required skill is not checked when a volunteer registers themselves** | That screen and `EventService` are the team's. Ranking and admin assignment still respect it. | Add a check in `EventService.registerVolunteer` |
| 4 | **An event's required skill can only be set by SQL or demo data** | `ManageEventsView` is the team's and has no such field | Add a field there |
| 5 | **Pickup tasks open when tasks are next listed**, not at the moment of donating | `DonationService` is unchanged | Call the repository from `DonationService` |
| 6 | **The team's `AddDonationView` has no area field**, so new donations have no area, and are offered to volunteers in every area | That screen is the team's | Add a field there |
| 7 | **The admin screens for events and permissions are the team's** | They replace the combined versions from `Merged` | None needed |

**Improved while merging:** a food donation that has been cancelled is no longer offered to volunteers.

---

## 7. Check against the final class diagram

Every class box on the diagram was read from the SVG. Its text is stored as shapes, so the shapes were matched to
letters first. Each field and method was then compared with the Java source.

| Group | Classes | Match |
|---|---|---|
| Volunteer module | 21 | **21** |
| The team | 42 | **39** |

### Differences in the team's classes: not changed

These are for the team to decide. Either the code or the diagram should change.

| Class | Diagram says | Code says |
|---|---|---|
| `DonationService` | `createDonation(type, donor, details): Donation` | `createDonation(type, request): int` |
| `FoodItem` | `quantity: BigDecimal`, `getQuantity(): BigDecimal` | `double` |
| `MoneyDonation` | `amount: BigDecimal`, `getAmount(): BigDecimal` | `double` |
| `MoneyDonation` | `markAsPaid(): void` | `markAsPaid(transactionId: String): void` |

### In the code but not on the diagram

**Volunteer members.** The diagram's own classes need these, so they were kept.

| Class | Members | Needed by |
|---|---|---|
| `Volunteer` | `skills`, `preferences`, `hasSkill`, `prefers` | `SkillMatchStrategy`, `PreferenceMatchStrategy` on the diagram |
| `PickupTask` | `requiredSkill` | `MatchCriteria.requiredSkill` on the diagram |
| `PickupTask` | `area`, `itemsSummary`, `MINIMUM_HOURS` | Area filter, task list, and the 15-minute minimum |
| `Certificate` | `volunteer` | The diagram's "issued to" line |

**Classes.** The diagram does not draw views, controllers or repositories, so those are expected. These other classes
are also not on it:

| Class | Owner |
|---|---|
| `VolunteerService`, `PickupTaskService`, `PickupTaskStatus` | Volunteer module (`PickupTask.status` does name `PickupTaskStatus` on the diagram) |
| `AdminService`, `DonationDetails`, `ReceiptOptions` | The team |

### Relationships

59 connectors were read, and each was checked for a reference between its two classes in the code.

- All volunteer relationships are present: ranks, scores, uses, creates, issues for, issued to, logged for, builds,
  formats, wraps and hoursEntries.
- Three have no direct class reference, all by design:
  - `DatabaseManager` to the `Database` shape, which is not a class.
  - `Notification` to `User`, which is stored as `recipientId: int`.
  - `ImpactReport` to `Distribution`, which `ReportController` computes.

**Limit of this check:** many methods on the diagram show parameters without types, such as `removeItem(item)`. For
those, only the name and the number of parameters could be compared. For relationships, the check confirms that the
reference exists, not that the multiplicity is right.

---

## 8. Test results

Every suite ran on its own throwaway PostgreSQL database, with real Swing windows opened.

| Suite | Owner | Result |
|---|---|---|
| Beneficiary | The team | **42** database checks and **12** GUI checks passed |
| Receipt | The team | **32** database checks, **6** GUI checks and the decorator checks passed |
| Volunteer module | New | **75** database checks and **11** GUI checks passed |

The volunteer suite covers:

- A donation saved by the team's code becoming a task, offered by area and qualification, and not offered once cancelled
- Accepting, declining, assigning, picking up and delivering, then the team's beneficiary code reading the delivery
- Registering through the team's event code, attendance and hours, and the team's Observer notifying volunteers
- All 3 ranking strategies, and certificates at 10 and 50 hours with their decorators
- Contact details saved without disturbing donations, household size or skills
- `database/seed_volunteer.sql` loading correctly, and each role's Home menu

**Not tested:**

- Building through Maven, which is not installed here. `javac` was used, with the same PostgreSQL driver.
- Your real `nonprofit_db`.
- A person clicking through every form. The GUI checks confirm that screens open without errors, and which menu each role gets.

---

## 9. Points for the team to discuss

**Existing issues noticed in the team's code. None were changed.**

| # | Where | Point |
|---|---|---|
| 1 | `UserRepository.update` | It deletes and re-inserts the user's role row. Saving a donor who has donations would fail, because `donations.donor_id` references that row. Nothing calls it on existing users today, and `UserController` uses the new `updateAccount` to stay clear of it. |
| 2 | `DonationService` line 116 | A leftover `TODO` about issuing a receipt. Receipts are in fact created from the donor's screen, so it can probably go. |
| 3 | `views/donor/ReceiptView.java` | It imports `patterns.decorator.ReceiptOptions`, so one view knows a pattern class, while the README says views call controllers. Minor. |
| 4 | `README.md` status block | It still says food donation creation belongs to the donation module, which is now done. Left as written. |

**Decisions needed:**

| # | Decision |
|---|---|
| 1 | The 4 diagram differences in [section 7](#7-check-against-the-final-class-diagram): update the code, or the diagram? |
| 2 | Should `Volunteer.skills`, `PickupTask.requiredSkill` and similar be added to the diagram? The strategies on it depend on them. |
| 3 | Should `VolunteerService`, `PickupTaskService`, `AdminService`, `DonationDetails` and `ReceiptOptions` be added to the diagram? |
| 4 | Should volunteers be able to donate? This needs one small change in `DonationRepository.loadDonor` ([section 6](#6-behaviour-that-differs-from-the-merged-branch)). |
| 5 | Are the 6 filled-in stubs in [section 4](#4-every-change-to-a-file-on-the-teams-branch) acceptable to whoever owns those modules? |
| 6 | `docs/class-diagram/foodbridge-class-diagram.*` and `generate_class_diagram.py` came in from `Merged`. They describe the earlier combined code, not this branch, and are superseded by the Lucid diagram. They were kept rather than deleted, and should probably be removed so nobody presents the wrong diagram. |

---

## 10. Setting up an existing database

```powershell
psql -U postgres -d nonprofit_db -f database/schema.sql
psql -U postgres -d nonprofit_db -f database/seed.sql
```

`schema.sql` is safe to run again. To add the volunteer demo data and the areas, run this **once**:

```powershell
psql -U postgres -d nonprofit_db -f database/seed_volunteer.sql
```

Without it there are no areas until an admin adds them on the Areas screen.

---

## 11. How to re-check this yourself

See everything this merge changed compared with the team's branch:

```powershell
git diff --stat 67c25fa HEAD
```

See one file. For example, this shows only additions to `HomeView`:

```powershell
git diff 67c25fa HEAD -- src/foodbridge/views/HomeView.java
```

Confirm a team file is untouched. This prints nothing when the file is identical:

```powershell
git diff 67c25fa HEAD -- src/foodbridge/services/DonationService.java
```

Run all the tests:

```powershell
py tests/run-beneficiary-tests.py --gui
py tests/run-receipt-tests.py --gui
py tests/run-volunteer-tests.py --gui
```
