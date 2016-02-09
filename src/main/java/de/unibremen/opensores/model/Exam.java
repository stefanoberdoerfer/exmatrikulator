package de.unibremen.opensores.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
    private String name = "";

    @Column(nullable = false)
    private String shortcut;

    @Column
    private Integer gradeType;

    @Column
    private BigDecimal maxPoints;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    @NotFound(action = NotFoundAction.IGNORE)
    //@NotFound because of
    //http://stackoverflow.com/questions/10347218/null-object-if-entity-not-found
    //Creating new course because of
    //http://stackoverflow.com/questions/13539050/entitynotfoundexception-in-hibernate-many-to-one-mapping-however-data-exist
    private Course course = new Course();

    /**
     * Boolean value to indicate if the exam has an upload to it.
     */
    @Column
    private boolean uploadAssignment;

    @Column
    private Date deadline;

    @ElementCollection
    @CollectionTable(name = "EXAM_ALLOWED_FILE_TYPES", joinColumns = @JoinColumn(name = "examId"))
    @Column
    private List<String> allowedFileEndings = new ArrayList<>();

    /**
     * The maximal file size in MegaByte.
     */
    @Column
    private Long maxFileSizeMB;

    @Column
    private boolean gradableByTutors;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return (deadline == null) ? null :
            new Date(deadline.getTime());
    }

    public void setDeadline(Date deadline) {
        this.deadline = (deadline == null) ? null :
            new Date(deadline.getTime());
    }

    public Long getMaxFileSizeMB() {
        return maxFileSizeMB;
    }

    public void setMaxFileSizeMB(Long maxFileSizeMB) {
        this.maxFileSizeMB = maxFileSizeMB;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Integer getGradeType() {
        return gradeType;
    }

    public void setGradeType(Integer gradeType) {
        this.gradeType = gradeType;
    }

    public boolean hasGradeType(GradeType gradeType) {
        return this.gradeType.equals(gradeType.getId());
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public boolean isGradableByTutors() {
        return gradableByTutors;
    }

    public void setGradableByTutors(boolean gradableByTutors) {
        this.gradableByTutors = gradableByTutors;
    }

    public boolean isUploadAssignment() {
        return uploadAssignment;
    }

    public void setUploadAssignment(boolean uploadAssignment) {
        this.uploadAssignment = uploadAssignment;
    }

    public List<String> getAllowedFileEndings() {
        return allowedFileEndings;
    }

    public void setAllowedFileEndings(List<String> allowedFileEndings) {
        this.allowedFileEndings = allowedFileEndings;
    }

    public boolean isValidGrading(String value) {
        value = value.replace(',', '.');
        BigDecimal decimal = new BigDecimal(value);

        for (GradeType g : GradeType.values()) {
            if (g.getId().equals(this.gradeType))
                return g.isValidGrading(decimal, maxPoints);
        }

        throw new IllegalArgumentException("Invalid GradeType enum id");
    }
}
