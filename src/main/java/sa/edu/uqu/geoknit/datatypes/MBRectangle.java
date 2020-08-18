package sa.edu.uqu.geoknit.datatypes;

public class MBRectangle {
    private Spatial center;
    private double halfDimention;

    public MBRectangle(Spatial center, double halfDimention) {
        this.center = center;
        this.halfDimention = halfDimention;
    }

    public Spatial getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public double getHalfDimention() {
        return halfDimention;
    }

    public void setHalfDimention(double halfDimention) {
        this.halfDimention = halfDimention;
    }

    public double getWestX() {
        return this.center.getX() - this.halfDimention;
    }

    public double getEastX() {
        return this.center.getX() + this.halfDimention;
    }

    public double getNorthY() {
        return this.center.getY() + this.halfDimention;
    }

    public double getSouthY() {
        return this.center.getY() - this.halfDimention;
    }

    public boolean intersects(MBRectangle r) {
        return
                !(r.getEastX() < this.getWestX()
                        || r.getWestX() > this.getEastX()
                        || r.getNorthY() < this.getSouthY()
                        || r.getSouthY() > this.getNorthY());
//		return
//			!( this.getWestX() > r.getEastX()
//			|| this.getEastX() < r.getWestX()
//			|| this.getNorthY() > r.getSouthY()
//			|| this.getSouthY() < r.getNorthY());

    }

    public boolean contains(Spatial point) {
        return
                point.getX() < this.getEastX()
                        && point.getX() > this.getWestX()
                        && point.getY() > this.getSouthY()
                        && point.getY() < this.getNorthY();
    }

    @Override
    public String toString() {
        String output = "";
        output += "[x1=" + this.getWestX() + ", y1=" + this.getNorthY() + ", x2=" + this.getEastX() + ", y2=" + this.getSouthY() + "]";
        return output;
    }
}
