package de.unibremen.opensores.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 * The entity for an exam event, used as an anointment for examinations with
 * attendance.
 */
@Entity
public class ExamEvent extends DefaultEvent implements Serializable {

    private static final long serialVersionUID = 1479290959512783799L;

    /**
     * The examiner of the ExamEvent, can be the user of a privileged user.
     */
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @ManyToOne
    private PrivilegedUser examiner;

    /**
     * The exam in which the examination is done.
     */
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @ManyToOne
    private Exam exam;

    /**
     * The students whic get examined in this exam event.
     */
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @ManyToMany
    @JoinTable(name = "EXAM_EVENT_STUDENT",
            joinColumns = {@JoinColumn(name = "eventId")},
            inverseJoinColumns = {@JoinColumn(name = "studentId")})
    private List<Student> examinedStudents = new ArrayList<>();


    /**
     * The maximal number of students which can register to this exam event.
     */
    @Column(nullable = false)
    private int maxNumStudents;


    /*
     * Constructors
     */


    /**
     * Default constructor for an ExamEvent.
     */
    public ExamEvent() {
        super();
        this.maxNumStudents = 1;
    }

    /**
     * Constructor for adding the exam and dates conveniently.
     * @param exam The exam in which the exam event is in.
     * @param startDate The start date of the exam event.
     * @param endDate The end date of the exam event.
     */
    public ExamEvent(Exam exam, Date startDate, Date endDate) {
        this();
        this.exam = exam;
        this.setStartDate(new Date(startDate.getTime()));
        this.setEndDate(new Date(endDate.getTime()));
    }

    /*
     * Public methods
     */


    public boolean isFull() {
        return examinedStudents.size() >= maxNumStudents;
    }


    @Override
    public boolean equals(Object object) {
        return object instanceof ExamEvent && ( this.getEventId() != null)
                ? this.getEventId().equals(((ExamEvent) object).getEventId())
                : (object == this);
    }

    @Override
    public int hashCode() {
        return this.getEventId() != null
                ? this.getClass().hashCode() + this.getEventId().hashCode()
                : super.hashCode();
    }

    @Override
    public String getTitle() {
        if (exam == null) {
            return super.toString();
        }
        return String.format("%s (%d/%d)", exam.getName(), examinedStudents.size(), maxNumStudents);
    }


    /*
     * Getters and Setters
     */


    public List<Student> getExaminedStudents() {
        return examinedStudents;
    }

    public void setExaminedStudents(List<Student> examinedStudents) {
        this.examinedStudents = examinedStudents;
    }

    public int getMaxNumStudents() {
        return maxNumStudents;
    }

    public void setMaxNumStudents(int maxNumStudents) {
        this.maxNumStudents = maxNumStudents;
    }

    public PrivilegedUser getExaminer() {
        return examiner;
    }

    public void setExaminer(PrivilegedUser examiner) {
        this.examiner = examiner;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

}
