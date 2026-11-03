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

# role: (strong colour, light colour)
COLORS = {
    "model": ("#475569", "#F1F5F9"),
    "lookup": ("#94A3B8", "#F8FAFC"),
    "strategy": ("#1D4ED8", "#EFF6FF"),
    "factory": ("#C2410C", "#FFF7ED"),
    "decorator": ("#7E22CE", "#FAF5FF"),
    "observer": ("#15803D", "#F0FDF4"),
    "singleton": ("#BE123C", "#FFF1F2"),
    "mvc": ("#0E7490", "#ECFEFF"),
}
INK = "#1E293B"
LINE = "#64748B"
LEGEND = [("model", "Model (MVC): domain class"), ("lookup", "Lookup table"), ("strategy", "Strategy"),
          ("factory", "Factory Method"), ("decorator", "Decorator"), ("observer", "Observer"),
          ("singleton", "Singleton"), ("mvc", "MVC layers")]

# --- Classes, in family order. ELK keeps this left-to-right order where it can. ---------------------
CLASSES = []


def cls(name, role, stereotype=None, fields=(), methods=(), tags=(), abstract=False):
    CLASSES.append(dict(name=name, role=role, stereotype=stereotype, fields=list(fields), methods=list(methods),
                        tags=list(tags), abstract=abstract))


# MVC layers
cls("Views", "mvc", "View layer", fields=[
    "LoginView, RegisterView, HomeView, ProfileView, NotificationsView",
    "AddDonationView, PayDonationView, ReceiptView",
    "VolunteerPanelView, VolunteerProfileView, PickupTasksView,",
    "EventRegistrationView, HoursCertificateView",
    "DonationRequestView, ReceivedDistributionsView",
    "ManageUsersView, ManageEventsView, ImpactReportView, VolunteerAssignmentView",
    "AreaView, DonationStatusView, EventTypeView, FoodCategoryView, UnitView"])
cls("Controllers", "mvc", "Controller layer", fields=[
    "AuthController, UserController, VolunteerController, DonationController,",
    "PickupController, BeneficiaryController, EventController,",
    "NotificationController, ReportController, LookupController"])

cls("Services", "mvc", "Service layer", fields=["All 7 service classes joined below"])

# Strategy: donations
cls("DonationService", "strategy", tags=[("strategy", "Strategy: Context"), ("factory", "Factory: Client")],
    fields=["- factories: Map<String, DonationFactory>", "- strategies: Map<String, DonationProcessingStrategy>"],
    methods=["+ createDonation(type, request): int", "+ processMoneyDonation(id): Result"])
cls("DonationProcessingStrategy", "strategy", "interface", tags=[("strategy", "Strategy")],
    methods=["+ process(donation): DonationProcessingResult"])
cls("CashStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")], methods=["+ process(donation): Result"])
cls("CheckStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")], methods=["+ process(donation): Result"])
cls("OnlineStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")], methods=["+ process(donation): Result"])
cls("DonationProcessingResult", "strategy", "helper",
    fields=["- successful: boolean", "- reference: String", "- message: String"])

# Factory Method: donations
cls("DonationFactory", "factory", "abstract", abstract=True, tags=[("factory", "Creator")],
    methods=["+ registerDonation(donor, details): Donation", "+ createDonation(donor, details): Donation {abstract}"])
