/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexing;

import core.Rectangle;
import core.CellInfo;
import core.GridInfo;
import core.Point;
import core.Shape;
import core.ResultCollector;
/**
 *
 * @author DELL
 */
public class GridPartitioner {

    /**
     * Origin of the grid
     */
    protected double x, y;

    /**
     * Number of tiles
     */
    protected int numTiles;

    /**
     * Total number of columns and rows within the input range
     */
    protected int numColumns, numRows;

    /**
     * With and height of a single tile
     */
    protected double tileWidth, tileHeight;
    /**
     * A default constructor to be able to dynamically instantiate it and
     * deserialize it
     */
    public GridPartitioner() {
    }

    public GridPartitioner(Rectangle mbr, int columns, int rows) {
        this.x = mbr.x1;
        this.y = mbr.y1;
        this.numTiles = rows * columns;
        this.numColumns = columns;
        this.numRows = rows;
        this.tileWidth = mbr.getWidth() / columns;
        this.tileHeight = mbr.getHeight() / rows;
    }

    public void createFromPoints(Rectangle mbr, Point[] points, int capacity) throws IllegalArgumentException {
        if (points.length == 0) {
            throw new IllegalArgumentException("Amount of points must be > 0");
        }

        x = mbr.x1;
        y = mbr.y1;

        // Start with a rough estimate for number of cells assuming uniformity
        numTiles = (int) Math.ceil(points.length / capacity);
        GridInfo gridInfo = new GridInfo(mbr.x1, mbr.y1, mbr.x2, mbr.y2);


        int maxCellSize;
        int maxIterations = 1000;

        do {
            int cols = (int) Math.round(Math.sqrt(numTiles));

            this.numColumns = gridInfo.columns = Math.max(1, cols);
            this.numRows = gridInfo.rows = (int) Math.ceil(numTiles / gridInfo.columns);
            maxCellSize = 0;
            // TODO uncomment the following part to further breakdown big tiles
//      int[] histogram = new int[gridInfo.columns * gridInfo.rows];
//      for (Point point : points) {
//        int cell = gridInfo.getOverlappingCell(point.x, point.y);
//        if (++histogram[cell] > maxCellSize)
//          maxCellSize = histogram[cell];
//      }
//      if (maxCellSize > capacity) {
//        // Further break the largest grid cell
//        numTiles = (int) (numTiles * Math.ceil((double)maxCellSize / capacity));
//      }
        } while (maxCellSize > capacity && maxIterations-- > 0);
        tileWidth = mbr.getWidth() / gridInfo.columns;
        tileHeight = mbr.getHeight() / gridInfo.rows;
    }

    public int getPartitionCount() {
        return numTiles;
    }

    public void overlapPartitions(Shape shape, ResultCollector<Integer> matcher) {
        if (shape == null) {
            return;
        }
        Rectangle shapeMBR = shape.getMBR();
        if (shapeMBR == null) {
            return;
        }
        int col1, col2, row1, row2;
        col1 = (int) Math.floor((shapeMBR.x1 - x) / tileWidth);
        col2 = (int) Math.ceil((shapeMBR.x2 - x) / tileWidth);
        row1 = (int) Math.floor((shapeMBR.y1 - y) / tileHeight);
        row2 = (int) Math.ceil((shapeMBR.y2 - y) / tileHeight);

        if (col1 < 0) {
            col1 = 0;
        }
        if (row1 < 0) {
            row1 = 0;
        }
        for (int col = col1; col < col2; col++) {
            for (int row = row1; row < row2; row++) {
                matcher.collect(getCellNumber(col, row));
            }
        }
    }

    private int getCellNumber(int col, int row) {
        return row * numColumns + col;
    }

    public int overlapPartition(Shape shape) {
        if (shape == null) {
            return -1;
        }
        Rectangle shapeMBR = shape.getMBR();
        if (shapeMBR == null) {
            return -1;
        }
        Point centerPoint = shapeMBR.getCenterPoint();
        int col = (int) Math.floor((centerPoint.x - x) / tileWidth);
        int row = (int) Math.floor((centerPoint.y - y) / tileHeight);
        return getCellNumber(col, row);
    }

    public CellInfo getPartition(int partitionID) {
        // Retrieve column and row of the given partition
        int col = partitionID % numColumns;
        int row = partitionID / numColumns;
        return new CellInfo(partitionID, x + col * tileWidth, y + row * tileHeight,
                x + (col + 1) * tileWidth, y + (row + 1) * tileHeight);
    }
    
    
    public CellInfo getPartitionAt(int index) {
        return getPartition(index);
    }
   
}
