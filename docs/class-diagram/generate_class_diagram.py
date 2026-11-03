"""Generates the FoodBridge class diagram (SVG + PNG) with Graphviz.

Run from anywhere:  python docs/class-diagram/generate_class_diagram.py
Needs: Graphviz `dot` on PATH. PNG output also needs Chrome or Edge (headless).

Each pattern is laid out as its own panel. A dashed "reference" box stands in for a class whose full
box lives in another panel, so no relationship line ever has to cross between panels.
"""
import html
import os
import re
import subprocess
import tempfile

OUT_DIR = os.path.dirname(os.path.abspath(__file__))
FONT = "Arial"

# role: (strong header colour, light body colour)
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
LINE = "#475569"


def esc(text):
    return html.escape(text, quote=False)


def box(name, role, stereotype=None, fields=(), methods=(), tags=None, abstract=False):
    """A full class box. `tags` is a list of (colour role, text) shown in the header."""
    strong, light = COLORS[role]
    tags = tags or []
    title = f'<B>{esc(name)}</B>'
    if abstract:
        title = f'<B><I>{esc(name)}</I></B>'
    rows = []
    width = max(len(tags), 1)
    stereo = (f'<FONT POINT-SIZE="10" COLOR="#FFFFFF">«{esc(stereotype)}»</FONT><BR/>'
              if stereotype else "")
    rows.append(f'<TR><TD COLSPAN="{width}" BGCOLOR="{strong}" CELLPADDING="6">{stereo}'
                f'<FONT POINT-SIZE="13" COLOR="#FFFFFF">{title}</FONT></TD></TR>')
    if tags:
        cells = "".join(
            f'<TD BGCOLOR="{COLORS[r][0]}" CELLPADDING="3"><FONT POINT-SIZE="9" COLOR="#FFFFFF">'
            f'{esc(t)}</FONT></TD>' for r, t in tags)
        rows.append(f"<TR>{cells}</TR>")
    for section in (fields, methods):
        if section:
            lines = "".join(f'{esc(s)}<BR ALIGN="LEFT"/>' for s in section)
            rows.append(f'<TR><TD COLSPAN="{width}" BGCOLOR="{light}" ALIGN="LEFT" BALIGN="LEFT" '
                        f'CELLPADDING="6"><FONT POINT-SIZE="10.5" COLOR="{INK}">{lines}</FONT></TD></TR>')
    return (f'<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" COLOR="{strong}">'
            + "".join(rows) + "</TABLE>>")


def ref(name, role, where):
    """A small dashed stand-in for a class drawn in full in another panel."""
    strong = "#64748B" if role == "lookup" else COLORS[role][0]  # darker grey stays readable on white
    return (f'<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" STYLE="DASHED" COLOR="{strong}" BGCOLOR="#FFFFFF">'
            f'<TR><TD CELLPADDING="5"><FONT POINT-SIZE="11" COLOR="{strong}"><B>{esc(name)}</B></FONT><BR/>'
            f'<FONT POINT-SIZE="9" COLOR="#64748B">see {esc(where)}</FONT></TD></TR></TABLE>>')


# Edge helpers -------------------------------------------------------------------------------------
def extends(child, parent):
    # Drawn parent -> child with the arrow reversed, so parents sit above their children.
    return (parent, child, 'dir=back arrowtail=empty arrowsize=1.2')


def extends_up(child, parent):
    # Same arrow, but the parent sits below (used where the child must line up with its creator).
    return (child, parent, 'arrowhead=empty arrowsize=1.2')


def implements(child, parent):
    return (parent, child, 'dir=back arrowtail=empty arrowsize=1.2 style=dashed')


def assoc(a, b, label="", tail="", head=""):
    return (a, b, f'arrowhead=vee label="{label}" taillabel="{tail}" headlabel="{head}"')


def link(a, b, label="", tail="", head=""):
    return (a, b, f'arrowhead=none label="{label}" taillabel="{tail}" headlabel="{head}"')


def aggregate(holder, part, label="", head="", constraint="true"):
    return (holder, part, f'dir=both arrowtail=odiamond arrowhead=vee label="{label}" headlabel="{head}" '
                          f'constraint={constraint}')


def compose(holder, part, label="", tail="", head=""):
    return (holder, part, f'dir=both arrowtail=diamond arrowhead=none label="{label}" '
                          f'taillabel="{tail}" headlabel="{head}"')


def uses(a, b, label="use"):
    return (a, b, f'arrowhead=vee style=dashed label="«{label}»"')


def hidden(a, b):
    return (a, b, 'style=invis')


def order(*names):
    """Keeps a row of same-rank boxes in this left-to-right order."""
    return [hidden(a, b) for a, b in zip(names, names[1:])]


# Panels -------------------------------------------------------------------------------------------
PANELS = {}


