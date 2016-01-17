package de.unibremen.opensores.model;

import java.util.List;

/**
 * Entity bean for the ParticipationType class.
 */
public class ParticipationType {

    private Long parttypeId;

    private String name;

    private Boolean isGroupPerformance;

    private String performanceContent;

    private String performanceArea;

    private Boolean isRestricted;

    private Course course;

    private List<Exam> exams;

    private List<Student> students;

    private GradeFormula gradeFormula;

    private List<GradeFormula> oldFormulas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getGroupPerformance() {
        return isGroupPerformance;
    }

    public void setGroupPerformance(Boolean groupPerformance) {
        isGroupPerformance = groupPerformance;
    }

    public String getPerformanceContent() {
        return performanceContent;
    }

    public void setPerformanceContent(String performanceContent) {
        this.performanceContent = performanceContent;
    }

    public String getPerformanceArea() {
        return performanceArea;
    }

    public void setPerformanceArea(String performanceArea) {
        this.performanceArea = performanceArea;
    }

    public Boolean getRestricted() {
        return isRestricted;
    }

    public void setRestricted(Boolean restricted) {
        isRestricted = restricted;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public GradeFormula getGradeFormula() {
        return gradeFormula;
    }

    public void setGradeFormula(GradeFormula gradeFormula) {
        this.gradeFormula = gradeFormula;
    }

    public List<GradeFormula> getOldFormulas() {
        return oldFormulas;
    }

    public void setOldFormulas(List<GradeFormula> oldFormulas) {
        this.oldFormulas = oldFormulas;
    }
}
