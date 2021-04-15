package indexing;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import core.BitArray;
import core.IntArray;
import core.Rectangle;



/**
 * A partial implementation for the original Antonin Guttman R-tree as described
 * in the following paper.
 * Antonin Guttman: R-Trees: A Dynamic Index Structure for Spatial Searching.
 * SIGMOD Conference 1984: 47-57
 *
 * It only contain the implementation of the parts needed for the indexing
 * methods. For example, the delete operation was not implemented as it is
 * not needed. Also, this index is designed mainly to be used to index a sample
 * in memory and use it for the partitioning. So, the disk-based mapping and
 * search were not implemented for simplicity.
 */
public class RTreeGuttman implements Closeable {
  /** Maximum capacity of a node */
  protected final int maxCapcity;

  /** Minimum capacity of a node. */
  protected final int minCapacity;

  /** The four coordinates (left, bottom, right, top) for objects (entries + nodes) */
  protected double[] x1s, y1s, x2s, y2s;

  /**A bit vector that stores which nodes are leaves*/
  protected BitArray isLeaf;

  /**A list of int[] that stores the children of each node*/
  protected List<IntArray> children;

  /**Total number of data entries*/
  protected int numEntries;

  /**Total number of nodes*/
  protected int numNodes;

  /**The index of the root in the list of nodes*/
  protected int root;

  /**Only when processing an on-disk tree. Stores the offset of each data entry in the file*/
  protected int[] entryOffsets;

  /**A deserializer that reads objects stored on disk*/
  private Deserializer<?> deser;

  /**The input stream that points to the underlying file*/

  /**When reading the tree from disk. The offset of the beginning of the tree*/
  private long treeStartOffset;

  /**The total size of the data chunk*/
  private int totalDataSize;

  /**
   * Make a room in the data structures to accommodate a new object whether
   * it is a node or a data entry.
   */
  protected void makeRoomForOneMoreObject() {
    if (x1s.length <= numEntries + numNodes) {
      // Expand the coordinate arrays in big chunks to avoid memory copy
      double[] newCoords = new double[x1s.length * 2];
      System.arraycopy(x1s, 0, newCoords, 0, x1s.length);
      x1s = newCoords;
      newCoords = new double[x2s.length * 2];
      System.arraycopy(x2s, 0, newCoords, 0, x2s.length);
      x2s = newCoords;
      newCoords = new double[y1s.length * 2];
      System.arraycopy(y1s, 0, newCoords, 0, y1s.length);
      y1s = newCoords;
      newCoords = new double[y2s.length * 2];
      System.arraycopy(y2s, 0, newCoords, 0, y2s.length);
      y2s = newCoords;

      this.isLeaf.resize(x1s.length);
    }
  }

  /**
   * Creates a new node that contains the given object and returns the ID
   * of that node.
   * @param leaf set to true to create a leaf node
   * @param iChildren the indexes of all children in this node
   * @return
   */
  protected int Node_createNodeWithChildren(boolean leaf, int ... iChildren) {
    makeRoomForOneMoreObject();
    int iNewNode = numEntries + numNodes;
    this.isLeaf.set(iNewNode, leaf);
    this.children.add(iNewNode, new IntArray());
    this.numNodes++;
    Node_reset(iNewNode, iChildren);
    return iNewNode;
  }

  /**
   * Reset a node to contain a new set of children wiping away the current
   * children.
   * @param iNode
   * @param newChildren
   */
  protected void Node_reset(int iNode, int ... newChildren) {
    children.get(iNode).clear();
    children.get(iNode).append(newChildren, 0, newChildren.length);
    Node_recalculateMBR(iNode);
  }

  protected void Node_recalculateMBR(int iNode) {
    x1s[iNode] = y1s[iNode] = Double.POSITIVE_INFINITY;
    x2s[iNode] = y2s[iNode] = Double.NEGATIVE_INFINITY;
    for (int iChild : children.get(iNode)) {
      if (x1s[iChild] < x1s[iNode])
        x1s[iNode] = x1s[iChild];
      if (y1s[iChild] < y1s[iNode])
        y1s[iNode] = y1s[iChild];
      if (x2s[iChild] > x2s[iNode])
        x2s[iNode] = x2s[iChild];
      if (y2s[iChild] > y2s[iNode])
        y2s[iNode] = y2s[iChild];
    }
  }

  /**
   * Returns the number of children for the given node.
   * @param iNode
   * @return
   */
  protected int Node_size(int iNode) {
    return children.get(iNode).size();
  }

  /**
   * Calculates the area of the node
   * @param iNode the ID of the node
   * @return
   */
  protected double Node_area(int iNode) {
    return (x2s[iNode] - x1s[iNode]) * (y2s[iNode] - y1s[iNode]);
  }

