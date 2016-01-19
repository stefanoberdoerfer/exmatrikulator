package de.unibremen.opensores.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity bean for the Course class.
 */
public class Course {

    private Long courseId;

    private String name;

    private String number;

    private Boolean requiresConformation;

    private String emailTemplate;

    private Integer minGroupSize;

    private Integer maxGroupSize;

    private List<Student> students;

    private List<Lecturer> lecturers;

    private List<PrivilegedUser> tutors;

    private Semester semester;

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

    public List<Lecturer> getLecturers() {
        return lecturers;
    }

    public void setLecturers(List<Lecturer> lecturers) {
        this.lecturers = lecturers;
    }

    public List<PrivilegedUser> getTutors() {
        return tutors;
    }

    public void setTutors(List<PrivilegedUser> tutors) {
        this.tutors = tutors;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
