package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.MailTemplate;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.MailTemplateService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.mail.Mail;
import de.unibremen.opensores.util.mail.MailJob;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

/**
 * The controller for the overview page of the course settings.
 */
@ManagedBean(name = "courseSettings")
@ViewScoped
public class CourseSettingsController {

    /**
     * The path to the course overview site.
     */
    private static final String PATH_TO_COURSE_OVERVIEW =
            "/course/overview.xhtml?faces-redirect=true";

    /**
     * The http parameter key by which the course id gets passed.
     */
    private static final String HTTP_PARAM_COURSE_ID = "course-id";

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(CourseSettingsController.class);

    /**
     * The GradeService for retrieving grade names.
     */
    @EJB
    private GradeService gradeService;

    /**
     * The MailTemplateService for getting the default template.
     */
    @EJB
    private MailTemplateService mailTemplateService;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * UserService for database transactions related to users.
     */
    @EJB
    private UserService userService;

    /**
     * The course for which the overview page gets accessed.
     */
    private Course course;

    /**
     * The user currently logged in.
     */
    private User user;

    /**
     * Method called when the bean is initialised.
     * Gets the course id from the http params and gets the corresponding course.
     * Redirects to the course overview page if the course is not found (incase
     * an invalid passed course id).
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        HttpServletRequest httpReq
                = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        log.debug("Request URI: " + httpReq.getRequestURI());
        final String courseIdString = httpReq.getParameter(HTTP_PARAM_COURSE_ID);

        log.debug("course-id: " + courseIdString);
        long courseId = -1;
        if (courseIdString != null) {
            try {
                courseId = Long.parseLong(courseIdString.trim());
            } catch (NumberFormatException e) {
                log.debug("NumberFormatException while parsing courseId");
            }
        }

        if (courseId != -1) {
            course = courseService.find(Course.class, courseId);
        }

        log.debug("Loaded course object: " + course);
        if (course == null) {
            log.debug("trying to redirect to /course/overview");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + PATH_TO_COURSE_OVERVIEW);
                return;
            } catch (IOException e) {
                log.error(e);
                log.fatal("Could not redirect to " + PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }

        user = (User) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(Constants.SESSION_MAP_KEY_USER);
    }

    /**
     * Deletes a course.
     *
     * @return String redirect to Settings page"
     */
    public String deleteCourse() {
        log.debug("Deleting course " + course.getName());
        course.setDeleted(true);
        courseService.update(course);
        return PATH_TO_COURSE_OVERVIEW;
    }

    /**
     * Finalizes the course and sends mail to all students.
     */
    public void finalizeAndSend() {
        course.setLastFinalization(new Date());
        courseService.update(course);
        sendCourse();
    }

    /**
     * Wether the user is allowed to print or not.
     *
     * @return boolean true if yes
     */
    public boolean isLecturer() {
        if (user == null) {
            return false;
        } else {
            return userService.hasCourseRole(user, "LECTURER", course);
        }
    }

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
            log.error("Cannot fill in template, null pointer in text or map.");
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
        String semester = course.getSemester().toString();
        String courseName = course.getName();

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
     */
    public void sendCourse() {
        if (course == null) {
            log.error("Could not send mail course is null!");
            return;
        }

        MailTemplate template = mailTemplateService.getDefaultTemplate(course);

        if (template == null) {
            log.error("Could not send mail no default template set!");
            return;
        }

        log.debug("Sending mails to course" + course.getName());
        issue(course.getStudents(), template);
    }

    /*
     * Getters and Setters
     */

    /**
     * Gets the course for which the setings overview page gets accessed.
     * @return The course object.
     */
    public Course getCourse() {
        return course;
    }

}