def panel(key, title, subtitle, role, nodes, edges, rankdir="TB", ranks=(), nodesep=0.35, ranksep=0.65):
    PANELS[key] = dict(title=title, subtitle=subtitle, role=role, nodes=nodes, edges=edges,
                       rankdir=rankdir, ranks=ranks, nodesep=nodesep, ranksep=ranksep)


# --- Users and volunteer work ---------------------------------------------------------------------
panel("users", "Model: users and volunteer work", "Domain classes used by the volunteer module", "model", {
    "User": box("User", "model", "abstract", abstract=True, fields=[
        "- id: int", "- name: String", "- email: String", "- passwordHash: String", "- phone: String",
        "- address: String", "- area: Area", "- active: boolean", "- createdAt: DateTime"],
        methods=["+ updateContactInfo(phone, address): void", "+ changeEmail(email): void",
                 "+ setActive(active): void"]),
    "Donor": box("Donor", "model", fields=["- donorType: String", "- organizationName: String"]),
    "Volunteer": box("Volunteer", "model", fields=[
        "- availability: String", "- hasTransport: boolean", "- totalHours: double",
        "- skills: List<String>", "- preferences: List<String>"],
        methods=["+ addHours(hours): void", "+ hasSkill(skill): boolean", "+ prefers(activity): boolean"]),
    "Beneficiary": box("Beneficiary", "model", fields=["- householdSize: int", "- needs: String"],
                       methods=["+ updateNeeds(needs): void"]),
    "Admin": box("Admin", "model", fields=["- permissions: Set<String>"],
                 methods=["+ hasPermission(p): boolean", "+ addPermission(p): void",
                          "+ removePermission(p): void"]),
    "Area": box("Area", "lookup", "lookup", fields=["- id: int", "- city: String", "- district: String"]),
    "Notification": box("Notification", "model", fields=[
        "- id: int", "- recipientId: int", "- message: String", "- read: boolean", "- createdAt: DateTime"],
        methods=["+ markAsRead(): void"]),
    "VolunteerHours": box("VolunteerHours", "model", fields=[
        "- hours: double", "- source: String", "- loggedAt: DateTime"]),
    "EventRegistration": box("EventRegistration", "model", fields=[
        "- id: int", "- registeredAt: DateTime", "- attendanceStatus: String"],
        methods=["+ updateAttendance(status): void"]),
    "Event": box("Event", "model", fields=[
        "- id: int", "- title: String", "- description: String", "- date: DateTime", "- location: String",
        "- requiredSkill: String"], methods=["+ reschedule(date): void"]),
    "EventType": box("EventType", "lookup", "lookup", fields=["- id: int", "- name: String"]),
    "PickupTask": box("PickupTask", "model", fields=[
        "- id: int", "- pickupTime: DateTime", "- deliveryTime: DateTime", "- status: PickupTaskStatus",
        "- hoursSpent: double", "- requiredSkill: String"],
        methods=["+ assign(volunteer): boolean", "+ markPickedUp(): void", "+ markDelivered(): Distribution",
                 "+ calculateHours(): double"]),
    "PickupTaskStatus": box("PickupTaskStatus", "model", "enum", fields=[
        "PENDING", "ASSIGNED", "PICKED_UP", "DELIVERED", "CANCELLED"]),
    "FoodDonationRef": ref("FoodDonation", "factory", "Model: donations"),
    "DistributionRef": ref("Distribution", "model", "Model: donations"),
}, [
    extends("Donor", "User"), extends("Volunteer", "User"), extends("Beneficiary", "User"),
    extends("Admin", "User"),
    assoc("User", "Area", "lives in", "0..*", "1"),
    link("User", "Notification", "receives", "1", "0..*"),
    link("Volunteer", "VolunteerHours", "logs", "1", "0..*"),
    link("Volunteer", "EventRegistration", "has", "1", "0..*"),
    link("Event", "EventRegistration", "has", "1", "0..*"),
    assoc("Event", "EventType", "of type", "0..*", "1"),
    link("Volunteer", "PickupTask", "accepts", "1", "0..*"),
    link("Beneficiary", "PickupTask", "receives", "1", "0..*"),
    assoc("PickupTask", "PickupTaskStatus"),
    link("FoodDonationRef", "PickupTask", "picked up in", "1", "0..1"),
    uses("PickupTask", "DistributionRef", "create"),
    *order("Area", "Notification", "Admin", "Donor", "Beneficiary", "FoodDonationRef", "Volunteer", "Event"),
    *order("PickupTask", "VolunteerHours", "EventRegistration", "EventType"),
], ranks=[["Area", "Notification", "Admin", "Donor", "Beneficiary", "FoodDonationRef", "Volunteer", "Event"],
          ["PickupTask", "VolunteerHours", "EventRegistration", "EventType"]])

