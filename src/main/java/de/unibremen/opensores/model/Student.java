package de.unibremen.opensores.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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

    @Column(nullable = false)
    private Boolean isHidden;

    @Column
    private String paboGrade;

    @OneToMany(mappedBy = "student")
    private List<Grading> gradings = new ArrayList<>();

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

    @ManyToOne(optional = true)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "parttypeId")
    private ParticipationType participationType;

    @ManyToMany(mappedBy = "uploaders")
    private List<Upload> uploads;

    public boolean isHidden() {
        return isHidden;
    }

    public boolean isAttending() {
        return isAttending;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public boolean hasAcceptedInvitation() {
        return acceptedInvitation;
    }

    public String getPaboGrade() {
        return paboGrade;
    }

    public void setPaboGrade(String paboGrade) {
        this.paboGrade = paboGrade;
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

    public void setStudentId(long id) {
        this.studentId = id;
    }

    public void setTries(Integer tries) {
        this.tries = tries;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setAcceptedInvitation(Boolean acceptedInvitation) {
        this.acceptedInvitation = acceptedInvitation;
    }

    public void setConfirmed(Boolean confirmed) {
        isConfirmed = confirmed;
    }

    public void setHidden(Boolean isHidden) {
        this.isHidden = isHidden;
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

    public Long getStudentId() {
        return studentId;
    }

    public Tutorial getTutorial() {
        return tutorial;
    }

    public void setTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

    public List<Upload> getUploads() {
        return uploads;
    }

    public void setUploads(List<Upload> uploads) {
        this.uploads = uploads;
    }

    public List<Grading> getGradings() {
        return gradings;
    }

    public void setGradings(List<Grading> gradings) {
        this.gradings = gradings;
    }

    public ParticipationType getParticipationType() {
        return participationType;
    }

    public void setParticipationType(ParticipationType participationType) {
        this.participationType = participationType;
    }
}
