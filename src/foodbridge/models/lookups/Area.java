package foodbridge.models.lookups;

/**
 * Lookup table (CRUD).
 */
public class Area {
    private int id;
    private String city;
    private String district;

    public Area() {
    }

    public Area(int id, String city, String district) {
        this.id = id;
        this.city = city;
        this.district = district;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return city + " - " + district;
    }

    public int getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
