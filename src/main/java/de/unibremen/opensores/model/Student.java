package de.unibremen.opensores.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity bean for the Student class.
 */
@Entity
@Table(name = "STUDENTS")
public class Student {

    @Id
    @GeneratedValue
    private Long studentId;

    @Column(nullable = false)
    private Boolean isAttending;

    @Column(nullable = false)
    private Boolean isConfirmed;

    @Column(nullable = false)
    private Boolean acceptedInvitation;

    //@Column
    //private PaboGrade paboGrade;

    @Column(nullable = false)
    private Integer tries;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "tutorialId")
    private Tutorial tutorial;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "groupId")
    private Group group;

    //@ManyToOne
    //@JoinColumn(name = "tutorialId")
    //private Tutorial tutorial;

    /*
    @ManyToOne
    @JoinColumn(name="groupId")
    private Group group;

    @ManyToOne
    @JoinColumn(name="patricipationId")
    private ParticipationType participationType;
    */
    /*
    private List<Grading> grades;

    private List<Upload> uploads;
    */

    public boolean isAttending() {
        return isAttending;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public boolean hasAcceptedInvitation() {
        return acceptedInvitation;
    }

    /*
    public PaboGrade getPaboGrade() {
        return paboGrade;
    }

    public void setPaboGrade(final PaboGrade paboGrade) {
        this.paboGrade = paboGrade;

    }
    */

    public int getTries() {
        return tries;
    }

    public User getUser() {
        return user;
    }

    public Course getCourse() {
        return course;
    }

    public void setTries(Integer tries) {
        this.tries = tries;

    public Group getGroup() {
        return group;
    }

    public void setAcceptedInvitation(Boolean acceptedInvitation) {
        this.acceptedInvitation = acceptedInvitation;
    }

    public void setConfirmed(Boolean confirmed) {
        isConfirmed = confirmed;
    }

    public void setAttending(Boolean attending) {
        isAttending = attending;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setCourse(final Course course) {
        this.course = course;
    }

    public void setGroup(final Course course) {
        this.group = group;
    }

    /*
    public Tutorial getTutorial() {
        return tutorial;
    }

    public void setTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

    public Long getStudentId() {
        return studentId;
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
    */
}
