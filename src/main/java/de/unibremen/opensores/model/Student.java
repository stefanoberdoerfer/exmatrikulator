package de.unibremen.opensores.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity bean for the Student class.
 */
@Entity
@Table(name = "students")
public class Student {
    @Id
    @Column(name = "student_id", nullable = false)
    @GeneratedValue
    private Long studentId;

    @Column(name = "is_attending", nullable = false)
    private Boolean isAttending;

    @Column(name = "is_confirmed", nullable = false)
    private Boolean isConfirmed;

    @Column(name = "accepted_invitation", nullable = false)
    private Boolean acceptedInvitation;

    @Column(name = "pabo_grade", nullable = false)
    private Integer paboGrade;

    @Column(name = "tries", nullable = false)
    private Integer tries;

    @OneToOne(optional=false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(optional=false)
    @JoinColumn(name = "course_id")
    private Course course;

    public boolean isAttending() {
        return isAttending;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public boolean hasAcceptedInvitation() {
        return acceptedInvitation;
    }

    public int getPaboGrade() {
        return paboGrade;
    }

    public int getTries() {
        return tries;
    }

    public User getUser() {
        return user;
    }

    public Course getCourse() {
        return course;
    }

    public void setAttending(final boolean isAttending) {
        this.isAttending = isAttending;
    }

    public void setConfirmed(final boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public void setAcceptedInvitation(final boolean acceptedInvitation) {
        this.accepted_invitation = acceptedInvitation;
    }

    public void setPaboGrade(final int paboGrade) {
        this.paboGrade = paboGrade;

    }

    public void setTries(final int tries) {
        this.tries = tries;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setCourse(final Course course) {
        this.course = course;
    }
}