# --- Donations and beneficiaries ------------------------------------------------------------------
panel("donations", "Model: donations and distribution", "Orange classes are the Factory Method products",
      "model", {
    "DonorRef": ref("Donor", "model", "Model: users"),
    "BeneficiaryRef": ref("Beneficiary", "model", "Model: users"),
    "BeneficiaryRef2": ref("Beneficiary", "model", "Model: users"),
    "VolunteerRef": ref("Volunteer", "model", "Model: users"),
    "AreaRef": ref("Area", "lookup", "Model: users"),
    "Donation": box("Donation", "factory", "abstract", abstract=True, tags=[("factory", "Factory Method: Product")],
                    fields=["- id: int", "- date: Date"],
                    methods=["+ updateStatus(status): void", "+ getType(): String {abstract}"]),
    "MoneyDonation": box("MoneyDonation", "factory", tags=[("factory", "Factory Method: ConcreteProduct")],
                         fields=["- amount: BigDecimal", "- method: String", "- transactionId: String",
                                 "- paid: boolean"],
                         methods=["+ markAsPaid(): void", "+ getType(): String"]),
    "FoodDonation": box("FoodDonation", "factory", tags=[("factory", "Factory Method: ConcreteProduct")],
                        fields=["- expiryDate: Date", "- pickupWindow: String"],
                        methods=["+ addItem(item): void", "+ removeItem(item): void", "+ isExpired(): boolean",
                                 "+ getType(): String"]),
    "DonationStatus": box("DonationStatus", "lookup", "lookup", fields=["- id: int", "- name: String"]),
    "Receipt": box("Receipt", "model", fields=["- id: int", "- receiptNumber: String", "- issueDate: Date"]),
    "FoodItem": box("FoodItem", "model", fields=[
        "- id: int", "- name: String", "- quantity: BigDecimal", "- allergens: String", "- expiryDate: Date"],
        methods=["+ isExpired(): boolean", "+ getInfo(): String"]),
    "FoodCategory": box("FoodCategory", "lookup", "lookup", fields=["- id: int", "- name: String"]),
    "Unit": box("Unit", "lookup", "lookup", fields=["- id: int", "- name: String"]),
    "FoodCategoryRef": ref("FoodCategory", "lookup", "this panel"),
    "UnitRef": ref("Unit", "lookup", "this panel"),
    "DonationRequest": box("DonationRequest", "model", fields=[
        "- id: int", "- quantity: BigDecimal", "- date: Date", "- status: String"],
        methods=["+ updateStatus(status): void"]),
    "Distribution": box("Distribution", "model", fields=["- id: int", "- date: Date", "- quantity: BigDecimal"]),
    "ImpactReport": box("ImpactReport", "model", fields=[
        "- period: String", "- mealsDelivered: int", "- familiesServed: int",
        "- deliveriesPerVolunteer: Map<Volunteer, Integer>"]),
}, [
    link("DonorRef", "Donation", "makes", "1", "0..*"),
    assoc("Donation", "AreaRef", "located in", "0..*", "1"),
    assoc("Donation", "DonationStatus", "has status", "0..*", "1"),
    link("Donation", "Receipt", "has", "1", "0..1"),
    extends("MoneyDonation", "Donation"), extends("FoodDonation", "Donation"),
    compose("FoodDonation", "FoodItem", "contains", "1", "1..*"),
    assoc("FoodItem", "FoodCategory", "belongs to", "0..*", "1"),
    assoc("FoodItem", "Unit", "measured in", "0..*", "1"),
    assoc("DonationRequest", "FoodCategoryRef", "asks for", "0..*", "1"),
    assoc("DonationRequest", "UnitRef", "in unit", "0..*", "1"),
    link("BeneficiaryRef", "DonationRequest", "submits", "1", "0..*"),
    link("FoodDonation", "Distribution", "records", "1", "0..*"),
    link("BeneficiaryRef2", "Distribution", "gets", "1", "0..*"),
    link("VolunteerRef", "Distribution", "delivers", "1", "0..*"),
    uses("ImpactReport", "Distribution"),
    *order("AreaRef", "DonationStatus", "Receipt", "BeneficiaryRef", "MoneyDonation", "FoodDonation",
           "BeneficiaryRef2", "VolunteerRef", "ImpactReport"),
    *order("DonationRequest", "FoodItem", "Distribution"),
    *order("FoodCategoryRef", "UnitRef", "FoodCategory", "Unit"),
], ranks=[["AreaRef", "DonationStatus", "Receipt", "BeneficiaryRef", "MoneyDonation", "FoodDonation",
           "BeneficiaryRef2", "VolunteerRef", "ImpactReport"],
          ["DonationRequest", "FoodItem", "Distribution"],
          ["FoodCategoryRef", "UnitRef", "FoodCategory", "Unit"]])