cls("MoneyDonationFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["+ createDonation(donor, details): Donation"])
cls("FoodDonationFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["+ createDonation(donor, details): Donation"])
cls("DonationDetails", "factory", "helper", fields=[
    "- amount: BigDecimal", "- method: String", "- items: List<FoodItem>", "- expiryDate: Date", "- pickupWindow: String"])
cls("Donation", "factory", "abstract", abstract=True, tags=[("factory", "Product")],
    fields=["- id: int", "- date: Date"], methods=["+ updateStatus(status): void", "+ getType(): String {abstract}"])
cls("MoneyDonation", "factory", tags=[("factory", "ConcreteProduct")],
    fields=["- amount: BigDecimal", "- method: String", "- transactionId: String", "- paid: boolean"],
    methods=["+ markAsPaid(): void", "+ getType(): String"])
cls("FoodDonation", "factory", tags=[("factory", "ConcreteProduct")],
    fields=["- expiryDate: Date", "- pickupWindow: String"],
    methods=["+ addItem(item): void", "+ removeItem(item): void", "+ isExpired(): boolean", "+ getType(): String"])

# Decorator: receipts
cls("ReceiptService", "decorator", tags=[("decorator", "Client")],
    methods=["+ createReceipt(donationId): Receipt", "+ generateReceiptText(id, options): String"])
cls("ReceiptOptions", "decorator", "helper", fields=["- thankYou: boolean", "- taxInfo: boolean"])
cls("ReceiptComponent", "decorator", "interface", tags=[("decorator", "Component")], methods=["+ generate(): String"])
cls("BasicReceipt", "decorator", tags=[("decorator", "ConcreteComponent")],
    fields=["- receipt: Receipt"], methods=["+ generate(): String"])
cls("ReceiptDecorator", "decorator", "abstract", abstract=True, tags=[("decorator", "Decorator")],
    fields=["# wrapped: ReceiptComponent"], methods=["+ generate(): String"])
cls("ThankYouDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")], methods=["+ generate(): String"])
cls("TaxInfoDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")], methods=["+ generate(): String"])

# Model: donation details
cls("DonationStatus", "lookup", "lookup", fields=["- id: int", "- name: String"])
cls("Receipt", "model", fields=["- id: int", "- receiptNumber: String", "- issueDate: Date"])
cls("FoodItem", "model", fields=[
    "- id: int", "- name: String", "- quantity: BigDecimal", "- allergens: String", "- expiryDate: Date"],
    methods=["+ isExpired(): boolean", "+ getInfo(): String"])
cls("FoodCategory", "lookup", "lookup", fields=["- id: int", "- name: String"])
cls("Unit", "lookup", "lookup", fields=["- id: int", "- name: String"])
cls("DonationRequest", "model", fields=["- id: int", "- quantity: BigDecimal", "- date: Date", "- status: String"],
    methods=["+ updateStatus(status): void"])
cls("Distribution", "model", fields=["- id: int", "- date: Date", "- quantity: BigDecimal"])
cls("ImpactReport", "model", fields=[
    "- period: String", "- mealsDelivered: int", "- familiesServed: int",
    "- deliveriesPerVolunteer: Map<Volunteer, Integer>"])

# Model: users
cls("User", "model", "abstract", abstract=True, fields=[
    "- id: int", "- name: String", "- email: String", "- passwordHash: String", "- phone: String",
    "- address: String", "- active: boolean", "- createdAt: DateTime"],
    methods=["+ updateContactInfo(phone, address): void", "+ changeEmail(email): void", "+ setActive(active): void"])
cls("Area", "lookup", "lookup", fields=["- id: int", "- city: String", "- district: String"])
cls("Admin", "model", fields=["- permissions: Set<String>"],
    methods=["+ hasPermission(p): boolean", "+ addPermission(p): void", "+ removePermission(p): void"])
cls("Donor", "model", fields=["- donorType: String", "- organizationName: String"])
cls("Beneficiary", "model", fields=["- householdSize: int", "- needs: String"], methods=["+ updateNeeds(needs): void"])

# Volunteer work
cls("PickupTaskService", "mvc", "Service layer", methods=[
    "+ viewAvailableTasks(volunteerId): List<PickupTask>", "+ viewMyTasks(volunteerId): List<PickupTask>",
    "+ acceptTask(taskId, volunteerId): boolean", "+ declineTask(taskId, volunteerId): void",
    "+ markPickedUp(taskId, volunteerId): void", "+ markDelivered(taskId, volunteerId): void",
    "+ assignTask(taskId, volunteerId): void"])
cls("PickupTask", "model", fields=[
    "- id: int", "- pickupTime: DateTime", "- deliveryTime: DateTime", "- status: PickupTaskStatus",
    "- hoursSpent: double", "- requiredSkill: String"],
    methods=["+ assign(volunteer): boolean", "+ markPickedUp(): void", "+ markDelivered(): Distribution",
             "+ calculateHours(): double"])
