package de.unibremen.opensores.model;

import java.util.Date;
import java.util.List;

/**
 * Entity bean for the PrivilegedUser class.
 */
public class PrivilegedUser {

    private List<Privilege> privileges;

    private boolean isSecretary;

    private Date lastHit;

    private User user;

    private Course course;

    private List<Tutorial> tutorials;

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public boolean isSecretary() {
        return isSecretary;
    }

    public void setSecretary(boolean secretary) {
        isSecretary = secretary;
    }

    public Date getLastHit() {
        return lastHit;
    }

    public void setLastHit(Date lastHit) {
        this.lastHit = lastHit;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Tutorial> getTutorials() {
        return tutorials;
    }

    public void setTutorials(List<Tutorial> tutorials) {
        this.tutorials = tutorials;
    }
}