  /**
   * Calculates the volume (area) expansion that will happen if the given object
   * is added to a given node.
   * @param iNode the ID of the node that would be expanded
   * @param iNewChild the ID of the object that would be added to the node
   * @return
   */
  protected double Node_volumeExpansion(int iNode, int iNewChild) {
    double widthB4Expansion = x2s[iNode] - x1s[iNode];
    double heightB4Expansion = y2s[iNode] - y1s[iNode];
    double widthAfterExpansion = Math.max(x2s[iNode], x2s[iNewChild]) -
        Math.min(x1s[iNode], x1s[iNewChild]);
    double heightAfterExpansion = Math.max(y2s[iNode], y2s[iNewChild]) -
        Math.min(y1s[iNode], y1s[iNewChild]);
    
    return widthAfterExpansion * heightAfterExpansion -
        widthB4Expansion * heightB4Expansion;
  }

  /**
   * Adds a new child to an existing node.
   * @param iNode
   * @param iNewChild
   */
  protected void Node_addChild(int iNode, int iNewChild) {
    this.children.get(iNode).add(iNewChild);
  }

  /**
   * Expand the MBR of the given node to enclose the given new object
   * @param node
   * @param newObject
   */
  protected void Node_expand(int node, int newObject) {
    // Expand the MBR to enclose the new child
    x1s[node] = Math.min(x1s[node], x1s[newObject]);
    y1s[node] = Math.min(y1s[node], y1s[newObject]);
    x2s[node] = Math.max(x2s[node], x2s[newObject]);
    y2s[node] = Math.max(y2s[node], y2s[newObject]);
  }

  /**
   * Split an existing node around the given separator. Current children from
   * indexes 1 to separator-1 (inclusive) remain in the given node. All remaining
   * children go to a new node. The ID of the new node created that contain the
   * children from separator to end.
   * @param iNode the index of the node to split
   * @param separator the index of the first child to be in the new node
   * @return the ID of the new node created after split
   */
  protected int Node_split(int iNode, int separator) {
    // Create the new node that will hold the entries from separator -> size
    makeRoomForOneMoreObject();
    int iNewNode = numNodes + numEntries;
    this.numNodes++;
    // Make room for the children of the new node
    this.children.add(iNewNode, new IntArray());
    // The new node in the same level so it follow the leaf/non-leaf status of the current node
    isLeaf.set(iNewNode, isLeaf.get(iNode));

    // Split the children around the separator
    children.get(iNewNode).append(children.get(iNode), separator,
        children.get(iNode).size() - separator);
    children.get(iNode).resize(separator);

    // Recalculate the MBRs of the two nodes
    Node_recalculateMBR(iNode);
    Node_recalculateMBR(iNewNode);

    return iNewNode;
  }

  /**
   * Initialize the current R-tree from given data entries
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   */
  public void initializeFromRects(double[] x1, double[] y1, double[] x2, double[] y2) {
    this.initializeDataEntries(x1, y1, x2, y2);
    this.insertAllDataEntries();
  }

  /**
   * Initialize the tree from a set of points
   * @param xs
   * @param ys
   */
  public void initializeFromPoints(double[] xs, double[] ys) {
    this.initializeDataEntries(xs, ys);
    this.insertAllDataEntries();
  }


  /**
   * Construct a new empty R-tree with the given parameters.
   * @param minCapacity - Minimum capacity of a node
   * @param maxCapcity - Maximum capacity of a node
   */
  public RTreeGuttman(int minCapacity, int maxCapcity) {
    if (minCapacity > maxCapcity / 2)
      throw new RuntimeException(String.format("Invalid minCapacity=%d and maxCapacity=%d. minCapacity should be at most maxCapacity/2", minCapacity, maxCapcity));
    if (minCapacity == 0)
      throw new RuntimeException("minCapacity cannot be equal to zero");
    this.minCapacity = minCapacity;
    this.maxCapcity = maxCapcity;
  }

  protected void insertAllDataEntries() {
    root = Node_createNodeWithChildren(true, 0);
    // Insert one by one
    for (int i = 1; i < numEntries; i++)
      insertAnExistingDataEntry(i);
  }

  /**
   * Initialize the data entries to a set of point coordinates without actually
   * inserting them into the tree structure.
   * @param xs
   * @param ys
   */
  protected void initializeDataEntries(double[] xs, double[] ys) {
    this.numEntries = xs.length;
    this.numNodes = 0; // Initially, no nodes are there
    this.isLeaf = new BitArray(numEntries);
    children = new ArrayList<IntArray>(numEntries);
    this.x1s = new double[numEntries];
    this.y1s = new double[numEntries];
    this.x2s = new double[numEntries];
    this.y2s = new double[numEntries];
    for (int i = 0; i < numEntries; i++) {
      this.x1s[i] = xs[i];
      this.y1s[i] = ys[i];
      this.x2s[i] = xs[i];
      this.y2s[i] = ys[i];
      children.add(null); // data entries do not have children
    }
  }