cls("PickupTaskStatus", "model", "enum", fields=["PENDING", "ASSIGNED", "PICKED_UP", "DELIVERED", "CANCELLED"])
cls("VolunteerService", "mvc", "Service layer", methods=[
    "+ updateProfile(...): void", "+ logHours(volunteerId, hours, note): void",
    "+ viewHours(volunteerId): List<VolunteerHours>"])
cls("Volunteer", "model", fields=[
    "- availability: String", "- hasTransport: boolean", "- totalHours: double", "- skills: List<String>",
    "- preferences: List<String>"],
    methods=["+ addHours(hours): void", "+ hasSkill(skill): boolean", "+ prefers(activity): boolean"])
cls("VolunteerHours", "model", fields=["- hours: double", "- source: String", "- loggedAt: DateTime"])

# Strategy: matching volunteers
cls("VolunteerMatchingService", "strategy", tags=[("strategy", "Strategy: Context")],
    fields=["- strategy: VolunteerMatchingStrategy"],
    methods=["+ setStrategy(name): void", "+ rankForTask(taskId): List<Volunteer>",
             "+ rankForEvent(eventId): List<Volunteer>"])
cls("VolunteerMatchingStrategy", "strategy", "interface", tags=[("strategy", "Strategy")],
    methods=["+ score(volunteer, criteria): int"])
cls("SkillMatchStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")], methods=["+ score(volunteer, criteria): int"])
cls("AvailabilityMatchStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")], methods=["+ score(volunteer, criteria): int"])
cls("PreferenceMatchStrategy", "strategy", tags=[("strategy", "ConcreteStrategy")], methods=["+ score(volunteer, criteria): int"])
cls("MatchCriteria", "strategy", "helper", fields=[
    "- activity: String", "- requiredSkill: String", "- availabilityKeyword: String", "- needsTransport: boolean"])

# Factory Method: certificates
cls("CertificateService", "factory", tags=[("factory", "Factory: Client"), ("decorator", "Decorator: Client")],
    methods=["+ requestCertificate(volunteerId): String"])
cls("CertificateFactory", "factory", "abstract", abstract=True, tags=[("factory", "Creator")],
    methods=["+ issue(volunteer): Certificate", "# createCertificate(): Certificate {abstract}"])
cls("ParticipationCertificateFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["# createCertificate(): Certificate"])
cls("AchievementCertificateFactory", "factory", tags=[("factory", "ConcreteCreator")],
    methods=["# createCertificate(): Certificate"])
cls("ParticipationCertificate", "factory", tags=[("factory", "ConcreteProduct")], methods=["+ getType(): String"])
cls("AchievementCertificate", "factory", tags=[("factory", "ConcreteProduct")], methods=["+ getType(): String"])
cls("Certificate", "factory", "abstract", abstract=True, tags=[("factory", "Product")],
    fields=["- certificateNumber: String", "- hoursAtIssue: double", "- issuedAt: DateTime"],
    methods=["+ getType(): String {abstract}"])

# Decorator: certificates
cls("CertificateComponent", "decorator", "interface", tags=[("decorator", "Component")], methods=["+ generate(): String"])
cls("BasicCertificate", "decorator", tags=[("decorator", "ConcreteComponent")],
    fields=["- certificate: Certificate"], methods=["+ generate(): String"])
cls("CertificateDecorator", "decorator", "abstract", abstract=True, tags=[("decorator", "Decorator")],
    fields=["# wrapped: CertificateComponent"], methods=["+ generate(): String"])
cls("HoursBreakdownDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
    fields=["- hoursEntries: List<VolunteerHours>"], methods=["+ generate(): String"])
cls("SignatureDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")], methods=["+ generate(): String"])

# Observer and events
cls("EventService", "observer", tags=[("observer", "Client")], fields=["- publisher: EventPublisher"],
    methods=["+ registerVolunteer(eventId, volunteerId): void", "+ markAttendance(registrationId, status, hours): void",
             "+ announceChange(event, message): void"])
cls("Subject", "observer", "interface", tags=[("observer", "Subject")],
    methods=["+ registerObserver(o): void", "+ removeObserver(o): void", "+ notifyObservers(): void"])
cls("EventPublisher", "observer", tags=[("observer", "ConcreteSubject")],
    fields=["- observersByEvent: Map<Integer, List<EventObserver>>", "- currentEvent: Event", "- currentMessage: String"],
    methods=["+ registerObserver(eventId, o): void", "+ clearObservers(eventId): void", "+ updateEvent(event, message): void"])
