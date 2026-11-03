"""Generates the FoodBridge class diagram as one connected SVG + PNG, coloured by design pattern.

First time only:  npm install --prefix docs/class-diagram
Then:             python docs/class-diagram/generate_class_diagram.py

Needs Node.js and Pillow. The PNG also needs Chrome or Edge (headless). Layout is done by ELK (elkjs).
"""
import html
import json
import os
import subprocess
import tempfile

from PIL import ImageFont

OUT_DIR = os.path.dirname(os.path.abspath(__file__))
FONT_DIR = r"C:\Windows\Fonts"
FONT = "Arial, Helvetica, sans-serif"

# role: (border colour, fill colour). Same scheme as the Lucidchart figure in the requirements document:
# pastel boxes with a coloured border per design pattern, white boxes with a black border for core classes.
CORE = ("#000000", "#FFFFFF")
COLORS = {
    "model": CORE,
    "lookup": CORE,
    "mvc": CORE,
    "singleton": ("#2E8B57", "#D4F1E4"),
    "factory": ("#7B5BB5", "#E6DAF8"),
    "strategy": ("#D9822B", "#FFE3CC"),
    "observer": ("#3A7BC8", "#D3E8FA"),
    "decorator": ("#C2527A", "#FBD9E3"),
}
INK = "#000000"
LINE = "#333333"
LEGEND = [("singleton", "Singleton"), ("factory", "Factory Method"), ("strategy", "Strategy"),
          ("observer", "Observer"), ("decorator", "Decorator"), ("model", "Core domain classes (no pattern)")]

# --- Classes, in family order. ELK keeps this left-to-right order where it can. ---------------------
CLASSES = []


def cls(name, role, stereotype=None, fields=(), methods=(), tags=(), abstract=False):
    CLASSES.append(dict(name=name, role=role, stereotype=stereotype, fields=list(fields), methods=list(methods),
                        tags=list(tags), abstract=abstract))


# MVC layers
cls("Views", "mvc", "View layer", fields=[
    "View (interface), ModuleView (abstract), HomeView, BackBar, DatePicker, SwingSupport",
    "LoginView, RegisterView, ProfileView, NotificationsView",
    "AddDonationView, PayDonationView, ReceiptView",
    "VolunteerPanelView, VolunteerProfileView, PickupTasksView,",
    "EventRegistrationView, HoursCertificateView",
    "BeneficiaryProfileView, DonationRequestView, ReceivedDistributionsView",
    "ManageUsersView, ManageBeneficiariesView, ManageEventsView, ImpactReportView, VolunteerAssignmentView",
    "AreaView, DonationStatusView, EventTypeView, FoodCategoryView, UnitView, LookupTableDialog"])
cls("Controllers", "mvc", "Controller layer", fields=[
    "AuthController, UserController, VolunteerController, DonationController,",
    "PickupController, BeneficiaryController, EventController,",
    "NotificationController, ReportController, LookupController"])

cls("Services", "mvc", "Service layer", fields=["All 7 service classes joined below"])

# Strategy: donations
cls("DonationService", "strategy", tags=[("strategy", "Strategy: Context"), ("factory", "Factory: Client")],
    fields=["- factories: Map<String, DonationFactory>", "- strategies: Map<String, DonationProcessingStrategy>",
            "- donationRepository: DonationRepository", "- donationStatusRepository: DonationStatusRepository",
            "- userRepository: UserRepository", "- pickupTaskRepository: PickupTaskRepository"],
    methods=["+ DonationService()", "+ DonationService(donationRepository: DonationRepository)",
             "+ registerStrategy(method: String, strategy: DonationProcessingStrategy): void",
             "+ createDonation(type: String, request: DonationDetails): int",
             "+ processMoneyDonation(id: int): DonationProcessingResult",
             "- registerDefaultFactories(): void", "- registerDefaultStrategies(): void"])
cls("DonationProcessingStrategy", "strategy", "interface", tags=[("strategy", "Strategy")],
    methods=["+ process(donation: MoneyDonation): DonationProcessingResult"])
cls("CashStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")],
    methods=["+ process(donation: MoneyDonation): DonationProcessingResult",
             "- invalid(donation: MoneyDonation): DonationProcessingResult"])
cls("CheckStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")],
    methods=["+ process(donation: MoneyDonation): DonationProcessingResult",
             "- invalid(donation: MoneyDonation): DonationProcessingResult"])
cls("OnlineStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")],
    fields=["- GATEWAY_LIMIT: double {static, final}"],
    methods=["+ process(donation: MoneyDonation): DonationProcessingResult", "- generateTransactionId(): String",
             "- invalid(donation: MoneyDonation): DonationProcessingResult"])
cls("DonationProcessingResult", "strategy", "datatype",
    fields=["- successful: boolean {final}", "- reference: String {final}", "- message: String {final}"],
    methods=["+ DonationProcessingResult(successful: boolean, reference: String, message: String)"])

# Factory Method: donations
cls("DonationFactory", "factory", "abstract", abstract=True, tags=[("factory", "Creator")],
    methods=["+ registerDonation(donor: Donor, details: DonationDetails): Donation",
             "+ createDonation(donor: Donor, details: DonationDetails): Donation {abstract}"])
