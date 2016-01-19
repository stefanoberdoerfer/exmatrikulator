package de.unibremen.opensores.model;

/**
 * Entity bean for the Semester class.
 */
public class Semester {

    private String name;

    private Boolean isWinter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getWinter() {
        return isWinter;
    }

    public void setWinter(Boolean winter) {
        isWinter = winter;
    }
}