# --- Strategy -------------------------------------------------------------------------------------
panel("strategy", "Strategy", "Swap the algorithm at runtime: pay a donation, rank volunteers", "strategy", {
    "DonationService": box("DonationService", "strategy", tags=[("strategy", "Strategy: Context"),
                                                                ("factory", "Factory: Client")],
                           fields=["- factories: Map<String, DonationFactory>",
                                   "- strategies: Map<String, DonationProcessingStrategy>"],
                           methods=["+ createDonation(type, request): int", "+ processMoneyDonation(id): Result"]),
    "DonationProcessingStrategy": box("DonationProcessingStrategy", "strategy", "interface",
                                      methods=["+ process(donation): DonationProcessingResult"]),
    "CashStrategy": box("CashStrategy", "strategy", methods=["+ process(donation): Result"]),
    "CheckStrategy": box("CheckStrategy", "strategy", methods=["+ process(donation): Result"]),
    "OnlineStrategy": box("OnlineStrategy", "strategy", methods=["+ process(donation): Result"]),
    "DonationProcessingResult": box("DonationProcessingResult", "strategy", "helper", fields=[
        "- successful: boolean", "- reference: String", "- message: String"]),
    "VolunteerMatchingService": box("VolunteerMatchingService", "strategy", tags=[("strategy", "Strategy: Context")],
                                    fields=["- strategy: VolunteerMatchingStrategy"],
                                    methods=["+ setStrategy(name): void", "+ rankForTask(taskId): List<Volunteer>",
                                             "+ rankForEvent(eventId): List<Volunteer>"]),
    "VolunteerMatchingStrategy": box("VolunteerMatchingStrategy", "strategy", "interface",
                                     methods=["+ score(volunteer, criteria): int"]),
    "SkillMatchStrategy": box("SkillMatchStrategy", "strategy", methods=["+ score(volunteer, criteria): int"]),
    "AvailabilityMatchStrategy": box("AvailabilityMatchStrategy", "strategy",
                                     methods=["+ score(volunteer, criteria): int"]),
    "PreferenceMatchStrategy": box("PreferenceMatchStrategy", "strategy",
                                   methods=["+ score(volunteer, criteria): int"]),
    "MatchCriteria": box("MatchCriteria", "strategy", "helper", fields=[
        "- activity: String", "- requiredSkill: String", "- availabilityKeyword: String",
        "- needsTransport: boolean"]),
}, [
    aggregate("DonationService", "DonationProcessingStrategy", "strategies", "1..*"),
    implements("CashStrategy", "DonationProcessingStrategy"),
    implements("CheckStrategy", "DonationProcessingStrategy"),
    implements("OnlineStrategy", "DonationProcessingStrategy"),
    uses("CheckStrategy", "DonationProcessingResult", "create"),
    aggregate("VolunteerMatchingService", "VolunteerMatchingStrategy", "strategy", "1"),
    implements("SkillMatchStrategy", "VolunteerMatchingStrategy"),
    implements("AvailabilityMatchStrategy", "VolunteerMatchingStrategy"),
    implements("PreferenceMatchStrategy", "VolunteerMatchingStrategy"),
    uses("AvailabilityMatchStrategy", "MatchCriteria"),
])

