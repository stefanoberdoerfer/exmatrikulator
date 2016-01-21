package de.unibremen.opensores.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

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
    private List<Lecturer> lecturers;

    /*
    @Column(name = "tutors")
    private List<PrivilegedUser> tutors;

    @Column(name = "semester")
    private Semester semester;
    */

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

    /*
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
    */

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

    public Long getCourseId() {
        return courseId;
    }
}
