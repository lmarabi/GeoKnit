package sa.edu.uqu.geoknit.dataTypesOsa;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LineString implements sa.edu.uqu.geoknit.dataTypesOsa.Shape, Comparable<LineString> {
    public double x1;
    public double y1;
    public double x2;
    public double y2;
    public int distance_number=0;

    public LineString() {
        this(0, 0 ,0 , 0);
    }

    public LineString(double x1, double y1 , double x2 , double y2) {
        set(x1, y1 ,x2 ,y2);
    }

    public LineString(double[] point1 , double[] point2){
        this.x1 = point1[0];
        this.y1 = point1[1];
        this.x2 = point2[0];
        this.y2 = point2[1];
    }

    /**
     * A copy constructor from any shape of type Point (or subclass of Point)
     * @param s1 ,s2
     */
    public LineString(sa.edu.uqu.geoknit.dataTypesOsa.Point s1 , Point s2) {
        this.x1 = s1.x;
        this.y1 = s1.y;
        this.x2 = s2.x;
        this.y2 = s2.y;
    }

    public void set(double x1, double y1 , double x2 , double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public void readJson(double[] point1    , double[] point2) {
        this.x1 = point1[0];
        this.y1 = point1[1];
        this.x2 = point2[0];
        this.y2 = point2[1];
    }

//    public void write(DataOutput out) throws IOException {
//        out.writeDouble(x);
//        out.writeDouble(y);
//    }
//
//    public void readFields(DataInput in) throws IOException {
//        this.x = in.readDouble();
//        this.y = in.readDouble();
//    }

    public int compareTo(sa.edu.uqu.geoknit.dataTypesOsa.Shape s) {
        LineString pt2 = (LineString) s;

        // Sort by id
        double difference = this.x1 - pt2.x1;
        if (difference == 0) {
            difference = this.y1 - pt2.y1;
        }
        if (difference == 0)
            return 0;
        return difference > 0 ? 1 : -1;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        LineString r2 = (LineString) obj;
        return this.x1 == r2.x1 && this.y1 == r2.y1
                && this.x2 == r2.x2 && this.y2 == r2.y2 ;
    }

    //need to search
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.x1);
        result = (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.y1);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        return result;
    }
//
//    public double distanceTo(Point s) {
//        double dx = s.x - this.x;
//        double dy = s.y - this.y;
//        return Math.sqrt(dx*dx+dy*dy);
//    }

    @Override
    public LineString clone() {
        return new LineString(this.x1, this.y1 ,  this.x2 , this.y2);
    }

    @Override
    public void draw(Graphics g, sa.edu.uqu.geoknit.dataTypesOsa.Rectangle fileMBR, int imageWidth, int imageHeight, double scale) {

    }

    @Override
    public void draw(Graphics g, double xscale, double yscale) {

    }
//    @Override
//    public void draw(Graphics g, double xscale1, double yscale1 , double xscale2, double yscale2) {
//        int imgx1 = (int) Math.round(x1 * xscale1);
//        int imgy1 = (int) Math.round(y1 * yscale1);
//        int imgx2 = (int) Math.round(x2 * xscale2);
//        int imgy2 = (int) Math.round(y2 * yscale2);
//        g.drawLine( imgx1 , imgy1 , imgx2 , imgy2 );
//    }

    /**
     * Returns the minimal bounding rectangle of this point. This method returns
     * the smallest rectangle that contains this point. For consistency with
     * other methods such as {@link sa.edu.uqu.geoknit.dataTypesOsa.Rectangle#isIntersected(sa.edu.uqu.geoknit.dataTypesOsa.Shape)}, the rectangle
     * cannot have a zero width or height. Thus, we use the method
     * {@link Math#ulp(double)} to compute the smallest non-zero rectangle that
     * contains this point. In other words, for a point <code>p</code> the
     * following statement should return true.
     * <code>p.getMBR().isIntersected(p);</code>
     */

    // not ready
    @Override
    public sa.edu.uqu.geoknit.dataTypesOsa.Rectangle getMBR() {
        return new Rectangle(x1, y1, x1 + Math.ulp(x1), y1 + Math.ulp(y1));
    }
    // not ready


    // not ready
    @Override
    public double distanceTo(double px, double py) {
        double dx = x1 - px;
        double dy = y1 - py;
        return Math.sqrt(dx * dx + dy * dy);
    }
    // not ready


    public double nearestDistanceTo(Point p1 , Point p2) {
       return  nearestDistanceTo(new LineString( p1 , p2 )) ;
    }
    public double nearestDistanceTo(LineString line) {
       return nearestDistanceTo(line.x1 , line.y1,line.x2 , line.y2);
    }
    public double nearestDistanceTo(double px1, double py1 ,double px2, double py2) {

        double [] distances = {
                //distance first point in line1 and first point in line2
                new Point( x1 , y1 ).distanceTo(new Point( px1 , py1 )),
                //distance first point in line1 and second point in line2
                new Point( x1 , y1 ).distanceTo(new Point( px2 , py2 )),
                //distance second point in line1 and first point in line2
                new Point( x2 , y2 ).distanceTo(new Point( px1 , py1 )),
                //distance second point in line1 and second point in line2
                new Point( x2 , y2 ).distanceTo(new Point( px2 , py2 ))

        };
        double minDistance = distances[0];
        for (int i = 1; i < 4; i++) {
            if(distances[i] < minDistance){
                minDistance = distances[i];
                this.distance_number =i;
            }
        }

        return minDistance;

    }

    public Shape getIntersection(Shape s) {
        return getMBR().getIntersection(s);
    }

    @Override
    public boolean isIntersected(Shape s) {
        return getMBR().isIntersected(s);
    }

    @Override
    public String toString() {
        return "Point: ("+x1+","+y1+")";
    }

//    @Override
//    public Text toText(Text text) {
//        TextSerializerHelper.serializeDouble(x, text, ',');
//        TextSerializerHelper.serializeDouble(y, text, '\0');
//        return text;
//    }

//    @Override
//    public void fromText(Text text) {
//        x = TextSerializerHelper.consumeDouble(text, ',');
//        y = TextSerializerHelper.consumeDouble(text, '\0');
//    }
    @Override
    public int compareTo(@NotNull LineString o) {
        if ( x1 < o.x1 || x2 <o.x2 )
            return -1;
        if (x1 > o.x1 || x2 >o.x2)
            return 1;
        if (y1 < o.y1 || y2 < o.y2 )
            return -1;
        if (y1 > o.y1 || y2 > o.y2)
            return 1;
        return 0;
    }



//    @Override
//    public void draw(Graphics g, Rectangle fileMBR, int imageWidth,
//                     int imageHeight, double scale) {
//        int imageX = (int) Math.round((this.x - fileMBR.x1) * imageWidth / fileMBR.getWidth());
//        int imageY = (int) Math.round((this.y - fileMBR.y1) * imageHeight / fileMBR.getHeight());
//        g.drawLine( imgx1 , imgy1 , imgx2 , imgy2 );
//    }




}