  /**
   * Initialize the data entries to a set of rectangular coordinates without
   * actually inserting them into the tree structure.
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   */
  protected void initializeDataEntries(double[] x1, double[] y1, double[] x2, double[] y2) {
    this.numEntries = x1.length;
    this.numNodes = 0; // Initially, no nodes are there
    this.isLeaf = new BitArray(numEntries);
    children = new ArrayList<IntArray>(numEntries);
    this.x1s = new double[numEntries];
    this.y1s = new double[numEntries];
    this.x2s = new double[numEntries];
    this.y2s = new double[numEntries];
    for (int i = 0; i < numEntries; i++) {
      this.x1s[i] = x1[i];
      this.y1s[i] = y1[i];
      this.x2s[i] = x2[i];
      this.y2s[i] = y2[i];
      children.add(null); // data entries do not have children
    }
  }


  /**
   * Inserts the given data entry into the tree. We assume that the coordinates
   * of this data entry are already stored in the coordinates arrays.
   * @param iEntry - The index of the point in the array of points
   */
  protected void insertAnExistingDataEntry(int iEntry) {
    // The path from the root to the newly inserted record. Used for splitting.
    IntArray path = new IntArray();
    int iCurrentVisitedNode = root;
    path.add(iCurrentVisitedNode);
    // Descend in the tree until we find a leaf node to add the object to
    while (!isLeaf.get(iCurrentVisitedNode)) {
      // Node is not leaf. Choose a child node
      // Descend to the best child found
      int iBestChild = chooseSubtree(iEntry, iCurrentVisitedNode);
      iCurrentVisitedNode = iBestChild;
      path.add(iCurrentVisitedNode);
    }

    // Now we have a child node. Insert the current element to it and split
    // if necessary
    Node_addChild(iCurrentVisitedNode, iEntry);
    adjustTree(iCurrentVisitedNode, path);
  }

  /**
   * Choose the best subtree to add a data entry to.
   * According to the original R-tree paper, this function chooses the node with
   * the minimum volume expansion, then the one with the smallest volume,
   * then the one with the least number of records, then randomly to any one.
   * @param iEntry
   * @param iNode
   * @return
   */
  protected int chooseSubtree(int iEntry, int iNode) {
    // 1. Choose the child with the minimum expansion
    double minExpansion = Double.POSITIVE_INFINITY;
    int iBestChild = 0;
    for (int iCandidateChild : children.get(iNode)) {
      double expansion = Node_volumeExpansion(iCandidateChild, iEntry);
      if (expansion < minExpansion) {
        minExpansion = expansion;
        iBestChild = iCandidateChild;
      } else if (expansion == minExpansion) {
        // Resolve ties by choosing the entry with the rectangle of smallest area
        if (Node_area(iCandidateChild) < Node_area(iBestChild))
          iBestChild = iCandidateChild;
      }
    }
    return iBestChild;
  }

  /**
   * Adjust the tree after an insertion by making the necessary splits up to
   * the root.
   * @param leafNode the index of the leaf node where the insertion happened
   * @param path
   */
  protected void adjustTree(int leafNode, IntArray path) {
    int iNode;
    int newNode = -1;
    if (Node_size(leafNode) > maxCapcity) {
      // Node full. Split into two
      newNode = split(leafNode, minCapacity);
    }
    // AdjustTree. Ascend from the leaf node L
    while (!path.isEmpty()) {
      iNode = path.pop();
      // Adjust covering rectangle in the node
      Node_expand(iNode, children.get(iNode).peek());
      if (path.isEmpty()) {
        // The node is the root (no parent)
        if (newNode != -1) {
          // If the root is split, create a new root
          root = Node_createNodeWithChildren(false, iNode, newNode);
        }
        // If N is the root with no partner NN, stop.
      } else {
        int parent = path.peek();
        if (newNode != -1) {
          // If N has a partner NN resulting from an earlier split,
          // create a new entry ENN and add to the parent if there is room.
          // Add Enn to P if there is room
          Node_addChild(parent, newNode);
          newNode = -1;
          if (Node_size(parent) >= maxCapcity) {
            newNode = split(parent, minCapacity);
          }
        }
      }
    }
  }


