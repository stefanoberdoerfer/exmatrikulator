package de.unibremen.opensores.model;

/**
 * Enumeration with course specific roles.
 */
public enum Role {
    LECTURER(0), PRIVILEGED_USER(1), STUDENT(2);

    private Integer id;

    Role(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
