package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matthias Reichmann
 */
@ManagedBean
@ViewScoped
public class GradingController {
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
    private static Logger log = LogManager.getLogger(GradingController.class);

    /**
     * The course which exams get edited.
     */
    private Course course;
    /**
     * Student gradings. Stored here so it won't be loaded multiple times.
     */
    private Map<Student, Map<Exam, Grading>> studentGradings = new HashMap<>();

    /**
     * String to search for
     */
    private String searchValue;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private GradingService gradingService;
    /**
     * Method called after initialisation.
     * Gets the corresponding course from the http param.
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
            course = courseService.findById(courseId);
        }

        log.debug("Loaded course object: " + course);

        if (course == null) {
            log.debug("trying to redirect to /course/overview");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + PATH_TO_COURSE_OVERVIEW);
            } catch (IOException e) {
                e.printStackTrace();
                log.fatal("Could not redirect to " + PATH_TO_COURSE_OVERVIEW);
            }
        }
    }

    /**
     * Returns the course.
     *
     * @return Course
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Returns a list of all students of the opened course.
     *
     * @return List of students or null
     */
    public List<Student> getStudents() {
        if (searchValue != null && searchValue.trim().length() > 0) {
            log.debug("Search for " + searchValue);
            return gradingService.getStudents(course, searchValue.trim());
        }
        else {
            return gradingService.getStudents(course);
        }
    }

    /**
     * Returns the student gradings for a student. Uses map because gradings
     * are only stored for graded exams. But we want all exams to show up.
     * @param s Student whose gradings shall be loaded
     * @return Map of exams with gradings
     */
    public Map<Exam, Grading> getStudentGradings(Student s) {
        log.debug("load student gradings for student "
                + s.getUser().getFirstName());

        Map<Exam, Grading> gradings = studentGradings.get(s);

        log.debug("Gradings is " +
                (gradings == null ? "not loaded" : "already loaded"));

        if (gradings == null) {
            gradings = gradingService.getStudentGradings(course, s);
            log.debug("Loaded gradings is " +
                    (gradings == null ? "null" : "not null"));
            studentGradings.put(s, gradings);
        }

        return gradings;
    }

    public void setSearchValue(String search) {
        this.searchValue = search;
    }

    public String getSearchValue() {
        return this.searchValue;
    }
}