  /**
   * Linear splitting algorithm as described on Page 52 of the paper
   * @param iNode
   * @param minSplitSize
   * @return
   */
  protected int split(int iNode, int minSplitSize) {
    IntArray nodeChildren = children.get(iNode);
    int highestLowSideX = nodeChildren.get(0);
    int lowestHighSideX = nodeChildren.get(0);
    int highestLowSideY = nodeChildren.get(0);
    int lowestHighSideY = nodeChildren.get(0);
    for (int iChild = 1; iChild < nodeChildren.size(); iChild++) {
      int child = nodeChildren.get(iChild);
      if (x1s[child] > x1s[highestLowSideX])
        highestLowSideX = child;
      if (y1s[child] > y1s[highestLowSideY])
        highestLowSideY = child;
      if (x2s[child] < x2s[lowestHighSideX])
        lowestHighSideX = child;
      if (y2s[child] < y2s[lowestHighSideY])
        lowestHighSideY = child;
    }
    double separationX = (x1s[highestLowSideX] - x2s[lowestHighSideX]) / (x2s[root] - x1s[root]);
    double separationY = (y1s[highestLowSideY] - y2s[lowestHighSideY]) / (y2s[root] - y1s[root]);
    int seed1, seed2;
    if (separationX > separationY) {
      seed1 = highestLowSideX;
      seed2 = lowestHighSideX;
    } else {
      seed1 = highestLowSideY;
      seed2 = lowestHighSideY;
    }

    // After picking the seeds, we will start picking next elements one-by-one
    IntArray nonAssignedNodes = nodeChildren.clone();
    Node_reset(iNode, seed1);
    int iNewNode = Node_createNodeWithChildren(isLeaf.get(iNode), seed2);
    nonAssignedNodes.remove(seed1);
    nonAssignedNodes.remove(seed2);
    int group1 = iNode;
    int group2 = iNewNode;
    for (int child : nonAssignedNodes) {
      // If one group has so few entries that all the rest must be assigned to it
      // in order to have the minimum number minSplitSize, assign them and stop
      if (nonAssignedNodes.size() + Node_size(group1) <= minSplitSize) {
        // Assign all the rest to group1
        for (int iObject : nonAssignedNodes) {
          Node_addChild(group1, iObject);
          Node_expand(group1, iObject);
        }
        break;
      } else if (nonAssignedNodes.size() + Node_size(group2) <= minSplitSize) {
        // Assign all the rest to group2
        for (int iObject : nonAssignedNodes) {
          Node_addChild(group2, iObject);
          Node_expand(group2, iObject);
        }
        break;
      } else {
        double d1 = Node_volumeExpansion(group1, child);
        double d2 = Node_volumeExpansion(group2, child);
        if (d1 == d2) {
          // Resolve ties by adding the entry to theh gorup with samller area
          d1 = Node_area(group1);
          d2 = Node_area(group2);
          if (d1 == d2) {
            // then to the one wih fewer entries
            d1 = Node_size(group1);
            d2 = Node_size(group2);
            if (d1 == d2) {
              // ... then to either
              d1 = 0.5;
              d2 = Math.random();
            }
          }
        }
        if (d1 < d2) {
          Node_addChild(group1, child);
          Node_expand(group1, child);
        } else if (d1 > d2) {
          Node_addChild(group2, child);
          Node_expand(group2, child);
        }
      }
    }
    return  iNewNode;
  }

  /**
   * Search for all the entries that overlap a given query rectangle
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @param results the results as a list of entry IDs as given in the construction
   *                function
   */
  public void search(double x1, double y1, double x2, double y2, IntArray results) {
    results.clear();
    IntArray nodesToSearch = new IntArray();
    nodesToSearch.add(root);
    while (!nodesToSearch.isEmpty()) {
      int nodeToSearch = nodesToSearch.pop();
      if (isLeaf.get(nodeToSearch)) {
        // Search and return all the entries in the leaf node
        for (int iEntry : children.get(nodeToSearch)) {
          if (Object_overlaps(iEntry, x1, y1, x2, y2))
            results.add(iEntry);
        }
      } else {
        // A non-leaf node, expand the search to all overlapping children
        for (int iChild : children.get(nodeToSearch)) {
          if (Object_overlaps(iChild, x1, y1, x2, y2))
            nodesToSearch.add(iChild);
        }
      }
    }
  }

  public Iterable<Entry> search(double x1, double y1, double x2, double y2) {
    return new SearchIterator(x1, y1, x2, y2);
  }

  /**
   * Tests if an object (entry or node) overlaps with a rectangle
   * @param iEntry
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return
   */
  protected boolean Object_overlaps(int iEntry, double x1, double y1, double x2, double y2) {
    return !(x2 < x1s[iEntry] || x2s[iEntry] < x1 ||
             y2 < y1s[iEntry] || y2s[iEntry] < y1);
  }

  /**
   * Total number of objects in the tree.
   * @return
   */
  public int numOfDataEntries() {
    return numEntries;
  }

  /**
   * Returns number of nodes in the tree.
   * @return
   */
  public int numOfNodes() {
    return numNodes;
  }

  /**
   * Computes the height of the tree which is defined as the number of edges
   * on the path from the root to the deepest node. Sine the R-tree is perfectly
   * balanced, it is enough to measure the length of the path from the root to
   * any node, e.g., the left-most node.
   * @return
   */
  public int getHeight() {
    if (numNodes == 0)
      return 0;
    // Compute the height of the tree by traversing any path from the root
    // to the leaf.
    // Since the tree is balanced, any path would work
    int height = 0;
    int iNode = root;
    while (!isLeaf.get(iNode)) {
      height++;
      iNode = children.get(iNode).get(0);
    }
    return height;
  }

  /**
   * The total number of leaf nodes.
   * @return
   */
  public int getNumLeaves() {
    return (int) isLeaf.countOnes();
  }

  /**
   * Retrieve all the leaf nodes in the tree.
   * @return
   */
  public Iterable<Node> getAllLeaves() {
    return new LeafNodeIterable();
  }

