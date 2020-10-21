package sa.edu.uqu.geoknit.dataTypes;

import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class City extends  Point implements Runnable,Serializable ,Comparable<String> , Binarylizable {

//    private static final long serialVersionUID = 91486038680078L;

    public Long city_id;
    public Long region_id;
    public String name_ar ,name_en;
    public Point center ;
    public Polygon boundariesP;
    public String boundaries;

    public City(){ }

    public City(Long l,String s){ this.city_id= l ; this.name_en=s ;}

    public City(Long city_id, Long region_id, String name_ar, String name_en, Point center, Polygon boundariesP, String boundaries) {
        this.city_id = city_id;
        this.region_id = region_id;
        this.name_ar = name_ar;
        this.name_en = name_en;
        this.center = center;
        this.boundariesP = boundariesP;
        this.boundaries = boundaries;
    }

    public City(double x, double y, Long city_id, Long region_id, String name_ar, String name_en, Point center, Polygon boundariesP, String boundaries) {
        super(x, y);
        this.city_id = city_id;
        this.region_id = region_id;
        this.name_ar = name_ar;
        this.name_en = name_en;
        this.center = center;
        this.boundariesP = boundariesP;
        this.boundaries = boundaries;
    }

    public City(double[] point, Long city_id, Long region_id, String name_ar, String name_en, Point center, Polygon boundariesP, String boundaries) {
        super(point);
        this.city_id = city_id;
        this.region_id = region_id;
        this.name_ar = name_ar;
        this.name_en = name_en;
        this.center = center;
        this.boundariesP = boundariesP;
        this.boundaries = boundaries;
    }

    public City(Point s, Long city_id, Long region_id, String name_ar, String name_en, Point center, Polygon boundariesP, String boundaries) {
        super(s);
        this.city_id = city_id;
        this.region_id = region_id;
        this.name_ar = name_ar;
        this.name_en = name_en;
        this.center = center;
        this.boundariesP = boundariesP;
        this.boundaries = boundaries;
    }

    public City(String s, Long city_id, Long region_id, String name_ar, String name_en, Point center, Polygon boundariesP, String boundaries) {
        super(s);
        this.city_id = city_id;
        this.region_id = region_id;
        this.name_ar = name_ar;
        this.name_en = name_en;
        this.center = center;
        this.boundariesP = boundariesP;
        this.boundaries = boundaries;
    }

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
//                ", region_id=" + region_id +
//                ", name_ar='" + name_ar + '\'' +
//                ", name_en='" + name_en + '\'' +
//                ", center=" + center.toString() +
//                ", boundaries=" + boundariesP.toString() +
                '}';
    }

    @Override
    public void run() {

    }

    @Override
    public int compareTo(@NotNull String s) {
        return 0;
    }

    @Override
    public void writeBinary(BinaryWriter binaryWriter) throws BinaryObjectException {
        binaryWriter.writeString("name_en", name_en);
        binaryWriter.writeLong("city_id", city_id);
    }

    @Override
    public void readBinary(BinaryReader binaryReader) throws BinaryObjectException {
        name_en = binaryReader.readString("street");
        city_id = binaryReader.readLong("zip");
    }
}
