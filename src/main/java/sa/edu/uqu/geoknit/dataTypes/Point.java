package sa.edu.uqu.geoknit.dataTypes;

/***********************************************************************
 * Copyright (c) 2015 by Regents of the University of Minnesota.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 *
 *************************************************************************/

import java.awt.*;
import java.util.Arrays;


/**
 * A class that holds coordinates of a point.
 * @author Osamah
 *
 */
public class Point implements sa.edu.uqu.geoknit.dataTypes.Shape, Comparable<Point> {
    public double x;
    public double y;

    public Point() {
        this(0, 0);
    }

    public Point(double x, double y) {
        set(x, y);
    }

    public Point(double[] point){
        this.x = point[0];
        this.y = point[1];
    }

    /**
     * A copy constructor from any shape of type Point (or subclass of Point)
     * @param s
     */
    public Point(Point s) {
        this.x = s.x;
        this.y = s.y;
    }

    public Point(String s){
        fromText(s);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void readJson(double[] point){
        this.x = point[0];
        this.y = point[1];
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

    public int compareTo(sa.edu.uqu.geoknit.dataTypes.Shape s) {
        Point pt2 = (Point) s;

        // Sort by id
        double difference = this.x - pt2.x;
        if (difference == 0) {
            difference = this.y - pt2.y;
        }
        if (difference == 0)
            return 0;
        return difference > 0 ? 1 : -1;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        Point r2 = (Point) obj;
        return this.x == r2.x && this.y == r2.y;
    }

    //need to search
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.y);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    public double distanceTo(Point s) {
        double dx = s.x - this.x;
        double dy = s.y - this.y;
        return Math.sqrt(dx*dx+dy*dy);
    }

    @Override
    public Point clone() {
        return new Point(this.x, this.y);
    }

    /**
     * Returns the minimal bounding rectangle of this point. This method returns
     * the smallest rectangle that contains this point. For consistency with
     * other methods such as {@link sa.edu.uqu.geoknit.dataTypes.Rectangle#isIntersected(sa.edu.uqu.geoknit.dataTypes.Shape)}, the rectangle
     * cannot have a zero width or height. Thus, we use the method
     * {@link Math#ulp(double)} to compute the smallest non-zero rectangle that
     * contains this point. In other words, for a point <code>p</code> the
     * following statement should return true.
     * <code>p.getMBR().isIntersected(p);</code>
     */
    @Override
    public sa.edu.uqu.geoknit.dataTypes.Rectangle getMBR() {
        return new sa.edu.uqu.geoknit.dataTypes.Rectangle(x, y, x + Math.ulp(x), y + Math.ulp(y));
    }

    @Override
    public double distanceTo(double px, double py) {
        double dx = x - px;
        double dy = y - py;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public sa.edu.uqu.geoknit.dataTypes.Shape getIntersection(sa.edu.uqu.geoknit.dataTypes.Shape s) {
        return getMBR().getIntersection(s);
    }

    @Override
    public boolean isIntersected(Shape s) {
        return getMBR().isIntersected(s);
    }

    @Override
    public String toString() {
        return "Point: ("+x+","+y+")";
    }

    public String toPoint() { return "["+x+","+y+"]";}

//    @Override
//    public Text toText(Text text) {
//        TextSerializerHelper.serializeDouble(x, text, ',');
//        TextSerializerHelper.serializeDouble(y, text, '\0');
//        return text;
//    }

    public void fromText(String point) {
        double[] pointD = pointToDouble(point);

        x = pointD[0];
        y = pointD[1];
    }
    private double[] pointToDouble(String point){
        String[] pointArray = point.replace('[',' ').replace(']',' ').split(",");
        double[] pointDouble = Arrays.stream(pointArray).mapToDouble(Double::parseDouble).toArray();
        return pointDouble;
    }

    @Override
    public int compareTo(Point o) {
        if (x < o.x)
            return -1;
        if (x > o.x)
            return 1;
        if (y < o.y)
            return -1;
        if (y > o.y)
            return 1;
        return 0;
    }

    @Override
    public void draw(Graphics g, Rectangle fileMBR, int imageWidth,
                     int imageHeight, double scale) {
        int imageX = (int) Math.round((this.x - fileMBR.x1) * imageWidth / fileMBR.getWidth());
        int imageY = (int) Math.round((this.y - fileMBR.y1) * imageHeight / fileMBR.getHeight());
        g.fillRect(imageX, imageY, 1, 1);
    }

    @Override
    public void draw(Graphics g, double xscale, double yscale) {
        int imgx = (int) Math.round(x * xscale);
        int imgy = (int) Math.round(y * yscale);
        g.fillRect(imgx, imgy, 1, 1);


    }

}