# --- Factory Method -------------------------------------------------------------------------------
panel("factory", "Factory Method", "Subclasses decide which donation or certificate to create", "factory", {
    "DonationServiceRef": ref("DonationService", "strategy", "Strategy"),
    "DonationFactory": box("DonationFactory", "factory", "abstract", abstract=True,
                           tags=[("factory", "Creator")],
                           methods=["+ registerDonation(donor, details): Donation",
                                    "+ createDonation(donor, details): Donation {abstract}"]),
    "MoneyDonationFactory": box("MoneyDonationFactory", "factory", tags=[("factory", "ConcreteCreator")],
                                methods=["+ createDonation(donor, details): Donation"]),
    "FoodDonationFactory": box("FoodDonationFactory", "factory", tags=[("factory", "ConcreteCreator")],
                               methods=["+ createDonation(donor, details): Donation"]),
    "MoneyRef": ref("MoneyDonation", "factory", "Model: donations"),
    "FoodRef": ref("FoodDonation", "factory", "Model: donations"),
    "DonationDetails": box("DonationDetails", "factory", "helper", fields=[
        "- amount: BigDecimal", "- method: String", "- items: List<FoodItem>", "- expiryDate: Date",
        "- pickupWindow: String"]),
    "CertificateService": box("CertificateService", "factory", tags=[("factory", "Factory: Client"),
                                                                     ("decorator", "Decorator: Client")],
                              methods=["+ requestCertificate(volunteerId): String"]),
    "CertificateFactory": box("CertificateFactory", "factory", "abstract", abstract=True,
                              tags=[("factory", "Creator")],
                              methods=["+ issue(volunteer): Certificate",
                                       "# createCertificate(): Certificate {abstract}"]),
    "ParticipationCertificateFactory": box("ParticipationCertificateFactory", "factory",
                                           tags=[("factory", "ConcreteCreator")],
                                           methods=["# createCertificate(): Certificate"]),
    "AchievementCertificateFactory": box("AchievementCertificateFactory", "factory",
                                         tags=[("factory", "ConcreteCreator")],
                                         methods=["# createCertificate(): Certificate"]),
    "Certificate": box("Certificate", "factory", "abstract", abstract=True, tags=[("factory", "Product")],
                       fields=["- certificateNumber: String", "- hoursAtIssue: double", "- issuedAt: DateTime"],
                       methods=["+ getType(): String {abstract}"]),
    "ParticipationCertificate": box("ParticipationCertificate", "factory", tags=[("factory", "ConcreteProduct")],
                                    methods=["+ getType(): String"]),
    "AchievementCertificate": box("AchievementCertificate", "factory", tags=[("factory", "ConcreteProduct")],
                                  methods=["+ getType(): String"]),
    "VolunteerRef": ref("Volunteer", "model", "Model: users"),
}, [
    uses("DonationServiceRef", "DonationFactory"),
    extends("MoneyDonationFactory", "DonationFactory"), extends("FoodDonationFactory", "DonationFactory"),
    uses("DonationFactory", "DonationDetails"),
    uses("MoneyDonationFactory", "MoneyRef", "create"), uses("FoodDonationFactory", "FoodRef", "create"),
    uses("CertificateService", "CertificateFactory"),
    extends("ParticipationCertificateFactory", "CertificateFactory"),
    extends("AchievementCertificateFactory", "CertificateFactory"),
    uses("ParticipationCertificateFactory", "ParticipationCertificate", "create"),
    uses("AchievementCertificateFactory", "AchievementCertificate", "create"),
    extends_up("ParticipationCertificate", "Certificate"), extends_up("AchievementCertificate", "Certificate"),
    link("VolunteerRef", "Certificate", "earns", "1", "0..*"),
]) 

# --- Decorator ------------------------------------------------------------------------------------
panel("decorator", "Decorator", "Wrap a document in extra layers without changing it: receipts, certificates",
      "decorator", {
    "ReceiptService": box("ReceiptService", "decorator", tags=[("decorator", "Client")],
                          methods=["+ createReceipt(donationId): Receipt",
                                   "+ generateReceiptText(id, options): String"]),
    "ReceiptOptions": box("ReceiptOptions", "decorator", "helper", fields=["- thankYou: boolean", "- taxInfo: boolean"]),
    "ReceiptComponent": box("ReceiptComponent", "decorator", "interface", tags=[("decorator", "Component")],
                            methods=["+ generate(): String"]),
    "BasicReceipt": box("BasicReceipt", "decorator", tags=[("decorator", "ConcreteComponent")],
                        fields=["- receipt: Receipt"], methods=["+ generate(): String"]),
    "ReceiptDecorator": box("ReceiptDecorator", "decorator", "abstract", abstract=True,
                            tags=[("decorator", "Decorator")], fields=["# wrapped: ReceiptComponent"],
                            methods=["+ generate(): String"]),
    "ThankYouDecorator": box("ThankYouDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
                             methods=["+ generate(): String"]),
    "TaxInfoDecorator": box("TaxInfoDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
                            methods=["+ generate(): String"]),
    "ReceiptRef": ref("Receipt", "model", "Model: donations"),
    "CertificateServiceRef": ref("CertificateService", "factory", "Factory Method"),
    "CertificateComponent": box("CertificateComponent", "decorator", "interface", tags=[("decorator", "Component")],
                                methods=["+ generate(): String"]),
    "BasicCertificate": box("BasicCertificate", "decorator", tags=[("decorator", "ConcreteComponent")],
                            fields=["- certificate: Certificate"], methods=["+ generate(): String"]),
    "CertificateDecorator": box("CertificateDecorator", "decorator", "abstract", abstract=True,
                                tags=[("decorator", "Decorator")], fields=["# wrapped: CertificateComponent"],
                                methods=["+ generate(): String"]),
    "HoursBreakdownDecorator": box("HoursBreakdownDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
                                   fields=["- hoursEntries: List<VolunteerHours>"], methods=["+ generate(): String"]),
    "SignatureDecorator": box("SignatureDecorator", "decorator", tags=[("decorator", "ConcreteDecorator")],
                              methods=["+ generate(): String"]),
    "CertificateRef": ref("Certificate", "factory", "Factory Method"),
    "HoursRef": ref("VolunteerHours", "model", "Model: users"),
}, [
    uses("ReceiptService", "ReceiptComponent"), uses("ReceiptService", "ReceiptOptions"),
    implements("BasicReceipt", "ReceiptComponent"), implements("ReceiptDecorator", "ReceiptComponent"),
    aggregate("ReceiptDecorator", "ReceiptComponent", "wraps", "1", "false"),
    extends("ThankYouDecorator", "ReceiptDecorator"), extends("TaxInfoDecorator", "ReceiptDecorator"),
    assoc("BasicReceipt", "ReceiptRef", "", "", "1"),
    uses("CertificateServiceRef", "CertificateComponent"),
    implements("BasicCertificate", "CertificateComponent"), implements("CertificateDecorator", "CertificateComponent"),
    aggregate("CertificateDecorator", "CertificateComponent", "wraps", "1", "false"),
    extends("HoursBreakdownDecorator", "CertificateDecorator"), extends("SignatureDecorator", "CertificateDecorator"),
    assoc("BasicCertificate", "CertificateRef", "", "", "1"),
    assoc("HoursBreakdownDecorator", "HoursRef", "", "", "0..*"),
])

