package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Entity bean for an RecordBook entry (timetracking per exam).
 *
 * @author Stefan Oberdoerfer
 */
@Entity
@Table(name = "RECBOOK_ENTRIES")
public class RecordBookEntry {

    @Id
    @GeneratedValue
    private Long entryId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "examId")
    private Exam exam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "studentId")
    private Student student;

    @Column
    private String comment;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private Integer duration;


    /**
     * Static constructor method to create a RecordBookEntry given the student, course, exam,
     * date, duration and an optional comment.
     * @return The RecordBookEntry with corresponding attributes.
     */
    public static RecordBookEntry from(Student student, Course course, Exam exam,
                                       Date date, int duration, String comment) {
        if (student == null) {
            throw new IllegalArgumentException("The user can't be null.");
        }
        if (comment == null) {
            comment = "";
        }

        RecordBookEntry rbe = new RecordBookEntry();
        rbe.student = student;
        rbe.course = course;
        rbe.exam = exam;
        rbe.duration = duration;
        rbe.comment = comment;
        rbe.date = date;
        return rbe;
    }

    public Long getEntryId() {
        return entryId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Custom getter necessary to not expose internal representation.
     */
    public Date getDate() {
        if (date == null) {
            return null;
        } else {
            return new Date(date.getTime());
        }
    }

    /**
     * Custom setter necessary to not expose internal representation.
     */
    public void setDate(final Date date) {
        if (date == null) {
            this.date = null;
        } else {
            this.date = new Date(date.getTime());
        }
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
