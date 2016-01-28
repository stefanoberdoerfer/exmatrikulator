package de.unibremen.opensores.model;

/**
 * Enumeration with all possible global roles a user in Exmatrikulator
 * could have. An element has an unique id to be easily stored in the
 * database.
 */
public enum GlobalRole {
    ADMIN(0), LECTURER(1), USER(2);

    private Integer id;

    GlobalRole(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