# --- Observer -------------------------------------------------------------------------------------
panel("observer", "Observer", "When an event changes, every registered volunteer is notified", "observer", {
    "EventService": box("EventService", "observer", tags=[("observer", "Client")],
                        fields=["- publisher: EventPublisher"],
                        methods=["+ registerVolunteer(eventId, volunteerId): void",
                                 "+ markAttendance(registrationId, status, hours): void",
                                 "+ announceChange(event, message): void"]),
    "Subject": box("Subject", "observer", "interface", tags=[("observer", "Subject")],
                   methods=["+ registerObserver(o): void", "+ removeObserver(o): void", "+ notifyObservers(): void"]),
    "EventPublisher": box("EventPublisher", "observer", tags=[("observer", "ConcreteSubject")],
                          fields=["- observersByEvent: Map<Integer, List<EventObserver>>",
                                  "- currentEvent: Event", "- currentMessage: String"],
                          methods=["+ registerObserver(eventId, o): void", "+ clearObservers(eventId): void",
                                   "+ updateEvent(event, message): void"]),
    "EventObserver": box("EventObserver", "observer", "interface", tags=[("observer", "Observer")],
                         methods=["+ update(event, message): void"]),
    "VolunteerObserver": box("VolunteerObserver", "observer", tags=[("observer", "ConcreteObserver")],
                             fields=["- volunteerId: int"], methods=["+ update(event, message): void"]),
    "NotificationLogger": box("NotificationLogger", "observer", tags=[("observer", "ConcreteObserver")],
                              methods=["+ update(event, message): void"]),
    "NotificationRef": ref("Notification", "model", "Model: users"),
    "EventRef": ref("Event", "model", "Model: users"),
}, [
    assoc("EventService", "EventPublisher", "", "", "1"),
    uses("EventService", "VolunteerObserver", "create"),
    implements("EventPublisher", "Subject"),
    aggregate("EventPublisher", "EventObserver", "notifies", "0..*"),
    assoc("EventPublisher", "EventRef", "", "", "0..1"),
    implements("VolunteerObserver", "EventObserver"), implements("NotificationLogger", "EventObserver"),
    uses("VolunteerObserver", "NotificationRef", "create"), uses("NotificationLogger", "NotificationRef", "create"),
])

# --- MVC and Singleton ----------------------------------------------------------------------------
panel("mvc", "MVC and Singleton", "How a request travels through the layers, ending at the one database connection",
      "mvc", {
    "Views": box("Views", "mvc", "View layer", fields=[
        "LoginView, RegisterView, HomeView, ProfileView, NotificationsView",
        "AddDonationView, PayDonationView, ReceiptView",
        "VolunteerPanelView, VolunteerProfileView, PickupTasksView,",
        "EventRegistrationView, HoursCertificateView",
        "DonationRequestView, ReceivedDistributionsView",
        "ManageUsersView, ManageEventsView, ImpactReportView, VolunteerAssignmentView",
        "AreaView, DonationStatusView, EventTypeView, FoodCategoryView, UnitView"]),
    "Controllers": box("Controllers", "mvc", "Controller layer", fields=[
        "AuthController, UserController, VolunteerController, DonationController,",
        "PickupController, BeneficiaryController, EventController,",
        "NotificationController, ReportController, LookupController"]),
    "VolunteerService": box("VolunteerService", "mvc", "Service layer", methods=[
        "+ updateProfile(...): void", "+ logHours(volunteerId, hours, note): void",
        "+ viewHours(volunteerId): List<VolunteerHours>"]),
    "PickupTaskService": box("PickupTaskService", "mvc", "Service layer", methods=[
        "+ viewAvailableTasks(volunteerId): List<PickupTask>", "+ viewMyTasks(volunteerId): List<PickupTask>",
        "+ acceptTask(taskId, volunteerId): boolean", "+ declineTask(taskId, volunteerId): void",
        "+ markPickedUp(taskId, volunteerId): void", "+ markDelivered(taskId, volunteerId): void",
        "+ assignTask(taskId, volunteerId): void"]),
    "OtherServices": box("Pattern services", "mvc", "Service layer", fields=[
        "DonationService, VolunteerMatchingService (Strategy)", "CertificateService (Factory Method)",
        "ReceiptService (Decorator)", "EventService (Observer)"]),
    "Repositories": box("Repositories", "mvc", "Repository layer", fields=[
        "Repository<T>: add, findById, findAll, update, delete",
        "UserRepository, VolunteerRepository, DonationRepository, ReceiptRepository,",
        "PickupTaskRepository, DistributionRepository, DonationRequestRepository,",
        "EventRepository, EventRegistrationRepository, NotificationRepository,",
        "CertificateRepository, and one per lookup table"]),
    "DatabaseManager": box("DatabaseManager", "singleton", tags=[("singleton", "Singleton")], fields=[
        "- uniqueInstance: DatabaseManager {static}", "- url: String", "- username: String", "- password: String"],
        methods=["- DatabaseManager()", "+ getInstance(): DatabaseManager {static}", "+ getConnection(): Connection"]),
}, [
    uses("Views", "Controllers", "calls"),
    uses("Controllers", "VolunteerService", "calls"), uses("Controllers", "PickupTaskService", "calls"),
    uses("Controllers", "OtherServices", "calls"),
    uses("VolunteerService", "Repositories", "calls"), uses("PickupTaskService", "Repositories", "calls"),
    uses("OtherServices", "Repositories", "calls"),
    uses("Repositories", "DatabaseManager", "getInstance"),
    assoc("DatabaseManager", "DatabaseManager", "uniqueInstance", "", "1"),
], rankdir="LR", ranksep=0.8)


