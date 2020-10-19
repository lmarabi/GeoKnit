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
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import sa.edu.uqu.geoknit.dataTypes.*;


/**
 * A class that represents a polygon using a list of points.
 * @author eldawy
 *
 */
public class Polygon extends java.awt.Polygon implements sa.edu.uqu.geoknit.dataTypes.Shape {

    private static final long serialVersionUID = -117491486038680078L;

    public double[] xpointsD;
    public double[] ypointsD;
    public Point[] points;

    public Polygon() {
        super();
    }

    public Polygon(int[] xpoints, int[] ypoints, int npoints) {
        super(xpoints, ypoints, npoints);
    }

    public Polygon(String []point , int npoint){
        fromText(point);
        this.npoints = npoint;
    }
    public Polygon(Point[] points ,int npoint){
        String [] stringArray = new String[npoint];
        for (int i = 0; i < npoint; i++) {
            stringArray[i] = points[i].toPoint();
        }
        new Polygon(stringArray , npoint);
    }
    /**
     * Set the points in the rectangle to the given array
     * @param xpoints
     * @param ypoints
     * @param npoints
     */
    public void set(int[] xpoints, int[] ypoints, int npoints) {
        this.npoints = npoints;
        this.xpoints = new int[npoints];
        this.ypoints = new int[npoints];
        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
    }

        public void fromText(String[] polygon) {
            this.npoints = polygon.length;
            this.points= new Point[this.npoints];
            this.xpointsD =  new double[this.npoints];
            this.ypointsD=  new double[this.npoints];
            this.xpoints = new int[this.npoints];
            this.ypoints = new int[this.npoints];
            for (int i = 0; i < polygon.length; i++) {
                Point p = new Point(polygon[i]);
                this.points[i] = p;
                this.xpointsD[i] =  p.x;
                this.ypointsD[i] =  p.y;
                this.xpoints[i] = (int) p.x;
                this.ypoints[i] = (int) p.y;



            }
        }

    @Override
    public String toString() {
        return "Polygon{" +
                "xpointsD=" + Arrays.toString(xpointsD) +
                ", ypointsD=" + Arrays.toString(ypointsD) +
                ", points=" + Arrays.toString(points) +
                ", npoints=" + npoints +
                ", xpoints=" + Arrays.toString(xpoints) +
                ", ypoints=" + Arrays.toString(ypoints) +
                ", bounds=" + bounds +
                '}';
    }
//
//    @Override
//    public void write(DataOutput out) throws IOException {
//        out.writeInt(npoints);
//        for (int i = 0; i < npoints; i++) {
//            out.writeInt(xpoints[i]);
//            out.writeInt(ypoints[i]);
//        }
//    }

//    @Override
//    public void readFields(DataInput in) throws IOException {
//        this.npoints = in.readInt();
//        this.xpoints = new int[npoints];
//        this.ypoints = new int[npoints];
//
//        for (int i = 0; i < npoints; i++) {
//            this.xpoints[i] = in.readInt();
//            this.ypoints[i] = in.readInt();
//        }
//    }

//    @Override
//    public Text toText(Text text) {
//        TextSerializerHelper.serializeInt(npoints, text, ',');
//        for (int i = 0; i < npoints; i++) {
//            TextSerializerHelper.serializeInt(xpoints[i], text, ',');
//            TextSerializerHelper.serializeInt(ypoints[i], text,
//                    i == npoints - 1 ? '\0' : ',');
//        }
//        return text;
//    }

//    @Override
//    public void fromText(Text text) {
//        this.npoints = TextSerializerHelper.consumeInt(text, ',');
//        this.xpoints = new int[npoints];
//        this.ypoints = new int[npoints];
//
//        for (int i = 0; i < npoints; i++) {
//            this.xpoints[i] = TextSerializerHelper.consumeInt(text, ',');
//            this.ypoints[i] = TextSerializerHelper.consumeInt(text,
//                    i == npoints - 1 ? '\0' : ',');
//        }
//    }

    @Override
    public Rectangle getMBR() {
        Rectangle2D mbr = super.getBounds2D();
        return new Rectangle(mbr.getMinX(), mbr.getMinY(),
                mbr.getMaxX(), mbr.getMaxY());
    }

    @Override
    public double distanceTo(double x, double y) {
        double dx = x - getBounds2D().getCenterX();
        double dy = y - getBounds2D().getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    public double distanceTo(Polygon p) {
       return distanceTo(p.getBounds2D().getCenterX() , p.getBounds2D().getCenterY());
    }

    @Override
    public boolean isIntersected(Shape s) {
        Rectangle2D mbr = super.getBounds2D();
        return super.intersects(mbr.getMinX(), mbr.getMinY(),
                mbr.getWidth(), mbr.getHeight());
    }

    public Polygon clone() {
        return new Polygon(xpoints, ypoints, npoints);
    }

    @Override
    public void draw(Graphics g, Rectangle fileMBR, int imageWidth,
                     int imageHeight, double scale) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void draw(Graphics g, double xscale, double yscale) {
        throw new RuntimeException("Not implemented yet");
    }
}
