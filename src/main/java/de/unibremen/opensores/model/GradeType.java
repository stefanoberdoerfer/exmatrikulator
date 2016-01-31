package de.unibremen.opensores.model;

/**
 * Enumeration of the different GradeTypes of a Grade.
 */
public enum GradeType {
    Numeric(1), Boolean(2), Point(3), Percent(4);

    private Integer id;

    GradeType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
