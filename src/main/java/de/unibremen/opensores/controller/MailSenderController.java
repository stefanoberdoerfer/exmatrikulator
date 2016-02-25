package de.unibremen.opensores.controller;

import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.MailTemplate;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.MailTemplateService;
import de.unibremen.opensores.util.mail.MailJob;
import de.unibremen.opensores.util.mail.Mail;
import de.unibremen.opensores.util.Constants;

import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;
import javax.ejb.EJB;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.MessagingException;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.annotation.PostConstruct;

/**
 * Controller for sending mails with templates.
 *
 * @Author Lorenz Hüther
 */
@ManagedBean
@RequestScoped
public class MailSenderController {

    @EJB
    private GradeService gradeService;

    @EJB
    private MailTemplateService mailTemplateService;

    /**
     * The Logger.
     */
    private Logger log = LogManager.getLogger(MailTemplateController.class);

    /**
     * Generates the text from a template and a given map.
     *
     * @param map the map.
     * @param template the template to use.
     *
     * @return the filled template as a string.
     */
    public String templateToString(Map<String, Object> map, MailTemplate template) {
        String text = template.getText();

        if (text == null || map == null) {
            log.error("Got a null pointer in text or map.");
            return "";
        }

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(text), "template");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, map);
        text =  writer.toString();
        try {
            writer.close();
        } catch (final IOException e) {
            log.error(e);
        }
        return text;
    }

    /**
     * Retrieves the needed data fields fro ma student.
     *
     * @param student the student in question.
     *
     * @return a map with the student's fields or null.
     */
    public Map<String, Object> getTemplateData(Student student) {
        User user = student.getUser();

        ArrayList<String> gradeList = new ArrayList<>();
        for (Grading g : student.getGradings()) {
            gradeList.add(g.getGrade().getValue().toString());
        }

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String salution = user.getSalution();
        String comment = student.getPublicComment();
        String paboGrade = student.getPaboGrade();
        String semester = student.getCourse().getSemester().toString();
        String courseName = student.getCourse().getName();

        HashMap<String, Object> map = new HashMap<>();
        map.put("firstName", (firstName == null) ? "" : firstName);
        map.put("lastName", (lastName == null) ? "" : lastName);
        map.put("salution", (salution == null) ? "" : salution);
        map.put("paboGrade", (paboGrade == null) ? "" : paboGrade);
        map.put("comment", (comment == null) ? "" : comment);
        map.put("semester", semester);
        map.put("courseName", (courseName == null) ? "" : courseName);
        map.put("grades", (gradeList.isEmpty()) ? "" : gradeList);

        return map;
    }

    /**
     * Creates a MailJob for a single Student and a template.
     *
     * @param student the student.
     * @param template the mail template to use.
     *
     * @return a MailJob for the student.
     */
    public MailJob createMailJob(Student student, MailTemplate template) {
        if (student == null || template == null) {
            log.error("Can't issue mail, student or template is null!");
            return null;
        }

        String mail = student.getUser().getEmail();
        String subject = template.getSubject();
        String text = templateToString(getTemplateData(student), template);
        return new MailJob(new String[] {mail}, subject, text);
    }

    /**
     * Issues the mail for a list of students.
     *
     * @param students the list of students.
     * @param template the mail template to use.
     */
    public void issue(List<Student> students, MailTemplate template) {
        if (students.isEmpty() || template == null) {
            log.error("Can't issue mail, students or template is null!");
            return;
        }

        List<MailJob> jobs = new ArrayList<>();
        for (Student s : students) {
            jobs.add(createMailJob(s, template));
        }

        MailJob[] jobArray = new MailJob[jobs.size()];
        try {
            new Mail().issue(jobs.toArray(jobArray));
        } catch (final IOException | MessagingException e) {
            log.error(e);
        }
    }

    /**
     * Sends the final Mail to the whole Course.
     *
     * @param course the course to be informed.
     *
     * @param template the template to send.
     */
    public void sendCourse(Course course, MailTemplate template) {
        if (course == null) {
            return;
        }

        log.debug("Sending mails to course" + course.getName());
        if (template == null) {
            return;
        }

        issue(course.getStudents(), template);
    }
}
