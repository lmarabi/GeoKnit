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
public class GridInfo extends Rectangle {

    public int columns, rows;

    public GridInfo() {
    }

    public GridInfo(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
        this.columns = 0;
        this.rows = 0;
    }

    @Override
    public String toString() {
        return "grid: " + x1 + "," + y1 + "," + x2 + "," + y2 + ", "
                + "cell: " + getAverageCellWidth() + "," + getAverageCellHeight()
                + "(" + columns + "x" + rows + ")";
    }

    public double getAverageCellHeight() {
        return (y2 - y1) / Math.max(rows, 1);
    }

    public double getAverageCellWidth() {
        return (x2 - x1) / Math.max(columns, 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        GridInfo gi = (GridInfo) obj;
        return super.equals(obj)
                && this.columns == gi.columns && this.rows == gi.rows;
    }

    @Override
    public int hashCode() {
        int result = this.columns;
        result = 31 * result + this.rows;
        return result;
    }

    public void calculateCellDimensions(long totalFileSize, long blockSize) {
        // An empirical number for the expected overhead in grid file due to
        // replication
        int numBlocks = (int) Math.ceil((double) totalFileSize / blockSize);
        calculateCellDimensions(numBlocks);
    }

    public void calculateCellDimensions(int numCells) {
        int gridCols = 1;
        int gridRows = 1;
        while (gridRows * gridCols < numCells) {
            // (  cellWidth          >    cellHeight        )
            if ((x2 - x1) / gridCols > (y2 - y1) / gridRows) {
                gridCols++;
            } else {
                gridRows++;
            }
        }
        columns = gridCols;
        rows = gridRows;
    }

    public CellInfo[] getAllCells() {
        int cellIndex = 0;
        CellInfo[] cells = new CellInfo[columns * rows];
        double ystart = y1;
        for (int row = 0; row < rows; row++) {
            double yend = row == rows - 1 ? y2 : (y1 + (y2 - y1) * (row + 1) / rows);
            double xstart = x1;
            for (int col = 0; col < columns; col++) {
                double xend = col == columns - 1 ? x2 : (x1 + (x2 - x1) * (col + 1) / columns);

                cells[cellIndex] = new CellInfo(++cellIndex, xstart, ystart, xend, yend);

                xstart = xend;
            }
            ystart = yend;
        }
        return cells;
    }

    /**
     * Computes the range of all cells that overlap a given rectangle
     *
     * @param rect
     * @return
     */
    public java.awt.Rectangle getOverlappingCells(Rectangle rect) {
        int col1, col2, row1, row2;
        col1 = (int) Math.floor((rect.x1 - this.x1) / this.getWidth() * columns);
        if (col1 < 0) {
            col1 = 0;
        }
        col2 = (int) Math.ceil((rect.x2 - this.x1) / this.getWidth() * columns);
        if (col2 > columns) {
            col2 = columns;
        }
        row1 = (int) Math.floor((rect.y1 - this.y1) / this.getHeight() * rows);
        if (row1 < 0) {
            row1 = 0;
        }
        row2 = (int) Math.ceil((rect.y2 - this.y1) / this.getHeight() * rows);
        if (row2 > rows) {
            row2 = rows;
        }
        return new java.awt.Rectangle(col1, row1, col2 - col1, row2 - row1);
    }

    public int getOverlappingCell(double x, double y) {
        if (!contains(x, y)) {
            return -1;
        }
        int column = (int) Math.floor((x - this.x1) / this.getWidth() * columns);
        int row = (int) Math.floor((y - this.y1) / this.getHeight() * rows);
        return getCellId(column, row);
    }

    /**
     * Returns the information of a grid cell given it ID
     *
     * @param cellId
     * @return
     */
    public CellInfo getCell(int cellId) {
        int col = (cellId - 1) % columns;
        int row = (cellId - 1) / columns;
        double xstart = x1 + (x2 - x1) * col / columns;
        double xend = col == columns - 1 ? x2 : (x1 + (x2 - x1) * (col + 1) / columns);
        double ystart = y1 + (y2 - y1) * row / rows;
        double yend = (row == rows - 1) ? y2 : (y1 + (y2 - y1) * (row + 1) / rows);
        return new CellInfo(cellId, xstart, ystart, xend, yend);
    }

    public int getCellId(int column, int row) {
        return (row * columns + column) + 1;
    }
}