# Rendering ----------------------------------------------------------------------------------------
def to_dot(p):
    out = ["digraph G {",
           f'rankdir={p["rankdir"]}; nodesep={p["nodesep"]}; ranksep={p["ranksep"]}; splines=true; bgcolor="transparent";',
           f'node [shape=plain fontname="{FONT}"];',
           f'edge [fontname="{FONT}" fontsize=10 color="{LINE}" fontcolor="{INK}" labeldistance=1.8 labelangle=35 penwidth=1.1];']
    for name, label in p["nodes"].items():
        out.append(f'"{name}" [label={label}];')
    for a, b, attrs in p["edges"]:
        out.append(f'"{a}" -> "{b}" [{attrs}];')
    for group in p["ranks"]:
        out.append("{rank=same; " + " ".join(f'"{n}";' for n in group) + "}")
    out.append("}")
    return "\n".join(out)


def render_panel(p):
    """Returns (width, height, inner svg markup) in points."""
    svg = subprocess.run(["dot", "-Tsvg"], input=to_dot(p), capture_output=True, text=True,
                         encoding="utf-8", check=True).stdout
    m = re.search(r'<svg width="(\d+)pt" height="(\d+)pt"\s+viewBox="([^"]+)"[^>]*>(.*)</svg>', svg, re.S)
    return int(m.group(1)), int(m.group(2)), m.group(3), m.group(4)


PAD, HEAD, GAP = 22, 58, 28
PAGE_MARGIN = 36


def panel_svg(key, x, y, width, height, data):
    p = PANELS[key]
    strong, light = COLORS[p["role"]]
    w, h, view, inner = data
    dx = x + (width - w) / 2
    return f"""
<g>
  <rect x="{x}" y="{y}" width="{width}" height="{height}" rx="14" fill="#FFFFFF" stroke="{strong}" stroke-width="2"/>
  <path d="M{x} {y + 14} a14 14 0 0 1 14 -14 h{width - 28} a14 14 0 0 1 14 14 v{HEAD - 14} h-{width} z" fill="{strong}"/>
  <text x="{x + 20}" y="{y + 26}" font-family="{FONT}" font-size="19" font-weight="bold" fill="#FFFFFF">{esc(p["title"])}</text>
  <text x="{x + 20}" y="{y + 46}" font-family="{FONT}" font-size="12.5" fill="#FFFFFF">{esc(p["subtitle"])}</text>
  <svg x="{dx}" y="{y + HEAD + (height - HEAD - h) / 2}" width="{w}" height="{h}" viewBox="{view}">{inner}</svg>
</g>"""


LEGEND_ITEMS = [("model", "Model (MVC): domain class"), ("lookup", "Lookup table"), ("strategy", "Strategy"),
                ("factory", "Factory Method"), ("decorator", "Decorator"), ("observer", "Observer"),
                ("singleton", "Singleton"), ("mvc", "MVC layers")]


