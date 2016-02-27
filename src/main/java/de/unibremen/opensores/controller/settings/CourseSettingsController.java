package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
