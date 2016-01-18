package de.unibremen.opensores.model;

/**
 * Enumeration with all possible roles a user in Exmatrikulator could have.
 * An element has an unique id to be easily stored in the database.
 */
public enum Role {
    ADMIN(0), LECTURER(1), PRIVILEGED_USER(2), STUDENT(3), USER(4);

    private Integer id;

    Role(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}