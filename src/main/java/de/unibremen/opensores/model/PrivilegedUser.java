package de.unibremen.opensores.model;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
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
    private Boolean isSecretary = false;

    @Column
    private Date lastHit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @Column(nullable = false)
    private Boolean isHidden = false;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @ManyToMany(mappedBy = "tutors")
    private List<Tutorial> tutorials = new ArrayList<>();

    @OneToMany(mappedBy = "examiner", cascade = CascadeType.MERGE)
    private List<ExamEvent> examEvents = new ArrayList<>();

    @Override
    public boolean equals(Object object) {
        return object instanceof PrivilegedUser && (privUserId != null)
                ? privUserId.equals(((PrivilegedUser) object).privUserId)
                : (object == this);
    }

    @Override
    public int hashCode() {
        return privUserId != null
                ? this.getClass().hashCode() + privUserId.hashCode()
                : super.hashCode();
    }

    public Long getPrivUserId() {
        return privUserId;
    }

    public void setPrivUserId(Long privUserId) {
        this.privUserId = privUserId;
    }

    public boolean hasPrivilege(Privilege priv) {
        return privileges.contains(priv.getId());
    }

    public boolean hasPrivilege(String privString) {
        return hasPrivilege(Privilege.valueOf(privString));
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
        return new Date(lastHit.getTime());
    }

    public void setLastHit(Date lastHit) {
        this.lastHit = new Date(lastHit.getTime());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean isHidden() {
        return isHidden;
    }

    public void setHidden(Boolean isHidden) {
        this.isHidden = isHidden;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    public List<ExamEvent> getExamEvents() {
        return examEvents;
    }

    public void setExamEvents(List<ExamEvent> examEvents) {
        this.examEvents = examEvents;
    }
}
