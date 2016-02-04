package de.unibremen.opensores.model;

/**
 * Wrapper class for strings needed by JSF in order to use
 * dynamic adding of elements to a table.
 */
public class Field {

    String data;

    public Field() {
    }

    public Field(String str) {
        this.data = str;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
