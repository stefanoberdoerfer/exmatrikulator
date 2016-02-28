package de.unibremen.opensores.model;

/**
 * Enumeration with all possible global roles a user in Exmatrikulator
 * could have. An element has an unique id to be easily stored in the
 * database.
 */
public enum GlobalRole {
    ADMIN(0, "common.admin"), LECTURER(1, "common.lecturer"),
    USER(2, "common.user");

    private Integer id;
    private String message;

    GlobalRole(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Returns a specific global role identified by the id.
     * @param id Id to check for
     * @return GlobalRole object
     */
    public static GlobalRole valueOf(Integer id) {
        for (GlobalRole g : GlobalRole.values()) {
            if (g.getId().equals(id)) {
                return g;
            }
        }

        throw new IllegalArgumentException();
    }
}