cls("MoneyDonationFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["+ createDonation(donor: Donor, details: DonationDetails): Donation"])
cls("FoodDonationFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["+ createDonation(donor: Donor, details: DonationDetails): Donation"])
cls("DonationDetails", "factory", "helper", fields=[
    "- donor: Donor", "- area: Area", "- amount: BigDecimal", "- method: String", "- items: List<FoodItem>",
    "- expiryDate: LocalDate", "- pickupWindow: String"])
cls("Donation", "factory", "abstract", abstract=True, tags=[("factory", "Product")],
    fields=["# id: int", "# date: LocalDate"],
    methods=["# Donation(donor: Donor)", "+ updateStatus(status: DonationStatus): void",
             "+ cancel(cancelled: DonationStatus): boolean", "+ getDetails(): String",
             "+ getType(): String {abstract}", "# statusName(): String"])
cls("MoneyDonation", "factory", tags=[("factory", "ConcreteProduct")],
    fields=["+ METHODS: List<String> {static, final}", "- amount: double", "- method: String",
            "- transactionId: String", "- paid: boolean"],
    methods=["+ MoneyDonation(donor: Donor)", "+ markAsPaid(transactionId: String): void", "+ isPaid(): boolean",
             "+ cancel(cancelled: DonationStatus): boolean", "+ getType(): String", "+ getDetails(): String"])
cls("FoodDonation", "factory", tags=[("factory", "ConcreteProduct")],
    fields=["- expiryDate: LocalDate", "- pickupWindow: String"],
    methods=["+ FoodDonation(donor: Donor)", "+ addItem(item: FoodItem): void", "+ removeItem(itemId: int): void",
             "+ getItems(): List<FoodItem>", "+ getTotalQuantity(): double", "+ isExpired(): boolean",
             "+ getType(): String", "+ getDetails(): String"])

# Decorator: receipts
cls("ReceiptService", "decorator", tags=[("decorator", "Client")],
    fields=["- repository: ReceiptRepository"],
    methods=["+ ReceiptService()", "+ createReceipt(donationId: int): Receipt",
             "+ generateReceiptText(receiptId: int, options: ReceiptOptions): String"])
cls("ReceiptOptions", "decorator", "datatype",
    fields=["- thankYou: boolean {final}", "- taxInfo: boolean {final}"],
    methods=["+ ReceiptOptions(thankYou: boolean, taxInfo: boolean)"])
cls("ReceiptComponent", "decorator", "interface", tags=[("decorator", "Component")], methods=["+ generate(): String"])
cls("BasicReceipt", "decorator", tags=[("decorator", "ConcreteComponent")],
    fields=["- receipt: Receipt {final}"], methods=["+ BasicReceipt(receipt: Receipt)", "+ generate(): String"])
cls("ReceiptDecorator", "decorator", "abstract", abstract=True, tags=[("decorator", "Decorator")],
    fields=["# wrapped: ReceiptComponent {final}"],
    methods=["+ ReceiptDecorator(wrapped: ReceiptComponent)", "+ generate(): String"])
cls("ThankYouDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
    methods=["+ ThankYouDecorator(wrapped: ReceiptComponent)", "+ generate(): String"])
cls("TaxInfoDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
    methods=["+ TaxInfoDecorator(wrapped: ReceiptComponent)", "+ generate(): String"])

# Model: donation details
cls("DonationStatus", "lookup", "lookup", fields=[
    "+ POSTED, PAID, PICKED_UP, DELIVERED,", "    EXPIRED, CANCELLED: String {static, final}",
    "- id: int", "- name: String"],
    methods=["+ DonationStatus(name: String)", "+ DonationStatus(id: int, name: String)"])
cls("Receipt", "model", fields=["- id: int", "- receiptNumber: String", "- issueDate: LocalDate"],
    methods=["+ Receipt(donation: Donation, receiptNumber: String, issueDate: LocalDate)"])
cls("FoodItem", "model", fields=[
    "- id: int", "- name: String", "- quantity: double", "- allergens: String", "- expiryDate: LocalDate"],
    methods=["+ getInfo(): String", "+ isExpired(): boolean", "~ formatQuantity(value: double): String {static}"])
cls("FoodCategory", "lookup", "lookup", fields=["- id: int", "- name: String"],
    methods=["+ FoodCategory(name: String)", "+ FoodCategory(id: int, name: String)"])
cls("Unit", "lookup", "lookup", fields=["- id: int", "- name: String"],
    methods=["+ Unit(name: String)", "+ Unit(id: int, name: String)"])
cls("DonationRequest", "model", fields=["- id: int", "- quantity: BigDecimal", "- date: LocalDate", "- status: String"],
    methods=["+ DonationRequest(beneficiary: Beneficiary, category: FoodCategory,",
             "    quantity: BigDecimal, unit: Unit)", "+ updateStatus(value: String): void"])
cls("Distribution", "model", fields=["- id: int", "- date: LocalDate", "- quantity: BigDecimal"],
    methods=["+ Distribution(donation: FoodDonation, beneficiary: Beneficiary,",
             "    volunteer: Volunteer, date: LocalDate)",
             "+ Distribution(donation: FoodDonation, beneficiary: Beneficiary,",
             "    volunteer: Volunteer, date: LocalDate, quantity: BigDecimal)"])
cls("ImpactReport", "model", fields=[
    "- period: String", "- mealsDelivered: int", "- familiesServed: int", "- deliveryCount: int",
    "- deliveriesPerVolunteer: Map<Volunteer, Integer>"],
    methods=["+ ImpactReport(period: String, familiesServed: int, deliveryCount: int,",
             "    deliveries: Map<Volunteer, Integer>)", "+ isMealCountAvailable(): boolean"])

# Model: users
cls("User", "model", "abstract", abstract=True, fields=[
    "# id: int", "# name: String", "# email: String", "# passwordHash: String", "# phone: String",
    "# address: String", "# active: boolean", "# createdAt: LocalDateTime"],
    methods=["+ updateContactInfo(phone: String, address: String): void", "+ changeEmail(email: String): void",
             "+ setActive(active: boolean): void"])
cls("Area", "lookup", "lookup", fields=["- id: int", "- city: String", "- district: String"],
    methods=["+ Area(id: int, city: String, district: String)"])
cls("Admin", "model", fields=["- permissions: Set<String>"],
    methods=["+ hasPermission(p: String): boolean", "+ addPermission(p: String): void",
             "+ removePermission(p: String): void"])
cls("Donor", "model", fields=["- donorType: String", "- organizationName: String"])
cls("Beneficiary", "model", fields=["- householdSize: int", "- needs: String"],
    methods=["+ updateNeeds(needs: String): void"])

# Volunteer work
cls("PickupTaskService", "mvc", "Service layer",
    fields=["- pickupTaskRepository: PickupTaskRepository", "- volunteerRepository: VolunteerRepository",
            "- notificationRepository: NotificationRepository"],
    methods=[
    "+ listPending(): List<PickupTask>",
    "+ viewAvailableTasks(volunteerId: int, areaId: Integer): List<PickupTask>",
    "+ viewMyTasks(volunteerId: int): List<PickupTask>",
    "+ acceptTask(taskId: int, volunteerId: int): boolean", "+ declineTask(taskId: int, volunteerId: int): void",
    "+ markPickedUp(taskId: int, volunteerId: int): void", "+ markDelivered(taskId: int, volunteerId: int): void",
    "+ assignTask(taskId: int, volunteerId: int): void",
    "- isQualified(volunteer: Volunteer, task: PickupTask): boolean",
    "- requireQualified(volunteer: Volunteer, task: PickupTask): void",
    "- requireTask(taskId: int): PickupTask", "- requireOwnTask(taskId: int, volunteerId: int): PickupTask",
    "- requireVolunteer(volunteerId: int): Volunteer"])
cls("PickupTask", "model", fields=[
    "- MINIMUM_HOURS: double {static, final}", "- id: int", "- pickupTime: LocalDateTime",
    "- deliveryTime: LocalDateTime", "- status: PickupTaskStatus", "- hoursSpent: double",
    "- requiredSkill: String", "- itemsSummary: String"],
    methods=["+ assign(v: Volunteer): boolean", "+ markPickedUp(): void", "+ markDelivered(): Distribution",
             "+ calculateHours(): double"])
cls("PickupTaskStatus", "model", "enum", fields=["PENDING", "ASSIGNED", "PICKED_UP", "DELIVERED", "CANCELLED"])
cls("VolunteerService", "mvc", "Service layer", fields=["- volunteerRepository: VolunteerRepository"], methods=[
    "+ getVolunteer(id: int): Volunteer", "+ listVolunteers(): List<Volunteer>",
    "+ updateProfile(id: int, availability: String, hasTransport: boolean, areaId: Integer,",
    "    skills: List<String>, preferences: List<String>): void",
    "+ logHours(volunteerId: int, hours: double, note: String): void",
    "+ viewHours(volunteerId: int): List<VolunteerHours>",
    "- cleanValues(values: List<String>): List<String>"])
cls("Volunteer", "model", fields=[
    "- availability: String", "- hasTransport: boolean", "- totalHours: double", "- skills: List<String>",
    "- preferences: List<String>"],
    methods=["+ addHours(hours: double): void", "+ hasSkill(skill: String): boolean",
             "+ prefers(activity: String): boolean",
             "- containsIgnoreCase(values: List<String>, target: String): boolean {static}"])
cls("VolunteerHours", "model", fields=[
    "+ PICKUP, EVENT, MANUAL: String {static, final}", "- id: int", "- volunteerId: int", "- hours: double",
    "- source: String", "- note: String", "- loggedAt: LocalDateTime"])

# Strategy: matching volunteers
cls("VolunteerMatchingService", "strategy", tags=[("strategy", "Strategy: Context")],
    fields=["- PICKUP_ACTIVITY: String {static, final}", "- strategies: Map<String, VolunteerMatchingStrategy>",
            "- strategy: VolunteerMatchingStrategy", "- volunteerRepository: VolunteerRepository",
            "- pickupTaskRepository: PickupTaskRepository", "- eventRepository: EventRepository"],
    methods=["+ VolunteerMatchingService()", "+ strategyNames(): List<String>", "+ setStrategy(name: String): void",
             "+ rankForTask(taskId: int): List<Volunteer>", "+ rankForEvent(eventId: int): List<Volunteer>",
             "- rank(criteria: MatchCriteria): List<Volunteer>", "- partOfWeek(date: LocalDateTime): String"])
cls("VolunteerMatchingStrategy", "strategy", "interface", tags=[("strategy", "Strategy")],
    fields=["+ MATCH: int {static, final}", "+ NO_MATCH: int {static, final}"],
    methods=["+ score(volunteer: Volunteer, criteria: MatchCriteria): int"])
cls("SkillMatchStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")],
    methods=["+ score(volunteer: Volunteer, criteria: MatchCriteria): int"])
