import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import foodbridge.controllers.*;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.lookups.*;
import foodbridge.models.operations.*;
import foodbridge.models.users.*;
import foodbridge.patterns.singleton.DatabaseManager;
import foodbridge.repositories.*;

/** Integration tests. Run ONLY against an isolated foodbridge_beneficiary_test database. */
public class BeneficiaryModuleTest {
    private static int assertions;
    static final String PASSWORD="test-only-password";
    static void check(boolean condition,String message) {
        assertions++; if(!condition) throw new AssertionError(message);
    }
    static void rejects(Class<? extends Throwable> type,Runnable action,String message) {
        assertions++;
        try { action.run(); } catch(Throwable e) { if(type.isInstance(e)) return; throw new AssertionError(message,e); }
        throw new AssertionError(message);
    }
    static int sqlId(String sql) throws SQLException {
        try(Connection c=DatabaseManager.getInstance().getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { r.next(); return r.getInt(1); }
    }
    static void sql(String sql) throws SQLException {
        try(Connection c=DatabaseManager.getInstance().getConnection(); Statement s=c.createStatement()) { s.execute(sql); }
    }
    static Beneficiary addBeneficiary(AuthController auth,String name) {
        Beneficiary b=new Beneficiary(); b.setName(name); b.changeEmail(name+"@example.test"); b.setHouseholdSize(3); b.updateNeeds("Rice"); auth.register(b,PASSWORD); return b;
    }
    static DonationRequest request(FoodCategory category,Unit unit,String quantity) {
        DonationRequest r=new DonationRequest(); r.setCategory(category); r.setUnit(unit); r.setQuantity(new BigDecimal(quantity)); return r;
    }
    public static void main(String[] args) throws Exception {
        try(Connection c=DatabaseManager.getInstance().getConnection()) {
            if(!"foodbridge_beneficiary_test".equals(c.getCatalog())) throw new IllegalStateException("Refusing to modify a non-test database.");
        }
        AuthController beneficiarySession=new AuthController(),adminSession=new AuthController();
        Beneficiary b1=addBeneficiary(beneficiarySession,"beneficiary-one"),b2=addBeneficiary(beneficiarySession,"beneficiary-two");
        Admin admin=new Admin(); admin.setName("Admin"); admin.changeEmail("admin@example.test"); adminSession.register(admin,PASSWORD);
        beneficiarySession.login(b1.getEmail(),PASSWORD); adminSession.login(admin.getEmail(),PASSWORD);
        BeneficiaryController self=new BeneficiaryController(beneficiarySession),management=new BeneficiaryController(adminSession);
        check(self.getProfile().getId()==b1.getId(),"Profile uses logged-in beneficiary");
        check(management.listBeneficiaries().size()==2,"Admin can list beneficiaries");
        rejects(SecurityException.class,() -> self.listRequests(b2.getId()),"Beneficiaries cannot read another user's requests");
        rejects(SecurityException.class,() -> self.listDistributions(b2.getId()),"Beneficiaries cannot read another user's deliveries");
        rejects(SecurityException.class,self::listAllRequests,"Beneficiary cannot list all requests");
        FoodCategory category=self.listCategories().get(0); Unit unit=self.listUnits().get(0);
        rejects(IllegalArgumentException.class,() -> request(category,unit,"0"),"Zero quantity rejected");
        rejects(IllegalArgumentException.class,() -> request(category,unit,"-1"),"Negative quantity rejected");
        rejects(IllegalArgumentException.class,() -> request(category,unit,"1.0001"),"Precision beyond schema rejected");
        rejects(IllegalArgumentException.class,() -> request(category,unit,"100000000000"),"Overflow rejected");
        DonationRequest r=request(category,unit,"4.125"); r.setBeneficiary(b2); r.updateStatus("APPROVED"); r.setDate(LocalDate.now().minusDays(20)); self.submitRequest(r);
        DonationRequest saved=new DonationRequestRepository().findById(r.getId());
        check(saved.getBeneficiary().getId()==b1.getId(),"Submitted owner cannot be forged");
        check(saved.getStatus().equals("PENDING"),"Submitted status cannot be forged");
        check(saved.getDate().equals(LocalDate.now()),"Submitted date set by controller");
        check(self.listMyRequests().size()==1,"Own requests loaded");
        rejects(SecurityException.class,() -> self.updateRequestStatus(r.getId(),"APPROVED"),"Only admin decides requests");
        management.updateRequestStatus(r.getId(),"APPROVED");
        check(new DonationRequestRepository().findById(r.getId()).getStatus().equals("APPROVED"),"Approval persisted");
        rejects(IllegalArgumentException.class,() -> management.updateRequestStatus(r.getId(),"REJECTED"),"Cannot decide a request twice");
        rejects(IllegalArgumentException.class,() -> management.updateRequestStatus(r.getId(),"FULFILLED"),"Unsupported status rejected");
        DonationRequest second=request(category,unit,"2"); self.submitRequest(second); management.updateRequestStatus(second.getId(),"REJECTED");
        check(new DonationRequestRepository().findById(second.getId()).getStatus().equals("REJECTED"),"Rejection persisted");
        FoodCategory missing=new FoodCategory(999999,"Missing");
        rejects(IllegalArgumentException.class,() -> self.submitRequest(request(missing,unit,"1")),"Missing lookup rejected");
        // Existing donation and volunteer records simulate data owned by those modules.
        int donor=sqlId("INSERT INTO users(name,email,password_hash,role) VALUES ('Donor','donor@example.test','unused','DONOR') RETURNING id");
        sql("INSERT INTO donors(user_id) VALUES ("+donor+")");
        int volunteer=sqlId("INSERT INTO users(name,email,password_hash,role) VALUES ('Volunteer','volunteer@example.test','unused','VOLUNTEER') RETURNING id");
        sql("INSERT INTO volunteers(user_id) VALUES ("+volunteer+")");
        int food=sqlId("INSERT INTO donations(donor_id,status_id,donation_type) VALUES ("+donor+",(SELECT id FROM donation_statuses WHERE name='Posted'),'FOOD') RETURNING id");
        sql("INSERT INTO food_donations(donation_id) VALUES ("+food+")");
        sql("INSERT INTO food_items(food_donation_id,name,quantity,category_id,unit_id) VALUES ("+food+",'Rice',20,"+category.getId()+","+unit.getId()+"),("+food+",'Mixed item',5,"+category.getId()+","+unit.getId()+")");
        List<FoodDonation> foods=management.listFoodDonations();
        check(foods.size()==1 && foods.get(0).getItems().size()==2,"Multi-item donations preserved and loaded without duplicate donations");
        check(foods.get(0).toString().contains("Rice"),"Donation selector displays contents");
        LocalDate today=LocalDate.now(),yesterday=today.minusDays(1);
        rejects(SecurityException.class,() -> self.recordDistribution(b1.getId(),food,null,today,BigDecimal.ONE),"Beneficiary cannot record a delivery");
        rejects(IllegalArgumentException.class,() -> management.recordDistribution(b1.getId(),food,null,today.plusDays(1),BigDecimal.ONE),"Future delivery rejected");
        rejects(IllegalArgumentException.class,() -> management.recordDistribution(b1.getId(),food,donor,today,BigDecimal.ONE),"Donor cannot be used as volunteer");
        rejects(IllegalArgumentException.class,() -> management.recordDistribution(b1.getId(),999999,null,today,BigDecimal.ONE),"Missing donation rejected");
        management.recordDistribution(b1.getId(),food,volunteer,today,new BigDecimal("4.125"));
        management.recordDistribution(b1.getId(),food,volunteer,today,BigDecimal.ONE);
        management.recordDistribution(b2.getId(),food,null,today,BigDecimal.ONE);
        management.recordDistribution(b2.getId(),food,null,yesterday,BigDecimal.ONE);
        check(self.listMyDistributions().size()==2,"Delivery history scoped to beneficiary");
        check(management.listDistributions(b2.getId()).stream().allMatch(d -> d.getVolunteer()==null),"Optional volunteer round trips as null");
        self.updateProfile(5,"  Rice and vegetables  ");
        check(self.getProfile().getHouseholdSize()==5 && self.getProfile().getNeeds().equals("Rice and vegetables"),"Profile changes persisted");
        check(self.listMyDistributions().size()==2,"Profile edit retains delivery foreign keys");
        check(self.listMyRequests().size()==2,"Profile edit retains request foreign keys");
        rejects(IllegalArgumentException.class,() -> self.updateProfile(0,"bad"),"Invalid household rejected");
        ReportController reports=new ReportController(adminSession);
        ImpactReport report=reports.generateReport(today,today);
        check(report.getDeliveryCount()==3,"Inclusive date range includes both endpoints");
        check(report.getFamiliesServed()==2,"Households counted once despite repeat deliveries");
        check(report.getDeliveriesPerVolunteer().size()==1 && report.getDeliveriesPerVolunteer().values().iterator().next()==2,"Volunteer rows aggregated by ID, not Java object identity");
        check(!report.isMealCountAvailable(),"No invented meal conversion");
        rejects(IllegalStateException.class,report::getMealsDelivered,"Unknown meal count never reported as zero");
        check(reports.generateReport(yesterday+".."+today).getDeliveryCount()==4,"String period entry point works");
        check(reports.generateReport(today.minusDays(10),today.minusDays(9)).getFamiliesServed()==0,"Empty report supported");
        rejects(IllegalArgumentException.class,() -> reports.generateReport(today,yesterday),"Reversed range rejected");
        rejects(SecurityException.class,() -> new ReportController(beneficiarySession).generateReport(today,today),"Beneficiary cannot read admin reports");
        sql("UPDATE users SET is_active=FALSE WHERE id="+b2.getId());
        rejects(IllegalArgumentException.class,() -> management.recordDistribution(b2.getId(),food,null,today,BigDecimal.ONE),"Inactive beneficiary cannot receive new record");
        beneficiarySession.logout();
        rejects(SecurityException.class,self::getProfile,"Logout invalidates controller access");
        // Preserve test sessions for the optional Swing smoke test in this isolated process.
        if(args.length>0 && args[0].equals("--gui")) {
            beneficiarySession.login(b1.getEmail(),PASSWORD);
            BeneficiaryGuiSmokeTest.run(beneficiarySession,adminSession);
        }
        System.out.println("PASS: "+assertions+" beneficiary workflow/database assertions.");
    }
}
