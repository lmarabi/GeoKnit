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
public class CellInfo extends Rectangle {

    /**
     * A unique ID for this cell in a file. This must be set initially when
     * cells for a file are created. It cannot be guessed from cell dimensions.
     */
    public int cellId;

    /**
     * Loads a cell serialized to the given stream
     *
     * @param in
     * @throws IOException
     */
  

    public CellInfo() {
        super();
    }

    public CellInfo(int id, double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
        this.cellId = id;
    }

    public CellInfo(int id, Rectangle cellInfo) {
        this(id, cellInfo.x1, cellInfo.y1, cellInfo.x2, cellInfo.y2);
        if (id == 0) {
            throw new RuntimeException("Invalid cell id: " + id);
        }
    }

    public CellInfo(CellInfo c) {
        this.set(c);
    }

    public void set(CellInfo c) {
        if (c == null) {
            this.cellId = 0; // Invalid number
        } else {
            super.set(c); // Set rectangle
            this.cellId = c.cellId; // Set cellId
        }
    }

    @Override
    public String toString() {
        return "Cell #" + cellId + " " + super.toString();
    }

    @Override
    public CellInfo clone() {
        return new CellInfo(cellId, x1, y1, x2, y2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return ((CellInfo) obj).cellId == this.cellId;
    }

    @Override
    public int hashCode() {
        return (int) this.cellId;
    }

    @Override
    public int compareTo(Rectangle s) {
        return (int) (this.cellId - ((CellInfo) s).cellId);
    }



    @Override
    public String toWKT() {
        return cellId + "," + super.Apache();
    }
}
