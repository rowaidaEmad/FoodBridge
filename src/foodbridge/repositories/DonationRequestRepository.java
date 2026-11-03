package foodbridge.repositories;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import foodbridge.models.operations.DonationRequest;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.patterns.singleton.DatabaseManager;

/** JDBC storage for beneficiary requests. */
public class DonationRequestRepository implements Repository<DonationRequest> {
    private final DatabaseManager db = DatabaseManager.getInstance();
    private static final String SELECT = "SELECT r.*, u.name AS beneficiary_name, c.name AS category_name, n.name AS unit_name"
        + " FROM donation_requests r JOIN users u ON u.id=r.beneficiary_id"
        + " JOIN food_categories c ON c.id=r.category_id JOIN units n ON n.id=r.unit_id";
    @Override public void add(DonationRequest item) {
        String sql = "INSERT INTO donation_requests(beneficiary_id,category_id,quantity,unit_id,request_date,status) VALUES (?,?,?,?,?,?)";
        try (Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p,item); p.executeUpdate();
            try (ResultSet r=p.getGeneratedKeys()) { if(r.next()) item.setId(r.getInt(1)); }
        } catch(SQLException e) { throw failure("save request",e); }
    }
    @Override public DonationRequest findById(int id) { return query(" WHERE r.id=?",id).stream().findFirst().orElse(null); }
    @Override public List<DonationRequest> findAll() { return query(""); }
    public List<DonationRequest> findByBeneficiary(int id) { return query(" WHERE r.beneficiary_id=?",id); }
    private List<DonationRequest> query(String suffix, Object... args) {
        List<DonationRequest> result=new ArrayList<>();
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement(SELECT+suffix+" ORDER BY r.request_date DESC,r.id DESC")) {
            for(int i=0;i<args.length;i++) p.setObject(i+1,args[i]);
            try(ResultSet r=p.executeQuery()) { while(r.next()) {
                Beneficiary b=new Beneficiary(); b.setId(r.getInt("beneficiary_id")); b.setName(r.getString("beneficiary_name"));
                DonationRequest item=new DonationRequest(b,new FoodCategory(r.getInt("category_id"),r.getString("category_name")),
                    r.getBigDecimal("quantity"),new Unit(r.getInt("unit_id"),r.getString("unit_name")));
                item.setId(r.getInt("id")); item.setDate(r.getDate("request_date").toLocalDate()); item.updateStatus(r.getString("status")); result.add(item);
            }}
        } catch(SQLException e) { throw failure("load requests",e); }
        return result;
    }
    /** Conditional update prevents two admins deciding the same pending request. */
    public boolean decidePending(int id,String status) {
        if(!"APPROVED".equals(status) && !"REJECTED".equals(status)) throw new IllegalArgumentException("Choose APPROVED or REJECTED.");
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement("UPDATE donation_requests SET status=? WHERE id=? AND status='PENDING'")) {
            p.setString(1,status); p.setInt(2,id); return p.executeUpdate()==1;
        } catch(SQLException e) { throw failure("update request status",e); }
    }
    @Override public void update(DonationRequest item) {
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement("UPDATE donation_requests SET beneficiary_id=?,category_id=?,quantity=?,unit_id=?,request_date=?,status=? WHERE id=?")) {
            bind(p,item); p.setInt(7,item.getId());
            if(p.executeUpdate()!=1) throw new IllegalArgumentException("Request no longer exists.");
        } catch(SQLException e) { throw failure("update request",e); }
    }
    @Override public void delete(int id) {
        try(Connection c=db.getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM donation_requests WHERE id=?")) {
            p.setInt(1,id); if(p.executeUpdate()!=1) throw new IllegalArgumentException("Request no longer exists.");
        } catch(SQLException e) { throw failure("delete request",e); }
    }
    private void bind(PreparedStatement p,DonationRequest item) throws SQLException {
        p.setInt(1,item.getBeneficiary().getId()); p.setInt(2,item.getCategory().getId()); p.setBigDecimal(3,item.getQuantity());
        p.setInt(4,item.getUnit().getId()); p.setDate(5,Date.valueOf(item.getDate())); p.setString(6,item.getStatus());
    }
    private RuntimeException failure(String action,SQLException e) { return new IllegalStateException("Could not "+action+". Check the database connection and selected records.",e); }
}
