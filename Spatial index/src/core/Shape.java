/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author DELL
 */
public interface Shape extends Cloneable{
  /**
   * Returns minimum bounding rectangle for this shape.
   * @return The minimum bounding rectangle for this shape
   */
  public Rectangle getMBR();
  
  /**
   * Gets the distance of this shape to the given point.
   * @param x The x-coordinate of the point to compute the distance to
   * @param y The y-coordinate of the point to compute the distance to
   * @return The Euclidean distance between this object and the given point
   */
  public double distanceTo(double x, double y);
  
  /**
   * Returns true if this shape is intersected with the given shape
   * @param s The other shape to test for intersection with this shape
   * @return <code>true</code> if this shape intersects with s; <code>false</code> otherwise.
   */
  public boolean isIntersected(final Shape s);
  
  /**
   * Returns a clone of this shape
   * @return A new object which is a copy of this shape
   */
  public Shape clone();
    
}
