package de.unibremen.opensores.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entity bean for the Grading class.
 *
 * @author Stefan Oberdoerfer, Lorenz Huether
 */
@Entity
@Table(name = "GRADINGS")
public class Grading {

    @Id
    @GeneratedValue
    private Long gradingId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User corrector;

    @ManyToOne(optional = false)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @OneToOne(optional = false)
    private Exam exam;

    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    private Grade grade;

    @Column
    private String publicComment;

    @Column
    private String privateComment;

    @OneToOne(optional = true)
    private Group group;

    public User getCorrector() {
        return corrector;
    }

    public void setCorrector(User corrector) {
        this.corrector = corrector;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public String getPublicComment() {
        return publicComment;
    }

    public void setPublicComment(String publicComment) {
        this.publicComment = publicComment;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public void setPrivateComment(String privateComment) {
        this.privateComment = privateComment;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public Long getGradingId() {
        return gradingId;
    }
}
