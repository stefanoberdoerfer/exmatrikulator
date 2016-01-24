package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity bean for the ParticipationType class.
 */
@Entity
@Table(name = "PARTICIPATIONTYPES")
public class ParticipationType {

    @Id
    @GeneratedValue
    private Long partTypeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean isGroupPerformance;

    @Column
    private String performanceContent;

    @Column
    private String performanceArea;

    @Column(nullable = false)
    private Boolean isRestricted;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @ManyToMany
    @JoinTable(name = "PARTTYPE_EXAM",
            joinColumns = {@JoinColumn(name = "parttypeId")},
            inverseJoinColumns = {@JoinColumn(name = "examId")})
    private List<Exam> exams = new ArrayList<>();

    @OneToMany(mappedBy = "participationType")
    private List<Student> students = new ArrayList<>();

    //private GradeFormula gradeFormula;

    //private List<GradeFormula> oldFormulas;

    //TODO: Boolean isDefault?

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

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Long getPartTypeId() {
        return partTypeId;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    /*
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
    */
}
