package de.unibremen.opensores.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity bean for the Course class.
 *
 * @author Stefan Oberdoerfer, Soeren Tempel, Lorenz Huether
 */
@Entity
@Table(name = "COURSES")
public class Course {

    @Id
    @GeneratedValue
    private Long courseId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer sws;

    @Column(nullable = false)
    private Integer creditPoints;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private Boolean requiresConformation;

    @Column(columnDefinition = "LONG VARCHAR")
    private String emailTemplate;

    @Column
    private Integer minGroupSize;

    @Column
    private Integer maxGroupSize;

    @OneToMany(mappedBy = "course", cascade = CascadeType.MERGE)
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.MERGE)
    private List<Group> groups = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.MERGE)
    private List<Lecturer> lecturers = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.MERGE)
    private List<Tutorial> tutorials = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.MERGE)
    private List<PrivilegedUser> tutors = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.MERGE)
    private List<ParticipationType> participationTypes = new ArrayList<>();

    //@OneToMany(mappedBy = "course", cascade = CascadeType.MERGE)
    //private List<Exam> exams = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "semesterId")
    private Semester semester;

    public Integer getCreditPoints() {
        return creditPoints;
    }

    public void setCreditPoints(Integer creditPoints) {
        this.creditPoints = creditPoints;
    }

    public Integer getSws() {
        return sws;
    }

    public void setSws(Integer sws) {
        this.sws = sws;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public boolean requiresConformation() {
        return requiresConformation;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    public int getMinGroupSize() {
        return minGroupSize;
    }

    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public void setRequiresConformation(final boolean requiresConformation) {
        this.requiresConformation = requiresConformation;
    }

    public void setEmailTemplate(final String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public void setMinGroupSize(final int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    public void setMaxGroupSize(final int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public List<PrivilegedUser> getTutors() {
        return tutors;
    }

    public void setTutors(List<PrivilegedUser> tutors) {
        this.tutors = tutors;
    }

    public List<Lecturer> getLecturers() {
        return lecturers;
    }

    public void setLecturers(List<Lecturer> lecturers) {
        this.lecturers = lecturers;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Long getCourseId() {
        return courseId;
    }

    public List<Tutorial> getTutorials() {
        return tutorials;
    }

    public void setTutorials(List<Tutorial> tutorials) {
        this.tutorials = tutorials;
    }

    public List<ParticipationType> getParticipationTypes() {
        return participationTypes;
    }

    public void setParticipationTypes(List<ParticipationType> participationTypes) {
        this.participationTypes = participationTypes;
    }

    /*
    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }
    */
}