def legend_svg(x, y, width):
    parts = [f'<text x="{x}" y="{y + 30}" font-family="{FONT}" font-size="30" font-weight="bold" fill="{INK}">FoodBridge Class Diagram</text>',
             f'<text x="{x}" y="{y + 54}" font-family="{FONT}" font-size="14" fill="#475569">Dashed boxes are references to a class drawn in full in the panel named inside them. Getters and setters are omitted.</text>']
    cx, cy = x, y + 72
    for role, label in LEGEND_ITEMS:
        parts.append(f'<rect x="{cx}" y="{cy}" width="20" height="20" rx="4" fill="{COLORS[role][0]}"/>'
                     f'<text x="{cx + 27}" y="{cy + 15}" font-family="{FONT}" font-size="14" fill="{INK}">{esc(label)}</text>')
        cx += 37 + len(label) * 7.6
    lx = x
    ly = cy + 42
    samples = [("Extends", "none", "hollow"), ("Implements", "6 4", "hollow"), ("Association", "none", "open"),
               ("Uses / creates", "6 4", "open"), ("Aggregation", "none", "odiamond"), ("Composition", "none", "diamond")]
    for label, dash, kind in samples:
        d = "" if dash == "none" else f' stroke-dasharray="{dash}"'
        parts.append(f'<line x1="{lx}" y1="{ly}" x2="{lx + 52}" y2="{ly}" stroke="{LINE}" stroke-width="1.5"{d}/>')
        if kind == "hollow":
            parts.append(f'<polygon points="{lx + 52},{ly - 6} {lx + 64},{ly} {lx + 52},{ly + 6}" fill="#FFFFFF" stroke="{LINE}" stroke-width="1.5"/>')
        elif kind == "open":
            parts.append(f'<polyline points="{lx + 54},{ly - 5} {lx + 64},{ly} {lx + 54},{ly + 5}" fill="none" stroke="{LINE}" stroke-width="1.5"/>')
        else:
            fill = "#FFFFFF" if kind == "odiamond" else LINE
            parts.append(f'<polygon points="{lx},{ly} {lx + 8},{ly - 5} {lx + 16},{ly} {lx + 8},{ly + 5}" fill="{fill}" stroke="{LINE}" stroke-width="1.5"/>')
        parts.append(f'<text x="{lx + 72}" y="{ly + 5}" font-family="{FONT}" font-size="14" fill="{INK}">{label}</text>')
        lx += 100 + len(label) * 7.6
    return "".join(parts), ly + 24 - y


# Page layout: rows of panels. Panels in a row share one height, and widths stretch to fill the page.
ROWS = [["users", "donations"], ["strategy"], ["factory", "decorator"], ["observer", "mvc"]]


def build():
    rendered = {k: render_panel(p) for k, p in PANELS.items()}
    for k, r in rendered.items():
        print(f"{k}: {r[0]} x {r[1]}")
    row_width = max(sum(rendered[k][0] + 2 * PAD for k in row) + GAP * (len(row) - 1) for row in ROWS)
    page_width = row_width + 2 * PAGE_MARGIN
    legend, legend_h = legend_svg(PAGE_MARGIN, PAGE_MARGIN, row_width)
    y = PAGE_MARGIN + legend_h + 12
    body = []
    for row in ROWS:
        natural = [rendered[k][0] + 2 * PAD for k in row]
        extra = (row_width - sum(natural) - GAP * (len(row) - 1)) / len(row)
        height = max(rendered[k][1] for k in row) + HEAD + 2 * PAD
        x = PAGE_MARGIN
        for k, n in zip(row, natural):
            w = n + extra
            body.append(panel_svg(k, x, y, w, height, rendered[k]))
            x += w + GAP
        y += height + GAP
    page_height = y - GAP + PAGE_MARGIN
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" '
           f'width="{page_width}" height="{page_height}" viewBox="0 0 {page_width} {page_height}">'
           f'<rect width="100%" height="100%" fill="#FFFFFF"/>{legend}{"".join(body)}</svg>')
    svg_path = os.path.join(OUT_DIR, "foodbridge-class-diagram.svg")
    with open(svg_path, "w", encoding="utf-8") as f:
        f.write(svg)
    print("page:", page_width, "x", page_height)
    return svg_path, int(page_width), int(page_height)


def to_png(svg_path, width, height):
    browsers = [r"C:\Program Files\Google\Chrome\Application\chrome.exe",
                r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"]
    browser = next((b for b in browsers if os.path.exists(b)), None)
    if browser is None:
        print("No Chrome or Edge found, so the PNG was not updated.")
        return
    png = os.path.join(OUT_DIR, "foodbridge-class-diagram.png")
    with tempfile.TemporaryDirectory() as profile:
        subprocess.run([browser, "--headless=new", "--disable-gpu", "--hide-scrollbars",
                        f"--user-data-dir={profile}", f"--window-size={width},{height}",
                        "--default-background-color=FFFFFFFF", "--force-device-scale-factor=2", f"--screenshot={png}",
                        "file:///" + svg_path.replace("\\", "/")], check=True, capture_output=True, timeout=120)


if __name__ == "__main__":
    path, w, h = build()
    to_png(path, w, h)
