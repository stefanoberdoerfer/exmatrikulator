package de.unibremen.opensores.model;

import de.unibremen.opensores.model.PaboData;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
    private Boolean isDeleted;

    @Column(nullable = false)
    private Boolean isConfirmed;

    @Column(nullable = false)
    private Boolean acceptedInvitation;

    @Column(nullable = false)
    private Boolean isHidden = false;

    @Column
    private String paboGrade;

    @Column
    private String publicComment;

    @Column
    private String privateComment;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "paboDataId")
    private PaboData paboData;

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

    @ManyToOne(optional = true)
    @JoinColumn(name = "tutorialId")
    private Tutorial tutorial;

    @ManyToOne(optional = true)
    @JoinColumn(name = "groupId")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "parttypeId")
    private ParticipationType participationType;

    @ManyToMany(mappedBy = "uploaders")
    private List<Upload> uploads = new ArrayList<>();

    @ManyToMany(mappedBy = "examinedStudents")
    private List<ExamEvent> examEvents = new ArrayList<>();


    /*
     * Public methods
     */

    @Override
    public boolean equals(Object object) {
        return object instanceof Student && ( studentId != null)
                ? this.studentId.equals(((Student) object).studentId)
                : (object == this);
    }

    @Override
    public int hashCode() {
        return this.studentId != null
                ? this.getClass().hashCode() + this.studentId.hashCode()
                : super.hashCode();
    }

    /**
     * Gets the grading of the student given the exam.
     * @param exam The exam of which the grading of the student should be returned.
     * @return The grading object of the student and the exam, or null if it doesn't
     *         exist.
     */
    public Grading getGradingFromExam(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("The exam can't be null");
        }
        for (Grading grading: gradings) {
            if (grading.getExam() != null && grading.getExam().equals(exam)) {
                return grading;
            }
        }
        return null;
    }

    public boolean isHidden() {
        return isHidden;
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

    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getPublicComment() {
        return publicComment;
    }

    public void setPublicComment(String publicComment) {
        this.publicComment = publicComment;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public void setPrivateComment(String privateComment) {
        this.privateComment = privateComment;
    }

    public PaboData getPaboData() {
        return paboData;
    }

    public void setPaboData(PaboData paboData) {
        this.paboData = paboData;
    }

    public List<ExamEvent> getExamEvents() {
        return examEvents;
    }

    public void setExamEvents(List<ExamEvent> examEvents) {
        this.examEvents = examEvents;
    }
}
