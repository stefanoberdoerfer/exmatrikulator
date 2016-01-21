package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "text", nullable = false)
    private String text;

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }
}
