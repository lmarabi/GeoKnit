/***********************************************************************
* Copyright (c) 2015 by Regents of the University of Minnesota.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0 which 
* accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*
*************************************************************************/
package indexing;

import core.CellInfo;
import core.Point;
import core.Rectangle;
import core.ResultCollector;
import core.Shape;
import java.io.*;


import org.apache.hadoop.conf.Configuration;

/**
 * A partitioner that uses an existing RTree as a black-box.
 * @see RTreeGuttman
 * @see RTreeGuttman#initializeFromPoints(double[], double[])
 * @author Ahmed Eldawy
 *
 */
public abstract class AbstractRTreeBBPartitioner {
  
  /**Arrays holding the coordinates*/
  protected double[] x1s, y1s, x2s, y2s;

  /**The ratio m/M used to construct the R-tree*/
  protected float mMRatio;

  public void setup(Configuration conf) {
    mMRatio = conf.getFloat("mMRatio", 0.3f);
  }

  
  public void construct(Rectangle mbr, Point[] points, int capacity) {
    double[] xs = new double[points.length];
    double[] ys = new double[points.length];
    for (int i = 0; i < points.length; i++) {
      xs[i] = points[i].x;
      ys[i] = points[i].y;
    }
    int M = capacity;
    int m = (int) Math.ceil(M * mMRatio);
    RTreeGuttman rtree = createRTree(m, M);
    rtree.initializeFromPoints(xs, ys);
    int numLeaves = rtree.getNumLeaves();
    x1s = new double[numLeaves];
    x2s = new double[numLeaves];
    y1s = new double[numLeaves];
    y2s = new double[numLeaves];
    int iLeaf = 0;
    for (RTreeGuttman.Node node : rtree.getAllLeaves()) {
      x1s[iLeaf] = node.x1;
      y1s[iLeaf] = node.y1;
      x2s[iLeaf] = node.x2;
      y2s[iLeaf] = node.y2;
      iLeaf++;
    }
  }

   /**Create the RTree that will be used to index the sample points*/
  public abstract RTreeGuttman createRTree(int m, int M);
  
  public static class RTreeGuttmanBBPartitioner extends AbstractRTreeBBPartitioner {
    enum SplitMethod {LinearSplit, QuadraticSplit};
    SplitMethod splitMethod;

    @Override
    public void setup(Configuration conf) {
      super.setup(conf);
      if (conf.get("split", "linear").equalsIgnoreCase("linear")) {
        this.splitMethod = SplitMethod.LinearSplit;
      } else {
        this.splitMethod = SplitMethod.QuadraticSplit;
      }
    }

    public RTreeGuttman createRTree(int m, int M) {
      switch (splitMethod) {
        case LinearSplit: return new RTreeGuttman(m, M);

        default: return new RTreeGuttman(m, M);
      }

    }
  }
    
  /**
   * Tests if a partition overlaps a given rectangle
   * @param partitionID
   * @param mbr
   * @return
   */
  protected boolean Partition_overlap(int partitionID, Rectangle mbr) {
    return !(mbr.x2 <= x1s[partitionID] || x2s[partitionID] < mbr.x1 ||
      mbr.y2 <= y1s[partitionID] || y2s[partitionID] < mbr.y1);
  }

  /**
   * Computes the area of a partition.
   * @param partitionID
   * @return
   */
  protected double Partition_area(int partitionID) {
    double px1 = x1s[partitionID];
    double px2 = x2s[partitionID];
    double py1 = y1s[partitionID];
    double py2 = y2s[partitionID];
    return (px2 - px1) * (py2 - py1);
  }

  /**
   * Computes the expansion that will happen on an a partition when it is
   * enlarged to enclose a given rectangle.
   * @param partitionID
   * @param mbr the MBR of the object to be added to the partition
   * @return
   */
  protected double Partition_expansion(int partitionID, Rectangle mbr) {
    // If the given rectangle is completely enclosed in the enalrged MBR of the
    // given partition, return 0
    if (mbr.x1 >= x1s[partitionID] && mbr.x2 <= x2s[partitionID] &&
        mbr.y1 >= y1s[partitionID] && mbr.y2 <= y2s[partitionID])
      return 0;
    // Retrieve partition MBR before expansion
    double px1 = x1s[partitionID];
    double px2 = x2s[partitionID];
    double py1 = y1s[partitionID];
    double py2 = y2s[partitionID];
    double areaBefore = (px2 - px1) * (py2 - py1);
    // Expand the partition MBR to include the given MBR
    px1 = Math.min(px1, mbr.x1);
    py1 = Math.min(py1, mbr.y1);
    px2 = Math.max(px2, mbr.x2);
    py2 = Math.max(py2, mbr.y2);
    return (px2-px1) * (py2-py1) - areaBefore;
  }

  public int getPartitionCount() {
    return x1s == null? 0 : x1s.length;
  }

  public void overlapPartitions(Shape shape, ResultCollector<Integer> matcher) {
    Rectangle shapeMBR = shape.getMBR();
    for (int i = 0; i < x1s.length; i++) {
      if (Partition_overlap(i, shapeMBR))
        matcher.collect(i);
    }
  }
  
  public int overlapPartition(Shape shape) {
    // ChooseLeaf. Select a leaf node in which to place a new entry E
    // Select a node N whose rectangle needs least enlargement to include E
    // Resolve ties by choosing the entry with the rectangle of smallest area
    Rectangle shapeMBR = shape.getMBR();
    double minExpansion = Double.POSITIVE_INFINITY;
    int chosenPartition = -1;
    for (int iPartition = 0; iPartition < x1s.length; iPartition++) {
      double expansion = Partition_expansion(iPartition, shapeMBR);
      if (expansion < minExpansion) {
        minExpansion = expansion;
        chosenPartition = iPartition;
      } else if (expansion == minExpansion) {
        // Resolve ties by choosing the entry with the rectangle of smallest area
        if (Partition_area(iPartition) < Partition_area(chosenPartition))
          chosenPartition = iPartition;
      }
    }

    return chosenPartition;
  }
  
  public CellInfo getPartitionAt(int index) {
    return getPartition(index);
  }

  public CellInfo getPartition(int id) {
    return new CellInfo(id, x1s[id], y1s[id], x2s[id], y2s[id]);
  }
}
