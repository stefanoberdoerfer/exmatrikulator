package de.unibremen.opensores.model;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity bean for the PrivilegedUser class.
 */
@Entity
@Table(name = "PRIVUSERS")
public class PrivilegedUser {

    @Id
    @GeneratedValue
    private Long privUserId;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Integer.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "privUserId"))
    @Column(name = "privs") //privileges is an SQL keyword
    private List<Integer> privileges = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isSecretary;

    @Column
    private Date lastHit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @ManyToMany(mappedBy = "tutors")
    private List<Tutorial> tutorials = new ArrayList<>();

    public boolean hasPrivilege(String privString) {
        return privileges.contains(Role.valueOf(privString).getId());
    }

    public List<Integer> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Integer> privileges) {
        this.privileges = privileges;
    }

    public Boolean isSecretary() {
        return isSecretary;
    }

    public void setSecretary(Boolean secretary) {
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
