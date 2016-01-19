package de.unibremen.opensores.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity bean for the Student class.
 */
public class Student {
    private Long studentId;

    private Boolean isAttending;

    private Boolean isConfirmed;

    private Boolean acceptedInvitation;

    private PaboGrade paboGrade;

    private Integer tries;

    private User user;

    private Course course;

    private Tutorial tutorial;

    private Group group;

    private ParticipationType participationType;

    private List<Grading> grades;

    private List<Upload> uploads;

    public boolean isAttending() {
        return isAttending;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public boolean hasAcceptedInvitation() {
        return acceptedInvitation;
    }

    public PaboGrade getPaboGrade() {
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
        this.acceptedInvitation = acceptedInvitation;
    }

    public void setPaboGrade(final PaboGrade paboGrade) {
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

    public List<Upload> getUploads() {
        return uploads;
    }

    public void setUploads(List<Upload> uploads) {
        this.uploads = uploads;
    }

    public List<Grading> getGrades() {
        return grades;
    }

    public void setGrades(List<Grading> grades) {
        this.grades = grades;
    }

    public ParticipationType getParticipationType() {
        return participationType;
    }

    public void setParticipationType(ParticipationType participationType) {
        this.participationType = participationType;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
