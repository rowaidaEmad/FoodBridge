package foodbridge.repositories;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import foodbridge.models.operations.Distribution;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.singleton.DatabaseManager;

/** Delivery records; no automatic inventory allocation or request fulfilment. */
public class DistributionRepository implements Repository<Distribution> {
    private final DatabaseManager db=DatabaseManager.getInstance();
    private static final String SELECT="SELECT d.*,b.name AS beneficiary_name,v.name AS volunteer_name FROM distributions d"
        + " JOIN users b ON b.id=d.beneficiary_id LEFT JOIN users v ON v.id=d.volunteer_id";
    @Override public void add(Distribution item) {
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement("INSERT INTO distributions(food_donation_id,beneficiary_id,volunteer_id,distribution_date,quantity) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            bind(p,item); p.executeUpdate(); try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) item.setId(r.getInt(1)); }
        } catch(SQLException e) { throw failure("save distribution",e); }
    }
    @Override public Distribution findById(int id) { return query(" WHERE d.id=?",id).stream().findFirst().orElse(null); }
    @Override public List<Distribution> findAll() { return query(""); }
    public List<Distribution> findByBeneficiary(int id) { return query(" WHERE d.beneficiary_id=?",id); }
    public List<Distribution> findBetween(LocalDate start,LocalDate end) { return query(" WHERE d.distribution_date BETWEEN ? AND ?",Date.valueOf(start),Date.valueOf(end)); }
    private List<Distribution> query(String suffix,Object... args) {
        List<Distribution> result=new ArrayList<>();
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement(SELECT+suffix+" ORDER BY d.distribution_date DESC,d.id DESC")) {
            for(int i=0;i<args.length;i++) p.setObject(i+1,args[i]);
            try(ResultSet r=p.executeQuery()) { while(r.next()) {
                Beneficiary b=new Beneficiary(); b.setId(r.getInt("beneficiary_id")); b.setName(r.getString("beneficiary_name"));
                FoodDonation food=new FoodDonation(); food.setId(r.getInt("food_donation_id"));
                Volunteer v=null; int volunteerId=r.getInt("volunteer_id");
                if(!r.wasNull()) { v=new Volunteer(); v.setId(volunteerId); v.setName(r.getString("volunteer_name")); }
                Distribution item=new Distribution(food,b,v,r.getDate("distribution_date").toLocalDate(),r.getBigDecimal("quantity"));
                item.setId(r.getInt("id")); result.add(item);
            }}
        } catch(SQLException e) { throw failure("load distributions",e); }
        return result;
    }
    @Override public void update(Distribution item) {
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement("UPDATE distributions SET food_donation_id=?,beneficiary_id=?,volunteer_id=?,distribution_date=?,quantity=? WHERE id=?")) {
            bind(p,item); p.setInt(6,item.getId()); if(p.executeUpdate()!=1) throw new IllegalArgumentException("Distribution no longer exists.");
        } catch(SQLException e) { throw failure("update distribution",e); }
    }
    @Override public void delete(int id) {
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM distributions WHERE id=?")) {
            p.setInt(1,id); if(p.executeUpdate()!=1) throw new IllegalArgumentException("Distribution no longer exists.");
        } catch(SQLException e) { throw failure("delete distribution",e); }
    }
    private void bind(PreparedStatement p,Distribution item) throws SQLException {
        p.setInt(1,item.getDonation().getId()); p.setInt(2,item.getBeneficiary().getId());
        if(item.getVolunteer()==null) p.setNull(3,Types.BIGINT); else p.setInt(3,item.getVolunteer().getId());
        p.setDate(4,Date.valueOf(item.getDate())); p.setBigDecimal(5,item.getQuantity());
    }
    private RuntimeException failure(String action,SQLException e) { return new IllegalStateException("Could not "+action+". Check the database connection and selected records.",e); }
}