cls("AvailabilityMatchStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")],
    methods=["+ score(volunteer: Volunteer, criteria: MatchCriteria): int"])
cls("PreferenceMatchStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")],
    methods=["+ score(volunteer: Volunteer, criteria: MatchCriteria): int"])
cls("MatchCriteria", "strategy", "datatype", fields=[
    "- activity: String {final}", "- requiredSkill: String {final}", "- availabilityKeyword: String {final}",
    "- needsTransport: boolean {final}"],
    methods=["+ MatchCriteria(activity: String, requiredSkill: String,",
             "    availabilityKeyword: String, needsTransport: boolean)"])

# Factory Method: certificates
cls("CertificateService", "factory", tags=[("factory", "Factory: Client"), ("decorator", "Decorator: Client")],
    fields=["- PARTICIPATION_HOURS: double {static, final}", "- ACHIEVEMENT_HOURS: double {static, final}",
            "- volunteerRepository: VolunteerRepository", "- certificateRepository: CertificateRepository"],
    methods=["+ requestCertificate(volunteerId: int): String", "- factoryFor(hours: double): CertificateFactory",
             "- findOrIssue(factory: CertificateFactory, volunteer: Volunteer): Certificate",
             "- hoursAtIssue(certificate: Certificate): List<VolunteerHours>"])