cls("EventObserver", "observer", "interface", tags=[("observer", "Observer")], methods=["+ update(event, message): void"])
cls("VolunteerObserver", "observer", tags=[("observer", "ConcreteObserver")],
    fields=["- volunteerId: int"], methods=["+ update(event, message): void"])
cls("NotificationLogger", "observer", tags=[("observer", "ConcreteObserver")], methods=["+ update(event, message): void"])
cls("Notification", "model", fields=[
    "- id: int", "- recipientId: int", "- message: String", "- read: boolean", "- createdAt: DateTime"],
    methods=["+ markAsRead(): void"])
cls("Event", "model", fields=[
    "- id: int", "- title: String", "- description: String", "- date: DateTime", "- location: String",
    "- requiredSkill: String"], methods=["+ reschedule(date): void"])
cls("EventType", "lookup", "lookup", fields=["- id: int", "- name: String"])
cls("EventRegistration", "model", fields=["- id: int", "- registeredAt: DateTime", "- attendanceStatus: String"],
    methods=["+ updateAttendance(status): void"])

# Back end of the MVC chain
cls("Repositories", "mvc", "Repository layer", fields=[
    "Repository<T>: add, findById, findAll, update, delete",
    "UserRepository, VolunteerRepository, DonationRepository, ReceiptRepository,",
    "PickupTaskRepository, DistributionRepository, DonationRequestRepository,",
    "EventRepository, EventRegistrationRepository, NotificationRepository,",
    "CertificateRepository, and one per lookup table"])
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
def aggregate(holder, part, label="", part_mult=""): edge(holder, part, "aggregate", label, "", part_mult)
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
aggregate("DonationService", "DonationProcessingStrategy", "strategies", "1..*")
for name in ["CashStrategy", "CheckStrategy", "OnlineStrategy"]:
    implements(name, "DonationProcessingStrategy")
uses("DonationProcessingStrategy", "DonationProcessingResult", "create")
aggregate("VolunteerMatchingService", "VolunteerMatchingStrategy", "strategy", "1")
for name in ["SkillMatchStrategy", "AvailabilityMatchStrategy", "PreferenceMatchStrategy"]:
    implements(name, "VolunteerMatchingStrategy")
uses("VolunteerMatchingStrategy", "MatchCriteria")
uses("VolunteerMatchingStrategy", "Volunteer")

# Factory Method
uses("DonationService", "DonationFactory")
extends("MoneyDonationFactory", "DonationFactory")
extends("FoodDonationFactory", "DonationFactory")
uses("DonationFactory", "DonationDetails")
uses("MoneyDonationFactory", "MoneyDonation", "create")
uses("FoodDonationFactory", "FoodDonation", "create")
extends("MoneyDonation", "Donation")
extends("FoodDonation", "Donation")
uses("CertificateService", "CertificateFactory")
extends("ParticipationCertificateFactory", "CertificateFactory")
extends("AchievementCertificateFactory", "CertificateFactory")
uses("ParticipationCertificateFactory", "ParticipationCertificate", "create")
uses("AchievementCertificateFactory", "AchievementCertificate", "create")
extends("ParticipationCertificate", "Certificate")
extends("AchievementCertificate", "Certificate")

# Decorator
uses("ReceiptService", "ReceiptComponent")
uses("ReceiptService", "ReceiptOptions")
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
implements("EventPublisher", "Subject")
aggregate("EventPublisher", "EventObserver", "notifies", "0..*")
assoc("EventPublisher", "Event", "currentEvent", "", "0..1")
implements("VolunteerObserver", "EventObserver")
implements("NotificationLogger", "EventObserver")
uses("VolunteerObserver", "Notification", "create")
uses("NotificationLogger", "Notification", "create")

# Domain
for name in ["Donor", "Volunteer", "Beneficiary", "Admin"]:
    extends(name, "User")
