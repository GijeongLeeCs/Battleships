package model;

import java.io.Serial;
import java.io.Serializable;

public class Point implements Serializable {

    public int row;
    public int col;
    @Serial
    private static final long serialVersionUID = 0L;

    public Point(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public int hashCode() {
        return row + 10*col;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Point otherP)) {
            return false;
        }
        return otherP.row == this.row && otherP.col == this.col;
    }

    @Override
    public String toString() {
        return row + " " + col;
    }
    
    public int getRow() {
    	return row;
    }
    
    public int getCol() {
    	return col;
    }
}
