package de.unibremen.opensores.model;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.ws.rs.CookieParam;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity bean for the Exam class.
 */
@Entity
@Table(name = "EXAMS")
public class Exam {

    @Id
    @GeneratedValue
    private Long examId;

    @Column(nullable = false)
    private String name;

    //private Grade gradeType;

    @Column
    private BigDecimal maxPoints;

    //@ManyToOne(optional = false)
    //@JoinColumn(name = "courseId")
    //private Course course;

    @Column
    private Date deadline;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "examId"))
    @Column
    private List<String> allowedMimeTypes = new ArrayList<>();

    @Column
    private Long maxFileSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
    public Grade getGradeType() {
        return gradeType;
    }

    public void setGradeType(Grade gradeType) {
        gradeType = gradeType;
    }
    */

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(BigDecimal maxPoints) {
        this.maxPoints = maxPoints;
    }

    /*
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
    */

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
