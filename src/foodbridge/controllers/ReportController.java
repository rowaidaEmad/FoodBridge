package foodbridge.controllers;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import foodbridge.models.operations.Distribution;
import foodbridge.models.operations.ImpactReport;
import foodbridge.models.users.Admin;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.repositories.DistributionRepository;

/** Admin reports derived from persisted distributions over an inclusive date range. */
public class ReportController {
    private final AuthController auth;
    private final DistributionRepository distributionRepository;
    public ReportController() { this(new AuthController()); }
    public ReportController(AuthController auth) { this(auth,new DistributionRepository()); }
    public ReportController(AuthController auth,DistributionRepository distributions) {
        this.auth=Objects.requireNonNull(auth); this.distributionRepository=Objects.requireNonNull(distributions);
    }
    /** Compatibility entry point: period format is yyyy-MM-dd..yyyy-MM-dd. */
    public ImpactReport generateReport(String period) {
        if(period==null || period.split("\\.\\.",-1).length!=2) throw new IllegalArgumentException("Use yyyy-MM-dd..yyyy-MM-dd for the report period.");
        String[] dates=period.split("\\.\\.");
        return generateReport(LocalDate.parse(dates[0].trim()),LocalDate.parse(dates[1].trim()));
    }
    public ImpactReport generateReport(LocalDate start,LocalDate end) {
        User user=auth.getCurrentUser();
        if(!(user instanceof Admin) || !user.isActive()) throw new SecurityException("Only logged-in admins can view impact reports.");
        if(start==null || end==null || start.isAfter(end)) throw new IllegalArgumentException("Start date must be on or before the end date.");
        var distributions=distributionRepository.findBetween(start,end);
        Set<Integer> households=new HashSet<>();
        Map<Integer,Volunteer> volunteers=new LinkedHashMap<>();
        Map<Integer,Integer> counts=new LinkedHashMap<>();
        for(Distribution d:distributions) {
            households.add(d.getBeneficiary().getId());
            Volunteer v=d.getVolunteer();
            if(v!=null) { volunteers.putIfAbsent(v.getId(),v); counts.merge(v.getId(),1,Integer::sum); }
        }
        Map<Volunteer,Integer> perVolunteer=new LinkedHashMap<>();
        counts.forEach((id,count) -> perVolunteer.put(volunteers.get(id),count));
        return new ImpactReport(start+" to "+end,households.size(),distributions.size(),perVolunteer);
    }
}
