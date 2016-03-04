package de.unibremen.opensores.model;

import de.unibremen.opensores.util.mail.MailJob;
import de.unibremen.opensores.util.mail.Mail;

import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @Column(nullable = false)
    private String subject;

    @Column(name = "text", nullable = false, columnDefinition = "LONG VARCHAR")
    private String text;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private Boolean isDefault;

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getLocale() {
        return locale;
    }

    public String getName() {
        return name;
    }

    public Long getMailTemplateId() {
        return mailTemplateId;
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

    public void setName(String name) {
        this.name = name;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setMailTemplateId(Long mailTemplateId) {
        this.mailTemplateId = mailTemplateId;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Generates the text from a template and a given map.
     *
     * @param map the map.
     * @return the filled template as a string.
     */
    private String toString(Map<String, Object> map) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(text), "template");
        return mustache.execute(new StringWriter(), map).toString();
    }

    /**
     * Retrieves the needed data fields from student.
     *
     * @param student the student in question.
     * @return a map with the student's fields or null.
     */
    private Map<String, Object> getTemplateData(Student student) {
        User user = student.getUser();

        ArrayList<String> gradeList = new ArrayList<>();
        for (Grading g : student.getGradings()) {
            gradeList.add(g.getGrade().getValue().toString());
        }

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String salutation = user.getSalutation();
        String comment = student.getPublicComment();
        String semester = course.getSemester().toString();
        String courseName = course.getName();

        HashMap<String, Object> map = new HashMap<>();
        map.put("firstName", (firstName == null) ? "" : firstName);
        map.put("lastName", (lastName == null) ? "" : lastName);
        map.put("salutation", (salutation == null) ? "" : salutation);
        map.put("paboGrade", "");
        map.put("comment", (comment == null) ? "" : comment);
        map.put("semester", semester);
        map.put("courseName", (courseName == null) ? "" : courseName);
        map.put("grades", (gradeList.isEmpty()) ? "" : gradeList);

        return map;
    }

    /**
     * Issues the mail for a list of students.
     *
     * @param students the list of students.
     * @throws IOException If an IO error occured while sending the mail.
     * @throws MessagingException If the mail couldn't be send.
     */
    public void issue(List<Student> students)
            throws IOException, MessagingException {
        List<MailJob> jobs = new ArrayList<>();
        for (Student s : students) {
            String mail = s.getUser().getEmail();
            String text = toString(getTemplateData(s));

            jobs.add(new MailJob(new String[] {mail},
                        subject, text));
        }

        MailJob[] jobArray = new MailJob[jobs.size()];
        new Mail().issue(jobs.toArray(jobArray));
    }
}