assoc("User", "Area", "lives in", "0..*", "1")
link("User", "Notification", "receives", "1", "0..*")
link("Volunteer", "VolunteerHours", "logs", "1", "0..*")
link("Volunteer", "EventRegistration", "has", "1", "0..*")
link("Event", "EventRegistration", "has", "1", "0..*")
assoc("Event", "EventType", "of type", "0..*", "1")
link("Volunteer", "PickupTask", "accepts", "1", "0..*")
link("Beneficiary", "PickupTask", "receives", "1", "0..*")
assoc("PickupTask", "PickupTaskStatus")
link("FoodDonation", "PickupTask", "picked up in", "1", "0..1")
uses("PickupTask", "Distribution", "create")
link("Volunteer", "Certificate", "earns", "1", "0..*")
link("Donor", "Donation", "makes", "1", "0..*")
assoc("Donation", "Area", "located in", "0..*", "1")
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
link("Volunteer", "Distribution", "delivers", "1", "0..*")
uses("ImpactReport", "Distribution")

# Services to the classes they work on
uses("VolunteerService", "Volunteer")
uses("PickupTaskService", "PickupTask")

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
           f'<rect x="{x}" y="{y}" width="{w}" height="{c["header"]}" fill="{strong}" rx="3"/>']
    cx, ty = x + w / 2, y + 17
    if c["stereotype"]:
        out.append(f'<text x="{cx}" y="{y + 13}" text-anchor="middle" font-size="{STEREO}" fill="#FFFFFF">«{esc(c["stereotype"])}»</text>')
        ty = y + 29
    style = ' font-style="italic"' if c["abstract"] else ""
    out.append(f'<text x="{cx}" y="{ty}" text-anchor="middle" font-size="{NAME}" font-weight="bold"{style} fill="#FFFFFF">{esc(c["name"])}</text>')
    cy = y + c["header"]
    if c["tags"]:
        tw = w / len(c["tags"])
        for i, (role, label) in enumerate(c["tags"]):
            out.append(f'<rect x="{x + i * tw}" y="{cy}" width="{tw}" height="{TAG_H}" fill="{COLORS[role][0]}" opacity="0.82"/>'
                       f'<text x="{x + i * tw + tw / 2}" y="{cy + 13}" text-anchor="middle" font-size="{TAG}" fill="#FFFFFF">{esc(label)}</text>')
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
            out.append(f'<text x="{x + PAD}" y="{cy + 12}" font-size="{BODY}" fill="{INK}">{esc(line)}</text>')
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


def halo_text(x, y, text, size=11, anchor="start", color=INK):
    return (f'<text x="{x:.1f}" y="{y:.1f}" text-anchor="{anchor}" font-size="{size}" fill="{color}" stroke="#FFFFFF" '
            f'stroke-width="3" paint-order="stroke" stroke-linejoin="round">{esc(text)}</text>')


def mult_text(end, inner, text):
    ux, uy = direction(end, inner)
    x, y = end[0] + ux * 21, end[1] + uy * 21
    if abs(uy) > abs(ux):  # vertical line: sit to the right of it
        return halo_text(x + 5, y + 4, text, 10.5)
    return halo_text(x, y - 5, text, 10.5, "middle")


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
    if e["top_mult"]:
        out.append(mult_text(points[0], points[1], e["top_mult"]))
    if e["bottom_mult"]:
        out.append(mult_text(points[-1], points[-2], e["bottom_mult"]))
    return "".join(out)


def legend_svg(x, y):
    parts = [f'<text x="{x}" y="{y + 30}" font-size="32" font-weight="bold" fill="{INK}">FoodBridge Class Diagram</text>',
             f'<text x="{x}" y="{y + 54}" font-size="15" fill="#475569">Each class is coloured by the design pattern it belongs to. Getters and setters are omitted.</text>']
    cx, cy = x, y + 74
    for role, label in LEGEND:
        parts.append(f'<rect x="{cx}" y="{cy}" width="22" height="22" rx="4" fill="{COLORS[role][0]}"/>'
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
        same = a["role"] == b["role"] and a["role"] not in ("model", "lookup")
        color = COLORS[a["role"]][0] if same else LINE
        for section in item.get("sections", []):
            pts = [section["startPoint"], *section.get("bendPoints", []), section["endPoint"]]
            points = [(p["x"] + ox, p["y"] + oy) for p in pts]
            edge_svg.append(draw_edge(e, points, color))
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