  /**
   * Creates an R-tree that contains only nodes (no data entries). The given
   * coordinates are used for the leaf nodes. This tree is used to model an R-tree
   * and use the different R-tree algorithms to test where an entry would end up
   * in the R-tree (without really inserting it).
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @see #noInsert(double, double, double, double)
   */
  protected void initializeHollowRTree(double[] x1, double[] y1, double[] x2, double[] y2) {
    // Create a regular R-tree with the given rectangles as data entries.
    initializeFromRects(x1, y1, x2, y2);

    // Make sure we have a room for an extra object which will be used in noInsert
    makeRoomForOneMoreObject();
  }

  /**
   * Simulates an insertion of a record and returns the ID of the object that either
   * contains the given boundaries or will be its sibling.
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return
   */
  protected int noInsert(double x1, double y1, double x2, double y2) {
    int i = numEntries + numNodes;
    x1s[i] = x1;
    y1s[i] = y1;
    x2s[i] = x2;
    y2s[i] = y2;

    // Descend from the root until reaching a data entry
    // The range of IDs for data entries is [0, numEntries[
    // All node IDs is in the rnage [numEntries, numEntries + numNodes[
    int p = root;
    while (p >= numOfDataEntries())
      p = chooseSubtree(i, p);

    // Return the index of the leaf node without really inserting the element
    return p;
  }

  /**
   * Only when the tree is read from file, return the total size of the data part
   * in bytes.
   * @return
   */
  public int getTotalDataSize() {
    return totalDataSize;
  }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

  /**
   * A class used to iterate over the data entries in the R-tree
   */
  public class Entry {
    public int id;
    public double x1, y1, x2, y2;

    protected Entry() {}

    @Override
    public String toString() {
      return String.format("Entry #%d (%f, %f, %f, %f)", id, x1, y1, x2, y2);
    }
  }

  /**
   * An iterable and iterator that traverses all data entries in the tree.
   */
  protected class EntryIterator implements Iterable<Entry>, Iterator<Entry> {
    private int iNextEntry = 0;
    private final Entry entry = new Entry();

    protected EntryIterator() {}

    @Override
    public Iterator<Entry> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      return iNextEntry < RTreeGuttman.this.numEntries;
    }

    @Override
    public Entry next() {
      entry.id = iNextEntry;
      entry.x1 = x1s[iNextEntry];
      entry.y1 = y1s[iNextEntry];
      entry.x2 = x2s[iNextEntry];
      entry.y2 = y2s[iNextEntry];
      iNextEntry++;
      return entry;
    }

