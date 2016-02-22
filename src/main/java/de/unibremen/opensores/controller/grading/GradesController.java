package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to display the data tables for students and groups.
 * @author Matthias Reichmann
 */
@ManagedBean
@ViewScoped
public class GradesController {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(GradesController.class);

    /**
     * The course which exams get edited.
     */
    private Course course;

    /**
     * Currently logged in user.
     */
    private User user;

    /**
     * Student object of currently logged in user
     */
    private Student student;

    /**
     * Student gradings. Stored here so it won't be loaded multiple times.
     */
    private Map<Long, Grading> studentGradings = new HashMap<>();

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * GradingService for database transactions related to gradings.
     */
    @EJB
    private GradingService gradingService;

    /**
     * GradeService for database transactions related to grades.
     */
    @EJB
    private GradeService gradeService;

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
        final String courseIdString = httpReq.getParameter(Constants.HTTP_PARAM_COURSE_ID);

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

        Object userObj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("user");

        log.debug("Loaded course object: " + course);

        if (course == null || !(userObj instanceof User)) {
            log.debug("trying to redirect to /course/overview");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + Constants.PATH_TO_COURSE_OVERVIEW);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                log.fatal("Could not redirect to " + Constants.PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }
        /*
        Load the user
         */
        user = (User)FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("user");
        /*
        Load the student object
         */
        student = courseService.findStudent(course, user);
        /*
        Load the student gradings
         */
        studentGradings = gradingService.getStudentGradings(student);
    }

    public String getPaboGradeName(final String name) {
        return gradeService.paboGradeDisplayName(name);
    }

    public Course getCourse() {
        return course;
    }

    public Map<Long, Grading> getStudentGradings() {
        return studentGradings;
    }

    public Student getStudent() {
        return student;
    }
}
