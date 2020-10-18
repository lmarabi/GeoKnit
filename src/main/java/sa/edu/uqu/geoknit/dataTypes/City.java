package sa.edu.uqu.geoknit.dataTypesOsa;

public class City extends  Point {

    public Long city_id;
    public Long region_id;
    public String name_ar ,name_en;
    public Point center ;
    public Polygon boundariesP;
    public String boundaries;


    public Long getCity_id() {
        return city_id;
    }

    public void setCity_id(Long city_id) {
        this.city_id = city_id;
    }

    public Long getRegion_id() {
        return region_id;
    }

    public void setRegion_id(Long region_id) {
        this.region_id = region_id;
    }

    public String getName_ar() {
        return name_ar;
    }

    public void setName_ar(String name_ar) {
        this.name_ar = name_ar;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Polygon getBoundariesP() {
        return boundariesP;
    }

    public void setBoundariesP(Polygon boundariesP) {
        this.boundariesP = boundariesP;
    }

    public String getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(String boundaries) {
        this.boundaries = boundaries;
    }

    @Override
    public String toString() {
        return "City{" +
                "city_id=" + city_id +
                ", region_id=" + region_id +
                ", name_ar='" + name_ar + '\'' +
                ", name_en='" + name_en + '\'' +
                ", center=" + center.toString() +
                ", boundaries=" + boundariesP.toString() +
                '}';
    }
}
