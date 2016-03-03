package de.unibremen.opensores.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

    /**
     * The examination with attendance events of an exam.
     */
    @OneToMany(mappedBy = "exam", cascade = CascadeType.MERGE)
    private List<ExamEvent> events = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    @NotFound(action = NotFoundAction.IGNORE)
    //@NotFound because of
    //http://stackoverflow.com/questions/10347218/null-object-if-entity-not-found
    //Creating new course because of
    //http://stackoverflow.com/questions/13539050/entitynotfoundexception-in-hibernate-many-to-one-mapping-however-data-exist
    private Course course = new Course();


    /**
     * The deadline for the registration for events for this event if it is with
     * attendance.
     */
    @Column
    private Date deadline;

    @Column
    private boolean isWithAttendance;

    @Override
    public boolean equals(Object object) {
        return object instanceof Exam && (examId != null)
                ? examId.equals(((Exam) object).examId)
                : (object == this);
    }

    @Override
    public int hashCode() {
        return examId != null
                ? this.getClass().hashCode() + examId.hashCode()
                : super.hashCode();
    }


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


    /**
     * Returns if the given grading value is valid.
     * @param value Value to check
     * @return true if valid
     */
    public boolean isValidGrading(BigDecimal value) {
        if (value == null) {
            return false;
        }

        for (GradeType g : GradeType.values()) {
            if (g.getId().equals(this.gradeType)) {
                return g.isValidGrading(value, maxPoints);
            }
        }

        return false;
    }

    public boolean isWithAttendance() {
        return isWithAttendance;
    }

    public void setWithAttendance(boolean withAttendance) {
        isWithAttendance = withAttendance;
    }

    public List<ExamEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ExamEvent> events) {
        this.events = events;
    }
}
