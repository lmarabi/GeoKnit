/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;


import java.awt.Graphics;
import java.awt.geom.Arc2D;

/**
 * A class that represents a circle
 * @author eldawy
 *
 */
public class Circle extends Arc2D.Double implements Shape {

  /**
   * 
   */
  private static final long serialVersionUID = 9221371239739890276L;

  /**
   * Default constructor to allow construction then deserialization
   */
  public Circle() {
  }
  
  /**
   * Initializes a circle with zero radius
   * @param x
   * @param y
   */
  public Circle(double x, double y) {
    this(x, y, 0);
  }
  
  /**
   * Initializes a circle with a specific center and radius
   * @param x
   * @param y
   * @param r
   */
  public Circle(double x, double y, double r) {
    set(x, y, r);
  }
  
  /**
   * Updates the circle to the given center and radius
   * @param x
   * @param y
   * @param r
   */
  public void set(double x, double y, double r) {
    super.setArc(x - r, y - r, r + r, r + r, 0, 360, Arc2D.CHORD);
  }

 
  private double getRadius() {
    return getWidth() / 2.0;
  }
  
  @Override
  public String toString() {
    return "Circle: @("+getCenterX()+","+getCenterY()+")- radius "+getRadius();
  }

  @Override
  public Rectangle getMBR() {
    return new Rectangle(getMinX(), getMinY(),
        getMaxX(), getMaxY());
  }

  @Override
  public double distanceTo(double x, double y) {
    double dx = getCenterX() - x;
    double dy = getCenterY() - y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  @Override
  public boolean isIntersected(Shape s) {
    Rectangle mbr = s.getMBR();
    return super.intersects(mbr.x1, mbr.y1, mbr.x2, mbr.y2);
  }
  
  public Circle clone() {
    return new Circle(getCenterX(), getCenterY(), getWidth() / 2);
  }
  
  public void draw(Graphics g, Rectangle fileMBR, int imageWidth,
      int imageHeight, double scale) {
    throw new RuntimeException("Not implemented yet");
  }
  
  public void draw(Graphics g, double xscale, double yscale) {
    throw new RuntimeException("Not implemented yet");
  }
}