    public void remove() {
      throw new RuntimeException("Not supported");
    }
  }

  /**
   * Returns an iterable on all data entries in the tree.
   * @return
   */
  public Iterable<Entry> entrySet() {
    return new EntryIterator();
  }

  /**
   * An iterator for range query search results
   */
  protected class SearchIterator implements Iterable<Entry>, Iterator<Entry> {
    /**The list of nodes yet to be searched*/
    private IntArray nodesToSearch;

    /**The ID of the entry to return on the next call*/
    private int iNextEntry;

    /**The object used to return all search results*/
    private Entry entry;

    /**The search range*/
    private double x1, y1, x2, y2;

    protected SearchIterator(double x1, double y1, double x2, double y2) {
      this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
      searchFirst();
    }

    /**
     * Search for the first element in the result
     */
    protected void searchFirst() {
      nodesToSearch = new IntArray();
      nodesToSearch.add(root);
      entry = new Entry();
      while (!nodesToSearch.isEmpty()) {
        // We keep the top of the stack for the subsequent next calls
        int iNodeToSearch = nodesToSearch.peek();
        if (isLeaf.get(iNodeToSearch)) {
          for (iNextEntry = 0; iNextEntry < Node_size(iNodeToSearch); iNextEntry++) {
            // Found a matching element in a leaf node
            if (Object_overlaps(children.get(iNodeToSearch).get(iNextEntry), x1, y1, x2, y2))
              return;
          }
          // No results found in that leaf node, remove it
          nodesToSearch.pop();
        } else {
          // Found a matching non-leaf node, visit its children
          nodesToSearch.pop(); // No longer needed
          for (int iChild : children.get(iNodeToSearch)) {
            if (Object_overlaps(iChild, x1, y1, x2, y2))
              nodesToSearch.add(iChild);
          }
        }
      }
      iNextEntry = -1;
    }

    protected void prefetchNext() {
      int iNodeToSearch = nodesToSearch.peek();
      while (++iNextEntry < Node_size(iNodeToSearch)) {
        if (Object_overlaps(children.get(iNodeToSearch).get(iNextEntry), x1, y1, x2, y2))
          return;
      }
      // Done with the current leaf node. Continue searching for the next leaf
      nodesToSearch.pop();
      while (!nodesToSearch.isEmpty()) {
        iNodeToSearch = nodesToSearch.peek();
        if (isLeaf.get(iNodeToSearch)) {
          for (iNextEntry = 0; iNextEntry < Node_size(iNodeToSearch); iNextEntry++) {
            // Found a matching element in a leaf node
            if (Object_overlaps(children.get(iNodeToSearch).get(iNextEntry), x1, y1, x2, y2))
              return;
          }
          // No results found in that leaf node, remove it
          nodesToSearch.pop();
        } else {
          // Found a matching non-leaf node, visit its children
          nodesToSearch.pop(); // No longer needed
          for (int iChild : children.get(iNodeToSearch)) {
            if (Object_overlaps(iChild, x1, y1, x2, y2))
              nodesToSearch.add(iChild);
          }
        }
      }
      iNextEntry = -1; // No more entries to search
    }

    @Override
    public Iterator<Entry> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      return iNextEntry != -1;
    }

    @Override
    public Entry next() {
      int iEntry = children.get(nodesToSearch.peek()).get(iNextEntry);
      entry.id = iEntry;
      entry.x1 = x1s[iEntry];
      entry.y1 = y1s[iEntry];
      entry.x2 = x2s[iEntry];
      entry.y2 = y2s[iEntry];
      prefetchNext();
      return entry;
    }

    public void remove() {
      throw new RuntimeException("Not supported");
    }
  }

  /**
   * A class that holds information about one node in the tree.
   */
  public static class Node {
    /**The internal ID of the node*/
    public int id;

    /**Whether this is a leaf node or not*/
    public boolean isLeaf;

    /**The boundaries of the node*/
    public double x1, y1, x2, y2;

    protected Node(){}

    public String toWKT() {
      return String.format("POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",
          x1, y1, x1, y2, x2, y2, x2, y1, x1, y1);
    }
  }

  protected class NodeIterable implements Iterable<Node>, Iterator<Node> {
    /**The ID of the next node to be returned*/
    protected int iNextNode;

    /**Current node pointed by the iterator*/
    protected Node currentNode;

    protected NodeIterable() {
      currentNode = new Node();
      iNextNode = numEntries - 1;
      prefetchNext();
    }

    protected void prefetchNext() {
      if (iNextNode >= numEntries + numNodes)
        return;
      iNextNode++;
    }

    @Override
    public Iterator<Node> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      return iNextNode < numEntries + numNodes;
    }

    @Override
    public Node next() {
      currentNode.x1 = x1s[iNextNode];
      currentNode.y1 = y1s[iNextNode];
      currentNode.x2 = x2s[iNextNode];
      currentNode.y2 = y2s[iNextNode];
      currentNode.isLeaf = isLeaf.get(iNextNode);
      currentNode.id = iNextNode;
      prefetchNext();
      return currentNode;
    }

    public void remove() {
      throw new RuntimeException("Not supported");
    }
  }

  protected class LeafNodeIterable extends NodeIterable {
    protected void prefetchNext() {
      if (iNextNode >= numEntries + numNodes)
        return;
      do {
        iNextNode++;
      } while (iNextNode < numEntries + numNodes && !isLeaf.get(iNextNode));
    }
  }

  /**
   * An interface for serializing objects given their entry number
   */
  public interface Serializer {
    int serialize(DataOutput out, int iObject) throws IOException;
  }

  public interface Deserializer<O> {
    O deserialize(DataInput in, int length) throws IOException;
  }

  /**
   * Serializes the tree and its data entries to an output stream. Notice that
   * this is not supposed to be used as an external tree format where you can insert
   * and delete entries. Rather, it is like a static copy of the tree where you
   * can search or load back in memory. The format of the tree on disk is as
   * described below.
   * <ul>
   *   <li>
   *     Data Entries: First, all data entries are written in an order that is consistent
   *   with the R-tree structure. This order will guarantee that all data entries
   *   under any node (from the root to leaves) will be adjacent in that order.
   *   </li>
   *   <li>
   *     Tree structure: This part contains the structure of the tree represented
   *   by its nodes. The nodes are stored in a level order traversal. This guarantees
   *   that the root will always be the first node and that all siblings will be
   *   stored consecutively. Each node contains the following information:
   *   (1) (n) Number of children as a 32-bit integer,
   *   (2) n Pairs of (child offset, MBR=(x1, y1, x2, y2). The child offset is
   *   the offset of the beginning of the child data (node or data entry) in the
   *   tree where 0 is the offset of the first data entry.
   *   </li>
   *   <li>
   *     Tree footer: This section contains some meta data about the tree as
   *     follows. All integers are 32-bits.
   *     (1) MBR of the root as (x1, y1, x2, y2),
   *     (2) Number of data entries,
   *     (3) Number of non-leaf nodes,
   *     (4) Number of leaf nodes,
   *     (5) Tree structure offset: offset of the beginning of the tree structure section
   *     (6) Footer offset: offset of the beginning of the footer as a 32-bit integer.
   *     (7) Tree size: Total tree size in bytes including data+structure+footer
   *   </li>
   *
   * </ul>
   * @param out
   * @throws IOException
   */
  public void write(DataOutput out, Serializer ser) throws IOException {
    // Tree data: write the data entries in the tree order
    // Since we write the data first, we will have to traverse the tree twice
    // first time to visit and write the data entries in the tree order,
    // and second time to visit and write the tree nodes in the tree order.
    Deque<Integer> nodesToVisit = new ArrayDeque<Integer>();
    nodesToVisit.add(root);
    int[] objectOffsets = new int[numOfDataEntries() + numOfNodes()];
    // Keep track of the offset of each data object from the beginning of the
    // data section
    int dataOffset = 0;
    // Keep track of the offset of each node from the beginning of the tree
    // structure section
    int nodeOffset = 0;
    while (!nodesToVisit.isEmpty()) {
      int node = nodesToVisit.removeFirst();
      // The node is supposed to be written in this order.
      // Measure its offset and accumulate its size
      objectOffsets[node] = nodeOffset;
      nodeOffset += 4 + (4 + 8 * 4) * Node_size(node);

      if (isLeaf.get(node)) {
        // Leaf node, write the data entries in order
        for (int child : children.get(node)) {
          objectOffsets[child] = dataOffset;
          if (ser != null)
            dataOffset += ser.serialize(out, child);
        }
      } else {
        // Internal node, recursively traverse its children
        for (int child : children.get(node))
          nodesToVisit.addLast(child);
      }
    }
    // Update node offsets as they are written after the data entries
    for (int i = 0; i < numNodes; i++)
      objectOffsets[i + numEntries] += dataOffset;

    // Tree structure: Write the nodes in tree order
    nodesToVisit.add(root);
    while (!nodesToVisit.isEmpty()) {
      int node = nodesToVisit.removeFirst();
      // (1) Number of children
      out.writeInt(Node_size(node));
      for (int child : children.get(node)) {
        // (2) Write the offset of the child
        out.writeInt(objectOffsets[child]);
        // (3) Write the MBR of each child
        out.writeDouble(x1s[child]);
        out.writeDouble(y1s[child]);
        out.writeDouble(x2s[child]);
        out.writeDouble(y2s[child]);
      }
      // If node is internal, add its children to the nodes to be visited
      if (!isLeaf.get(node)) {
        for (int child : children.get(node))
          nodesToVisit.addLast(child);
      }
    }

    // Tree footer
    int footerOffset = dataOffset + nodeOffset;
    // (1) MBR of the root
    out.writeDouble(x1s[root]);
    out.writeDouble(y1s[root]);
    out.writeDouble(x2s[root]);
    out.writeDouble(y2s[root]);
    // (2) Number of data entries
    out.writeInt(numOfDataEntries());
    // (3) Number of non-leaf nodes
    out.writeInt((int) (numOfNodes() - isLeaf.countOnes()));
    // (4) Number of leaf nodes
    out.writeInt((int) isLeaf.countOnes());
    // (5) Offset of the tree structure section
    out.writeInt(dataOffset);
    // (6) Offset of the footer
    out.writeInt(footerOffset);
    // (7) Size of the entire tree
    int footerSize = 4 * 8 + 6 * 4;
    out.writeInt(footerOffset + footerSize);
  }

  /**
   * Read an R-tree stored using the method {@link #write(DataOutput, Serializer)}
   * @param in
   * @param length
   * @throws IOException
   */

  /**
   * Writes all nodes of the tree in a WKT format to be visualized in QGIS.
   * @param out
   */
  public void toWKT(PrintStream out) {
    for (Node node : new NodeIterable()) {
      out.printf("%d\tPOLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))\t%s\n",
          node.id,
          node.x1, node.y1,
          node.x1, node.y2,
          node.x2, node.y2,
          node.x2, node.y1,
          node.x1, node.y1,
          Boolean.toString(node.isLeaf)
      );
    }
  }

  /**
   * Assigns an entry to a group based on the R-tree paper (Page 52, Step QS3)
   * @param mbr1
   * @param size1
   * @param mbr2
   * @param size2
   * @param x
   * @param y
   * @return
   */
  static int chooseGroup(Rectangle mbr1, int size1, Rectangle mbr2, int size2, double x, double y) {
    double w1 = mbr1.getWidth();
    double h1 = mbr1.getHeight();
    if (x < mbr1.x1) w1 += mbr1.x1 - x;
    if (x > mbr1.x2) w1 += x - mbr1.x2;
    if (y < mbr1.y1) h1 += mbr1.y1 - y;
    if (y > mbr1.y2) h1 += y - mbr1.y2;

    double w2 = mbr2.getWidth();
    double h2 = mbr2.getHeight();
    if (x < mbr2.x1) w2 += mbr2.x1 - x;
    if (x > mbr2.x2) w2 += x - mbr2.x2;
    if (y < mbr2.y1) h2 += mbr2.y1 - y;
    if (y > mbr2.y2) h2 += y - mbr2.y2;

    double d1 = w1*h1 - mbr1.area(); // Volume expansion
    double d2 = w2*h2 - mbr2.area(); // Volume expansion
    if (d1 == d2) {
      // Resolve ties by adding the entry to theh gorup with samller area
      d1 = mbr1.area();
      d2 = mbr2.area();
      if (d1 == d2) {
        // then to the one wih fewer entries
        d1 = size1;
        d2 = size2;
        if (d1 == d2) {
          // ... then to either
          d1 = 0.5;
          d2 = Math.random();
        }
      }
    }
    return d1 < d2 ? 0 : 1;
  }

  /**
   * Partitions the given set of points using the linear split algorithm to produce a set of partitions where each one
   * has between minPartitionSize and maxPartitionSize points, inclusively. The parameter fractionMinSplitSize can be
   * set to any number
   * @param xs
   * @param ys
   * @param minPartitionSize
   * @param maxPartitionSize
   * @param fractionMinSplitSize
   * @return
   */
  static Rectangle[] partitionPoints(double[] xs, double[] ys, int minPartitionSize,
                                     int maxPartitionSize, float fractionMinSplitSize) {
    class Range {
      /**The range of points to partition [start, end)*/
      int start, end;

      Range(int s, int e) {
        this.start = s;
        this.end = e;
      }
    }

    // The ranges that might need to be split
    Stack<Range> rangesToSplit = new Stack<Range>();
    rangesToSplit.push(new Range(0, xs.length));

    // The output list of partitions
    List<Rectangle> partitions = new ArrayList<Rectangle>();

    // Compute the MBR of all points to be able to normalize the separation
    Rectangle mbr = null;

    while (!rangesToSplit.empty()) {
      Range r = rangesToSplit.pop();
      if (r.end - r.start <= maxPartitionSize) {
        // No need to further split this range. Report it to the answer.
        Rectangle partition = new Rectangle(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        for (int i = r.start; i < r.end; i++)
          partition.expand(xs[i], ys[i]);
        partitions.add(partition);
        System.out.println("Generated a partition of size " + (r.end - r.start));
      } else {
        // Apply the linear-time R-tree splitting algorithm
        // First, pick the two seeds the have the largest separation
        int minX = r.start;
        int maxX = r.start;
        int minY = r.start;
        int maxY = r.start;
        for (int iPoint = r.start+1; iPoint < r.end; iPoint++) {
          if (xs[iPoint] < xs[minX])
            minX = iPoint;
          if (xs[iPoint] > xs[maxX])
            maxX = iPoint;
          if (ys[iPoint] < ys[minY])
            minY = iPoint;
          if (ys[iPoint] > ys[maxY])
            maxY = iPoint;
        }
        // Compute the MBR for the very first group to normalize separations
        if (mbr == null)
          mbr = new Rectangle(xs[minX], ys[minY], xs[maxX], ys[maxY]);
        int seed1, seed2;
        if ((xs[maxX] - xs[minX])/mbr.getWidth() > (ys[maxY] - ys[minY])/mbr.getHeight()) {
          seed1 = minX;
          seed2 = maxX;
        } else {
          seed1 = minY;
          seed2 = maxY;
        }
        // Swap seed1 with range.start and seed2 with range.end-1
        double temp;
        temp = xs[r.start]; xs[r.start] = xs[seed1]; xs[seed1] = temp;
        temp = ys[r.start]; ys[r.start] = ys[seed1]; ys[seed1] = temp;
        temp = xs[r.end-1]; xs[r.end-1] = xs[seed2]; xs[seed2] = temp;
        temp = ys[r.end-1]; ys[r.end-1] = ys[seed2]; ys[seed2] = temp;

        Rectangle mbr1 = new Rectangle(xs[r.start], ys[r.start], xs[r.start], ys[r.start]);
        Rectangle mbr2 = new Rectangle(xs[r.end-1], ys[r.end-1], xs[r.end-1], ys[r.end-1]);
        // Split the range [r.start, r.end) so that the first group is [r.start, i] and the second group is
        // [j, r.end)
        int i = r.start;
        int j = r.end - 1;
        while (i < j) {
          int group;
          // Advance i as long as the element at i belongs to the first group
          while (i < j && chooseGroup(mbr1, i - r.start, mbr2, r.end - j, xs[i], ys[i]) ==  0) {
            mbr1.expand(xs[i], ys[i]);
            i++;
          }
          // Decrease j as long as the element at j belongs to the second group
          while (i < j && chooseGroup(mbr1, i - r.start, mbr2, r.end - j, xs[j], ys[j]) ==  1) {
            mbr2.expand(xs[j], ys[j]);
            j--;
          }
          // Swap the elements at i and j and continue
          if (i < j) {
            temp = xs[i]; xs[i] = xs[j]; xs[j] = temp;
            temp = ys[i]; ys[i] = ys[j]; ys[j] = temp;
          }
          // Check if all the remaining items need to be assigned to one group to  meet the minimum size constraint
          if (r.end - i <= minPartitionSize)
            j=i;
          else if (j - r.start <= minPartitionSize)
            i = j;
        }
        // Now split around i and j (notice that i == j at this point)
        // Ensure that the two partitions are valid
      

      
        rangesToSplit.push(r);
      }
    }

    return partitions.toArray(new Rectangle[partitions.size()]);
  }
}
