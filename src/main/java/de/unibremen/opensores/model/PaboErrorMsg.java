package de.unibremen.opensores.model;

/**
 * Helper class for displaying pabo error messages, because a map can't be sorted
 * by UI Frameworks.
 * Is not an entity.
 */
public class PaboErrorMsg {

    public PaboErrorMsg(int row, String message) {
        this.row = row;
        this.message = message;
    }

    private int row;

    private String message;


    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
