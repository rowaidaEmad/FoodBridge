package foodbridge.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.operations.Distribution;
import foodbridge.models.operations.DonationRequest;
import foodbridge.models.users.*;
import foodbridge.repositories.*;

/** Beneficiary MVC workflows. All public operations check the live login session. */
public class BeneficiaryController {
    private final AuthController auth;
    private final DonationRequestRepository requestRepository;
    private final DistributionRepository distributionRepository;
    private final UserRepository users;
    private final FoodCategoryRepository categories;
    private final UnitRepository units;
    private final DonationRepository donations;

    /** Retained for compatibility; operations require a logged-in session. */
    public BeneficiaryController() { this(new AuthController()); }
    public BeneficiaryController(AuthController auth) {
        this(auth,new DonationRequestRepository(),new DistributionRepository(),new UserRepository(),
            new FoodCategoryRepository(),new UnitRepository(),new DonationRepository());
    }
    public BeneficiaryController(AuthController auth, DonationRequestRepository requests,
            DistributionRepository distributions, UserRepository users, FoodCategoryRepository categories,
            UnitRepository units, DonationRepository donations) {
        this.auth=Objects.requireNonNull(auth); this.requestRepository=Objects.requireNonNull(requests);
        this.distributionRepository=Objects.requireNonNull(distributions); this.users=Objects.requireNonNull(users);
        this.categories=Objects.requireNonNull(categories); this.units=Objects.requireNonNull(units);
        this.donations=Objects.requireNonNull(donations);
    }
    public Beneficiary getProfile() {
        User user=requireUser();
        if(!(user instanceof Beneficiary)) throw new SecurityException("Only beneficiaries can edit their profile here.");
        return beneficiary(user.getId());
    }
    public void updateProfile(int householdSize,String needs) {
        Beneficiary profile=getProfile();
        profile.setHouseholdSize(householdSize);
        String value=needs==null ? "" : needs.trim();
        users.updateBeneficiaryProfile(profile.getId(),householdSize,value);
        Beneficiary session=(Beneficiary)requireUser(); session.setHouseholdSize(householdSize); session.updateNeeds(value);
    }
    public void submitRequest(DonationRequest request) {
        Beneficiary owner=getProfile();
        if(request==null || request.getCategory()==null || request.getUnit()==null)
            throw new IllegalArgumentException("Choose a category and unit.");
        FoodCategory category=categories.findById(request.getCategory().getId());
        Unit unit=units.findById(request.getUnit().getId());
        if(category==null || unit==null) throw new IllegalArgumentException("The category or unit no longer exists. Refresh the form.");
        DonationRequest item=new DonationRequest(owner,category,request.getQuantity(),unit);
        requestRepository.add(item); request.setId(item.getId());
    }
    public List<DonationRequest> listRequests(int beneficiaryId) {
        requireOwnerOrAdmin(beneficiaryId); return requestRepository.findByBeneficiary(beneficiaryId);
    }
    public List<DonationRequest> listMyRequests() { return listRequests(getProfile().getId()); }
    public List<DonationRequest> listAllRequests() { requireAdmin(); return requestRepository.findAll(); }
    public void updateRequestStatus(int requestId,String status) {
        requireAdmin();
        if(!"APPROVED".equals(status) && !"REJECTED".equals(status)) throw new IllegalArgumentException("Choose APPROVED or REJECTED.");
        if(!requestRepository.decidePending(requestId,status))
            throw new IllegalArgumentException("Request was already decided or no longer exists. Refresh the list.");
    }
    public List<Distribution> listDistributions(int beneficiaryId) {
        requireOwnerOrAdmin(beneficiaryId); return distributionRepository.findByBeneficiary(beneficiaryId);
    }
    public List<Distribution> listMyDistributions() { return listDistributions(getProfile().getId()); }
    public List<Beneficiary> listBeneficiaries() {
        requireAdmin(); return users.findAll().stream().filter(u -> u instanceof Beneficiary).map(u -> (Beneficiary)u).toList();
    }
    public List<Volunteer> listVolunteers() {
        requireAdmin(); return users.findAll().stream().filter(u -> u instanceof Volunteer && u.isActive()).map(u -> (Volunteer)u).toList();
    }
    public List<FoodCategory> listCategories() { requireModuleUser(); return categories.findAll(); }
    public List<Unit> listUnits() { requireModuleUser(); return units.findAll(); }
    public List<FoodDonation> listFoodDonations() { requireAdmin(); return donations.findFoodDonations(); }
    public void recordDistribution(int beneficiaryId,int donationId,Integer volunteerId,LocalDate date,BigDecimal quantity) {
        requireAdmin();
        if(date==null || date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Delivery date must be today or earlier.");
        Beneficiary recipient=beneficiary(beneficiaryId);
        if(!recipient.isActive()) throw new IllegalArgumentException("Choose an active beneficiary.");
        if(donationId<=0) throw new IllegalArgumentException("Choose a food donation.");
        FoodDonation food=donations.findFoodById(donationId);
        if(food==null) throw new IllegalArgumentException("Food donation no longer exists or was cancelled.");
        Volunteer volunteer=null;
        if(volunteerId!=null) {
            User candidate=users.findById(volunteerId);
            if(!(candidate instanceof Volunteer) || !candidate.isActive()) throw new IllegalArgumentException("Choose an active volunteer.");
            volunteer=(Volunteer)candidate;
        }
        distributionRepository.add(new Distribution(food,recipient,volunteer,date,quantity));
    }
    private Beneficiary beneficiary(int id) {
        User user=users.findById(id);
        if(!(user instanceof Beneficiary)) throw new IllegalArgumentException("Beneficiary no longer exists.");
        return (Beneficiary)user;
    }
    private User requireUser() {
        User user=auth.getCurrentUser();
        if(user==null || !user.isActive()) throw new SecurityException("Please log in again.");
        return user;
    }
    private void requireAdmin() {
        if(!(requireUser() instanceof Admin)) throw new SecurityException("Only admins can perform this action.");
    }
    private void requireModuleUser() {
        User user=requireUser();
        if(!(user instanceof Beneficiary) && !(user instanceof Admin)) throw new SecurityException("Access is restricted to beneficiaries and admins.");
    }
    private void requireOwnerOrAdmin(int id) {
        User user=requireUser();
        if(!(user instanceof Admin) && (!(user instanceof Beneficiary) || user.getId()!=id))
            throw new SecurityException("You can only view your own beneficiary records.");
    }
}
