package de.unibremen.opensores.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Entity bean for the Exam class.
 */
public class Exam {

    private Long examId;

    private String name;

    private Grade gradeType;

    private BigDecimal maxPoints;

    private Course course;

    private Date deadline;

    private List<String> allowedMimeTypes;

    private Long maxFileSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grade getGradeType() {
        return gradeType;
    }

    public void setGradeType(Grade gradeType) {
        gradeType = gradeType;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(BigDecimal maxPoints) {
        this.maxPoints = maxPoints;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

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