cls("CertificateFactory", "factory", "abstract", abstract=True, tags=[("factory", "Creator")],
    methods=["+ issue(volunteer: Volunteer): Certificate", "# createCertificate(): Certificate {abstract}"])
cls("ParticipationCertificateFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["# createCertificate(): Certificate"])
cls("AchievementCertificateFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["# createCertificate(): Certificate"])
cls("ParticipationCertificate", "factory", tags=[("factory", "ConcreteProduct")],
    fields=["+ TYPE: String {static, final}"], methods=["+ getType(): String"])
cls("AchievementCertificate", "factory", tags=[("factory", "ConcreteProduct")],
    fields=["+ TYPE: String {static, final}"], methods=["+ getType(): String"])
cls("Certificate", "factory", "abstract", abstract=True, tags=[("factory", "Product")],
    fields=["- id: int", "- certificateNumber: String", "- hoursAtIssue: double", "- issuedAt: LocalDateTime"],
    methods=["+ getType(): String {abstract}"])

# Decorator: certificates
cls("CertificateComponent", "decorator", "interface", tags=[("decorator", "Component")], methods=["+ generate(): String"])
cls("BasicCertificate", "decorator", tags=[("decorator", "ConcreteComponent")],
    fields=["- DATE_FORMAT: DateTimeFormatter {static, final}", "- certificate: Certificate {final}"],
    methods=["+ BasicCertificate(certificate: Certificate)", "+ generate(): String", "- title(): String"])
cls("CertificateDecorator", "decorator", "abstract", abstract=True, tags=[("decorator", "Decorator")],
    fields=["# wrapped: CertificateComponent {final}"],
    methods=["# CertificateDecorator(wrapped: CertificateComponent)", "+ generate(): String"])
cls("HoursBreakdownDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
    fields=["- hoursEntries: List<VolunteerHours> {final}"],
    methods=["+ HoursBreakdownDecorator(wrapped: CertificateComponent,", "    hoursEntries: List<VolunteerHours>)",
             "+ generate(): String"])
cls("SignatureDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
    methods=["+ SignatureDecorator(wrapped: CertificateComponent)", "+ generate(): String"])

# Observer and events
cls("EventService", "observer", tags=[("observer", "Client")],
    fields=["- repository: EventRepository", "- registrationRepository: EventRegistrationRepository",
            "- volunteerRepository: VolunteerRepository", "- notificationRepository: NotificationRepository",
            "- publisher: EventPublisher"],
    methods=["+ create(value: Event): long", "+ findById(id: long): Event", "+ list(): List<Event>",
             "+ update(event: Event): boolean", "+ updateEvent(id: int, data: Event): void",
             "+ cancelEvent(eventId: long): boolean",
             "+ registerVolunteer(eventId: int, volunteerId: int): void",
             "+ listRegistrations(volunteerId: int): List<EventRegistration>",
             "+ listRegistrationsForEvent(eventId: int): List<EventRegistration>",
             "+ markAttendance(registrationId: int, status: String, hours: double): void",
             "+ announceChange(e: Event, msg: String): void",
             "- notifyRegistered(event: Event, message: String, registrations: List<EventRegistration>): void",
             "- requireDetails(event: Event): void", "- requireEvent(id: int): Event"])
cls("Subject", "observer", "interface", tags=[("observer", "Subject")],
    methods=["+ registerObserver(o: EventObserver): void", "+ removeObserver(o: EventObserver): void",
             "+ notifyObservers(): void"])
cls("EventPublisher", "observer", tags=[("observer", "ConcreteSubject")],
    fields=["- ALL_EVENTS: int {static, final}", "- observersByEvent: Map<Integer, List<EventObserver>>",
            "- currentEvent: Event", "- currentMessage: String"],
    methods=["+ registerObserver(o: EventObserver): void", "+ registerObserver(eventId: int, o: EventObserver): void",
             "+ removeObserver(o: EventObserver): void", "+ clearObservers(eventId: int): void",
             "+ notifyObservers(): void", "+ updateEvent(event: Event, message: String): void",
             "- notifyList(key: int): void"])
cls("EventObserver", "observer", "interface", tags=[("observer", "Observer")],
    methods=["+ update(event: Event, message: String): void"])
cls("VolunteerObserver", "observer", tags=[("observer", "ConcreteObserver")],
    fields=["- volunteerId: int {final}", "- notificationRepository: NotificationRepository {final}"],
    methods=["+ VolunteerObserver(volunteerId: int, notificationRepository: NotificationRepository)",
             "+ update(event: Event, message: String): void"])
cls("NotificationLogger", "observer", tags=[("observer", "ConcreteObserver")],
    fields=["- repository: NotificationRepository"],
    methods=["+ NotificationLogger(repository: NotificationRepository)",
             "+ update(event: Event, message: String): void"])
cls("Notification", "model", fields=[
    "- id: int", "- recipientId: int", "- message: String", "- read: boolean", "- createdAt: LocalDateTime"],
    methods=["+ Notification(recipientId: int, message: String)", "+ markAsRead(): void"])
cls("Event", "model", fields=[
    "- id: int", "- title: String", "- description: String", "- date: LocalDateTime", "- location: String",
    "- requiredSkill: String"], methods=["+ reschedule(date: LocalDateTime): void"])
cls("EventType", "lookup", "lookup", fields=["- id: int", "- name: String"],
    methods=["+ EventType(id: int, name: String)"])
cls("EventRegistration", "model", fields=[
    "+ REGISTERED, ATTENDED, ABSENT: String {static, final}", "- VALID_STATUSES: Set<String> {static, final}",
    "- id: int", "- registeredAt: LocalDateTime", "- attendanceStatus: String"],
    methods=["+ EventRegistration(event: Event, volunteer: Volunteer)", "+ updateAttendance(status: String): void"])

# Back end of the MVC chain
cls("Repositories", "mvc", "Repository layer", fields=[
    "Repository<T>: add, findById, findAll, update, delete",
    "UserRepository, VolunteerRepository, DonationRepository, ReceiptRepository,",
    "PickupTaskRepository, DistributionRepository, DonationRequestRepository,",
    "EventRepository, EventRegistrationRepository, NotificationRepository,",
    "CertificateRepository, and one per lookup table; JdbcSupport (shared helper)"])
cls("DatabaseManager", "singleton", tags=[("singleton", "Singleton")], fields=[
    "- uniqueInstance: DatabaseManager {static}", "- url: String", "- username: String", "- password: String"],
    methods=["- DatabaseManager()", "+ getInstance(): DatabaseManager {static}", "+ getConnection(): Connection"])

# --- Relationships ----------------------------------------------------------------------------------
# Each is drawn from `top` to `bottom`, so `top` sits above `bottom` wherever possible.
EDGES = []


def edge(top, bottom, kind, label="", top_mult="", bottom_mult=""):
    EDGES.append(dict(top=top, bottom=bottom, kind=kind, label=label, top_mult=top_mult, bottom_mult=bottom_mult))


def extends(child, parent): edge(parent, child, "extends")
def implements(child, parent): edge(parent, child, "implements")
def assoc(a, b, label="", a_mult="", b_mult=""): edge(a, b, "assoc", label, a_mult, b_mult)
def link(a, b, label="", a_mult="", b_mult=""): edge(a, b, "link", label, a_mult, b_mult)
def uses(a, b, label="use"): edge(a, b, "uses", f"«{label}»")
def aggregate(holder, part, label="", part_mult="", holder_mult=""): edge(holder, part, "aggregate", label, holder_mult, part_mult)
def wraps(decorator, component): edge(component, decorator, "wraps", "wraps", "1", "")
def compose(holder, part, label="", holder_mult="", part_mult=""): edge(holder, part, "compose", label, holder_mult, part_mult)


# MVC chain
# "Services" stands for the whole service layer, so 14 separate lines become one chain plus a fan.
uses("Views", "Controllers", "calls")
uses("Controllers", "Services", "calls")
uses("Services", "Repositories", "calls")
for service in ["DonationService", "ReceiptService", "PickupTaskService", "VolunteerService", "VolunteerMatchingService",
                "CertificateService", "EventService"]:
    edge("Services", service, "member")
uses("Repositories", "DatabaseManager", "getInstance")
edge("DatabaseManager", "DatabaseManager", "assoc", "uniqueInstance", "", "1")

# Strategy
aggregate("DonationService", "DonationProcessingStrategy", "strategies", "1..*", "1")
for name in ["CashStrategy", "CheckStrategy", "OnlineStrategy"]:
    implements(name, "DonationProcessingStrategy")
uses("DonationProcessingStrategy", "DonationProcessingResult", "returns")
uses("DonationProcessingStrategy", "MoneyDonation", "processes")
aggregate("VolunteerMatchingService", "VolunteerMatchingStrategy", "strategies", "1..*", "1")
for name in ["SkillMatchStrategy", "AvailabilityMatchStrategy", "PreferenceMatchStrategy"]:
    implements(name, "VolunteerMatchingStrategy")
uses("VolunteerMatchingService", "MatchCriteria", "create")
uses("VolunteerMatchingStrategy", "MatchCriteria")
uses("VolunteerMatchingStrategy", "Volunteer")

# Factory Method
aggregate("DonationService", "DonationFactory", "factories", "1..*", "1")
extends("MoneyDonationFactory", "DonationFactory")
extends("FoodDonationFactory", "DonationFactory")
uses("DonationFactory", "DonationDetails")
uses("DonationFactory", "Donation", "create")
uses("MoneyDonationFactory", "MoneyDonation", "create")
uses("FoodDonationFactory", "FoodDonation", "create")
extends("MoneyDonation", "Donation")
extends("FoodDonation", "Donation")
uses("CertificateService", "CertificateFactory")
extends("ParticipationCertificateFactory", "CertificateFactory")
extends("AchievementCertificateFactory", "CertificateFactory")
uses("CertificateFactory", "Certificate", "create")
uses("ParticipationCertificateFactory", "ParticipationCertificate", "create")
uses("AchievementCertificateFactory", "AchievementCertificate", "create")
extends("ParticipationCertificate", "Certificate")
extends("AchievementCertificate", "Certificate")

# Decorator
uses("ReceiptService", "ReceiptComponent")
uses("ReceiptService", "ReceiptOptions")
uses("ReceiptService", "Receipt", "create")
implements("BasicReceipt", "ReceiptComponent")
implements("ReceiptDecorator", "ReceiptComponent")
wraps("ReceiptDecorator", "ReceiptComponent")
extends("ThankYouDecorator", "ReceiptDecorator")
extends("TaxInfoDecorator", "ReceiptDecorator")
assoc("BasicReceipt", "Receipt", "", "", "1")
uses("CertificateService", "CertificateComponent")
implements("BasicCertificate", "CertificateComponent")
implements("CertificateDecorator", "CertificateComponent")
wraps("CertificateDecorator", "CertificateComponent")
extends("HoursBreakdownDecorator", "CertificateDecorator")
extends("SignatureDecorator", "CertificateDecorator")
assoc("BasicCertificate", "Certificate", "", "", "1")
assoc("HoursBreakdownDecorator", "VolunteerHours", "", "", "0..*")

# Observer
assoc("EventService", "EventPublisher", "", "", "1")
uses("EventService", "VolunteerObserver", "create")
uses("EventService", "Event")
uses("EventService", "EventRegistration", "create")
implements("EventPublisher", "Subject")
aggregate("EventPublisher", "EventObserver", "notifies", "0..*")
assoc("EventPublisher", "Event", "currentEvent", "", "0..1")
implements("VolunteerObserver", "EventObserver")
implements("NotificationLogger", "EventObserver")
uses("VolunteerObserver", "Notification", "create")

# Domain. Multiplicities follow the foreign keys in database/schema.sql: a nullable column is 0..1.
for name in ["Donor", "Volunteer", "Beneficiary", "Admin"]:
    extends(name, "User")
assoc("User", "Area", "lives in", "0..*", "0..1")
link("User", "Notification", "receives", "1", "0..*")
link("Volunteer", "VolunteerHours", "logs", "1", "0..*")
link("Volunteer", "EventRegistration", "has", "1", "0..*")
link("Event", "EventRegistration", "has", "1", "0..*")
assoc("Event", "EventType", "of type", "0..*", "1")
link("Volunteer", "PickupTask", "accepts", "0..1", "0..*")
link("Beneficiary", "PickupTask", "receives", "0..1", "0..*")
assoc("PickupTask", "PickupTaskStatus")
assoc("PickupTask", "Area", "in area", "0..*", "0..1")
link("FoodDonation", "PickupTask", "picked up in", "1", "0..1")
uses("PickupTask", "Distribution", "create")
link("Volunteer", "Certificate", "earns", "1", "0..*")
link("Donor", "Donation", "makes", "1", "0..*")
assoc("Donation", "Area", "located in", "0..*", "0..1")
assoc("Donation", "DonationStatus", "has status", "0..*", "1")
link("Donation", "Receipt", "has", "1", "0..1")
compose("FoodDonation", "FoodItem", "contains", "1", "1..*")
assoc("FoodItem", "FoodCategory", "belongs to", "0..*", "1")
assoc("FoodItem", "Unit", "measured in", "0..*", "1")
assoc("DonationRequest", "FoodCategory", "asks for", "0..*", "1")
assoc("DonationRequest", "Unit", "in unit", "0..*", "1")
link("Beneficiary", "DonationRequest", "submits", "1", "0..*")
link("FoodDonation", "Distribution", "records", "1", "0..*")
link("Beneficiary", "Distribution", "gets", "1", "0..*")
link("Volunteer", "Distribution", "delivers", "0..1", "0..*")
uses("ImpactReport", "Distribution")

# Services to the classes they work on
uses("VolunteerService", "Volunteer")
uses("PickupTaskService", "PickupTask")
uses("PickupTaskService", "Notification", "create")

# --- Sizes ------------------------------------------------------------------------------------------
_fonts = {}


def text_width(text, size, bold=False, italic=False):
    name = {(False, False): "arial.ttf", (True, False): "arialbd.ttf", (True, True): "arialbi.ttf",
            (False, True): "ariali.ttf"}[(bold, italic)]
    key = (name, size)
    if key not in _fonts:
        _fonts[key] = ImageFont.truetype(os.path.join(FONT_DIR, name), size)
    return _fonts[key].getlength(text)


PAD = 9
LINE_H = 16
BODY = 12
NAME = 14
STEREO = 10.5
TAG = 10
TAG_H = 18


def measure(c):
    widths = [text_width(c["name"], NAME, True, c["abstract"]) + 2 * PAD]
    if c["stereotype"]:
        widths.append(text_width(f"«{c['stereotype']}»", STEREO) + 2 * PAD)
    widths += [text_width(line, BODY) + 2 * PAD for line in c["fields"] + c["methods"]]
    if c["tags"]:
        widths.append(sum(text_width(t, TAG) + 14 for _, t in c["tags"]))
    header = 26 + (14 if c["stereotype"] else 0)
    tag_h = TAG_H if c["tags"] else 0
    body = sum(len(section) * LINE_H + 10 for section in (c["fields"], c["methods"]) if section)
    c["w"], c["h"], c["header"], c["tag_h"] = round(max(widths) + 2), header + tag_h + body, header, tag_h


# --- Layout -----------------------------------------------------------------------------------------
def layout():
    for c in CLASSES:
        measure(c)
    children = [dict(id=c["name"], width=c["w"], height=c["h"]) for c in CLASSES]
    elk_edges = []
    for i, e in enumerate(EDGES):
        if e["top"] == e["bottom"]:
            continue  # the self-loop is drawn by hand
        labels = []
        if e["label"]:
            labels.append(dict(id=f"l{i}", text=e["label"], width=round(text_width(e["label"], 11)) + 6, height=14))
        elk_edges.append(dict(id=f"e{i}", sources=[e["top"]], targets=[e["bottom"]], labels=labels))
    graph = dict(id="root", children=children, edges=elk_edges, layoutOptions={
        "elk.algorithm": "layered",
        "elk.direction": "DOWN",
        "elk.edgeRouting": "ORTHOGONAL",
        "elk.spacing.nodeNode": "36",
        "elk.spacing.edgeNode": "18",
        "elk.spacing.edgeEdge": "11",
        "elk.spacing.edgeLabel": "3",
        "elk.layered.spacing.nodeNodeBetweenLayers": "42",
        "elk.layered.spacing.edgeNodeBetweenLayers": "20",
        "elk.layered.spacing.edgeEdgeBetweenLayers": "11",
        "elk.layered.nodePlacement.strategy": "NETWORK_SIMPLEX",
        "elk.layered.crossingMinimization.thoroughness": "60",
        "elk.layered.considerModelOrder.strategy": "PREFER_NODES",
        "elk.layered.mergeEdges": "false",  # separate exit points keep multiplicities from overlapping
        "elk.padding": "[top=10,left=10,bottom=10,right=10]",
    })
    done = subprocess.run(["node", os.path.join(OUT_DIR, "layout.js")], input=json.dumps(graph), capture_output=True,
                          text=True, encoding="utf-8", cwd=OUT_DIR, check=False)
    if done.returncode != 0:
        raise SystemExit("ELK layout failed:\n" + done.stderr)
    return json.loads(done.stdout)


# --- SVG drawing ------------------------------------------------------------------------------------
def esc(text):
    return html.escape(text, quote=False)


def draw_class(c, x, y):
    strong, light = COLORS[c["role"]]
    w, h = c["w"], c["h"]
    out = [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{light}" stroke="{strong}" stroke-width="1.5" rx="3"/>',
           f'<line x1="{x}" y1="{y + c["header"]}" x2="{x + w}" y2="{y + c["header"]}" stroke="{strong}" stroke-width="0.8"/>']
    cx, ty = x + w / 2, y + 17
    if c["stereotype"]:
        out.append(f'<text x="{cx}" y="{y + 13}" text-anchor="middle" font-size="{STEREO}" fill="{INK}">«{esc(c["stereotype"])}»</text>')
        ty = y + 29
    style = ' font-style="italic"' if c["abstract"] else ""
    out.append(f'<text x="{cx}" y="{ty}" text-anchor="middle" font-size="{NAME}" font-weight="bold"{style} fill="{INK}">{esc(c["name"])}</text>')
    cy = y + c["header"]
    if c["tags"]:
        tw = w / len(c["tags"])
        for i, (role, label) in enumerate(c["tags"]):
            out.append(f'<rect x="{x + i * tw}" y="{cy}" width="{tw}" height="{TAG_H}" fill="{COLORS[role][0]}" opacity="0.22"/>'
                       f'<text x="{x + i * tw + tw / 2}" y="{cy + 13}" text-anchor="middle" font-size="{TAG}" fill="{INK}">{esc(label)}</text>')
        cy += TAG_H
    first = True
    for section in (c["fields"], c["methods"]):
        if not section:
            continue
        if not first:
            out.append(f'<line x1="{x}" y1="{cy}" x2="{x + w}" y2="{cy}" stroke="{strong}" stroke-width="0.8"/>')
        first = False
        cy += 5
        for line in section:
            indent = 16 if line.startswith("    ") else 0  # a wrapped signature continues indented
            out.append(f'<text x="{x + PAD + indent}" y="{cy + 12}" font-size="{BODY}" fill="{INK}">{esc(line.strip())}</text>')
            cy += LINE_H
        cy += 5
    return "".join(out)


def direction(a, b):
    dx, dy = b[0] - a[0], b[1] - a[1]
    length = max((dx * dx + dy * dy) ** 0.5, 0.001)
    return dx / length, dy / length


def _pts(points):
    return " ".join(f"{p[0]:.1f},{p[1]:.1f}" for p in points)


def marker(kind, tip, ux, uy, color):
    """(ux, uy) points from the line towards the tip."""
    px, py = -uy, ux
    x, y = tip
    if kind == "triangle":
        base = (x - ux * 13, y - uy * 13)
        pts = [tip, (base[0] + px * 7, base[1] + py * 7), (base[0] - px * 7, base[1] - py * 7)]
        return f'<polygon points="{_pts(pts)}" fill="#FFFFFF" stroke="{color}" stroke-width="1.4"/>'
    if kind == "arrow":
        base = (x - ux * 10, y - uy * 10)
        pts = [(base[0] + px * 5, base[1] + py * 5), tip, (base[0] - px * 5, base[1] - py * 5)]
        return f'<polyline points="{_pts(pts)}" fill="none" stroke="{color}" stroke-width="1.4"/>'
    mid = (x - ux * 8, y - uy * 8)
    back = (x - ux * 16, y - uy * 16)
    pts = [tip, (mid[0] + px * 5, mid[1] + py * 5), back, (mid[0] - px * 5, mid[1] - py * 5)]
    fill = "#FFFFFF" if kind == "open_diamond" else color
    return f'<polygon points="{_pts(pts)}" fill="{fill}" stroke="{color}" stroke-width="1.4"/>'


def halo_text(x, y, text, size=11, anchor="start", color=INK, halo=3):
    return (f'<text x="{x:.1f}" y="{y:.1f}" text-anchor="{anchor}" font-size="{size}" fill="{color}" stroke="#FFFFFF" '
            f'stroke-width="{halo}" paint-order="stroke" stroke-linejoin="round">{esc(text)}</text>')


def mult_text(end, inner, text):
    ux, uy = direction(end, inner)
    x, y = end[0] + ux * 21, end[1] + uy * 21
    if abs(uy) > abs(ux):  # vertical line: sit to the right of it
        return halo_text(x + 5, y + 4, text, 10.5, halo=6)
    return halo_text(x, y - 5, text, 10.5, "middle", halo=6)


def draw_edge(e, points, color):
    kind = e["kind"]
    dash = ' stroke-dasharray="6 4"' if kind in ("uses", "implements") else ""
    if kind == "member":
        dash = ' stroke-dasharray="2 3"'
    d = "M" + " L".join(f"{p[0]:.1f} {p[1]:.1f}" for p in points)
    out = [f'<path d="{d}" fill="none" stroke="{color}" stroke-width="1.25"{dash}/>']
    ux, uy = direction(points[-2], points[-1])
    sx, sy = direction(points[1], points[0])
    if kind in ("extends", "implements"):
        out.append(marker("triangle", points[0], sx, sy, color))
    if kind in ("assoc", "uses", "aggregate"):
        out.append(marker("arrow", points[-1], ux, uy, color))
    if kind == "aggregate":
        out.append(marker("open_diamond", points[0], sx, sy, color))
    if kind == "wraps":
        out.append(marker("arrow", points[0], sx, sy, color))
        out.append(marker("open_diamond", points[-1], ux, uy, color))
    if kind == "compose":
        out.append(marker("diamond", points[0], sx, sy, color))
    return "".join(out)


def draw_multiplicities(e, points):
    """Kept apart from draw_edge so they go in the top layer, where no later line can cross them."""
    out = []
    if e["top_mult"]:
        out.append(mult_text(points[0], points[1], e["top_mult"]))
    if e["bottom_mult"]:
        out.append(mult_text(points[-1], points[-2], e["bottom_mult"]))
    return "".join(out)


def legend_svg(x, y):
    parts = [f'<text x="{x}" y="{y + 30}" font-size="32" font-weight="bold" fill="{INK}">FoodBridge Class Diagram</text>',
             f'<text x="{x}" y="{y + 54}" font-size="15" fill="#475569">Each class is coloured by the design pattern it belongs to; white classes are the core domain. Getters, setters, toString() and no-arg constructors of data classes are omitted; references between domain classes are drawn as lines.</text>']
    cx, cy = x, y + 74
    for role, label in LEGEND:
        parts.append(f'<rect x="{cx}" y="{cy}" width="22" height="22" rx="4" fill="{COLORS[role][1]}" stroke="{COLORS[role][0]}" stroke-width="1.5"/>'
                     f'<text x="{cx + 29}" y="{cy + 16}" font-size="15" fill="{INK}">{esc(label)}</text>')
        cx += 50 + text_width(label, 15)
    lx, ly = x, cy + 46
    for label, dash, kind in [("Extends", False, "triangle"), ("Implements", True, "triangle"), ("Association", False, "arrow"),
                              ("Uses / creates", True, "arrow"), ("Aggregation", False, "open_diamond"),
                              ("Composition", False, "diamond"), ("Part of service layer", None, "none")]:
        d = ' stroke-dasharray="2 3"' if dash is None else ' stroke-dasharray="6 4"' if dash else ""
        parts.append(f'<line x1="{lx}" y1="{ly}" x2="{lx + 58}" y2="{ly}" stroke="{LINE}" stroke-width="1.4"{d}/>')
        if kind in ("triangle", "arrow"):
            parts.append(marker(kind, (lx + 60, ly), 1, 0, LINE))
        elif kind != "none":
            parts.append(marker(kind, (lx, ly), -1, 0, LINE))
        parts.append(f'<text x="{lx + 70}" y="{ly + 5}" font-size="15" fill="{INK}">{label}</text>')
        lx += 100 + text_width(label, 15)
    return "".join(parts), ly + 26 - y


def build():
    result = layout()
    by_name = {c["name"]: c for c in CLASSES}
    placed = {n["id"]: n for n in result["children"]}
    margin = 40
    legend, legend_h = legend_svg(margin, margin)
    ox, oy = margin, margin + legend_h + 10
    width = max(result["width"], 1200) + 2 * margin
    height = result["height"] + oy + margin

    edge_svg, label_svg = [], []
    for item in result["edges"]:
        e = EDGES[int(item["id"][1:])]
        a, b = by_name[e["top"]], by_name[e["bottom"]]
        same = a["role"] == b["role"] and a["role"] not in ("model", "lookup", "mvc")
        color = COLORS[a["role"]][0] if same else LINE
        for section in item.get("sections", []):
            pts = [section["startPoint"], *section.get("bendPoints", []), section["endPoint"]]
            points = [(p["x"] + ox, p["y"] + oy) for p in pts]
            edge_svg.append(draw_edge(e, points, color))
            label_svg.append(draw_multiplicities(e, points))
        for label in item.get("labels", []):
            label_svg.append(halo_text(label["x"] + ox + 3, label["y"] + oy + 11, label["text"]))

    # DatabaseManager's self-loop
    db = placed["DatabaseManager"]
    bx, by = db["x"] + ox + db["width"], db["y"] + oy + 30
    loop = [(bx, by), (bx + 26, by), (bx + 26, by + 34), (bx, by + 34)]
    loop_d = " L".join(f"{p[0]} {p[1]}" for p in loop)
    edge_svg.append(f'<path d="M{loop_d}" fill="none" stroke="{COLORS["singleton"][0]}" stroke-width="1.25"/>')
    edge_svg.append(marker("arrow", loop[-1], -1, 0, COLORS["singleton"][0]))
    label_svg.append(halo_text(bx + 31, by + 21, "uniqueInstance 1", 11))

    boxes = [draw_class(c, placed[c["name"]]["x"] + ox, placed[c["name"]]["y"] + oy) for c in CLASSES]
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{width:.0f}" height="{height:.0f}" viewBox="0 0 {width:.0f} {height:.0f}" '
           f'font-family="{FONT}"><rect width="100%" height="100%" fill="#FFFFFF"/>{legend}'
           f'{"".join(edge_svg)}{"".join(boxes)}{"".join(label_svg)}</svg>')
    path = os.path.join(OUT_DIR, "foodbridge-class-diagram.svg")
    with open(path, "w", encoding="utf-8") as f:
        f.write(svg)
    print(f"page: {width:.0f} x {height:.0f}, {len(CLASSES)} classes, {len(EDGES)} relationships")
    return path, int(width), int(height)


def to_png(svg_path, width, height):
    browsers = [r"C:\Program Files\Google\Chrome\Application\chrome.exe",
                r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"]
    browser = next((b for b in browsers if os.path.exists(b)), None)
    if browser is None:
        print("No Chrome or Edge found, so the PNG was not updated.")
        return
    png = os.path.join(OUT_DIR, "foodbridge-class-diagram.png")
    with tempfile.TemporaryDirectory() as profile:
        subprocess.run([browser, "--headless=new", "--disable-gpu", "--hide-scrollbars", f"--user-data-dir={profile}",
                        f"--window-size={width},{height}", "--default-background-color=FFFFFFFF",
                        "--force-device-scale-factor=2", f"--screenshot={png}", "file:///" + svg_path.replace("\\", "/")],
                       check=True, capture_output=True, timeout=180)


if __name__ == "__main__":
    svg_file, w, h = build()
    to_png(svg_file, w, h)
