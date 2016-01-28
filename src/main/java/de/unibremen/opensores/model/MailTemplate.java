package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * Entity bean for the MailTemplate Class.
 *
 * @author Lorenz Huether
 */
@Entity
@Table(name = "MAIL_TEMPLATES")
public class MailTemplate {

    @Id
    @GeneratedValue
    private Long mailTemplateId;

    @OneToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @Column(nullable = false)
    private String subject;

    @Column(name = "text", nullable = false, columnDefinition = "LONG VARCHAR")
    private String text;

    @Column(nullable = false)
    private String locale;

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getLocale() {
        return locale;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
