package de.unibremen.opensores.controller;


import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.ServerProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.Properties;

/**
 * Password reset token controller.
 *
 * @author Sören Tempel
 */
@ManagedBean
@RequestScoped
public class CertificateController {
    /**
     * The course service.
     */
    @EJB
    private CourseService courseService;

    /**
     * The User service.
     */
    @EJB
    private UserService userService;


    /**
     * The course for which the certs should be created.
     */
    private Course course;

    /**
     * In case only one student is supposed to be returned.
     */
    private Student student;

    /**
     * The FacesContext of this controller.
     */
    private FacesContext context;

    /**
     * Resource bundle.
     */
    private ResourceBundle bundle;

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            CertificateController.class);

    /**
     * The key to acces the state property.
     */
    private final String statePropertyKey = "certcreator.state";

    /**
     * The key to acces the uni property.
     */
    private final String uniPropertyKey = "certcreator.uni";

    /**
     * The state where the software is used.
     */
    private String state;

    /**
     * The uni where the software is being used.
     */
    private String uni;

    /**
     * Executed on request, initializes course and student.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");

        context = FacesContext.getCurrentInstance();

        bundle = ResourceBundle.getBundle("messages",
            context.getViewRoot().getLocale());

        HttpServletRequest request = (HttpServletRequest) context
                .getExternalContext()
                .getRequest();

        String courseId = request.getParameter(
                Constants.HTTP_PARAM_COURSE_ID);
        String studentId = request.getParameter(
                Constants.HTTP_PARAM_STUDENT_ID);

        User user = (User) context.getExternalContext()
                .getSessionMap()
                .get(Constants.SESSION_MAP_KEY_USER);

        Course currentCourse = null;
        if (courseId != null) {
            currentCourse = courseService.findCourseById(courseId);
            if (currentCourse == null) {
                log.debug("Cannot find course" + courseId);
                return;
            }
            log.debug("Got courseId: " + courseId);
        }

        if (!userService.hasCourseRole(user, Role.LECTURER, currentCourse)
                || userService.getLecturer(user, currentCourse).isDeleted()) {
            log.warn("Unprivileged user " + user.getUserId()
                + " tried to create certificates.");
            return;
        } else {
            course = currentCourse;
        }

        if (studentId != null) {
            student = courseService.findStudentById(course, studentId);
            log.debug("Got studentId: " + studentId);
        }

        try {
            Properties props = ServerProperties.getProperties();
            state = props.getProperty(statePropertyKey);
            state += ", " + bundle.getString("common.datearticle");
            uni = props.getProperty(uniPropertyKey);
        } catch (final IOException e) {
            log.error(e);
            state = "";
            uni = "";
        }
    }

    /**
     * Returns a list of students.
     *
     * @return A list of students.
     */
    public List<Student> getStudentList() {
        List<Student> students = null;

        if (course != null && student == null) {
            students = courseService.getStudentList(course);
        } else if (course != null && student != null) {
            students = new ArrayList<Student>();
            students.add(student);
        }

        if (students == null || students.isEmpty()) {
            log.debug("No students found.");
        }

        return (students == null) ? new ArrayList<Student>() : students;
    }

    /**
     * Returns the ParticipationType as a String.
     *
     * @param student the student to be checked.
     *
     * @return the ParticipationType as a String.
     */
    public String getParticipationType(Student student) {
        ParticipationType partType = student.getParticipationType();

        if (partType.getGroupPerformance()) {
            return bundle.getString("participationType.groupPerformance");
        } else {
            return bundle.getString("participationType.singlePerformance");
        }
    }

    /**
     * Returns the credit points as a String.
     *
     * @param student the student to be checked.
     *
     * @return the credit points as a String.
     */
    public String getCreditPoints(Student student) {
        ParticipationType partType = student.getParticipationType();
        Integer cp = partType.getCreditPoints();

        if (cp == null) {
            return course.getDefaultCreditPoints().toString();
        } else {
            return cp.toString();
        }
    }

    /**
     * Returns the sws as a String.
     *
     * @param student the student to be checked.
     *
     * @return the sws as a String.
     */
    public String getSws(Student student) {
        ParticipationType partType = student.getParticipationType();
        String sws = partType.getSws();

        if (sws == null) {
            return course.getDefaultSws().toString();
        } else {
            return sws.toString();
        }
    }

    /**
     * Returns the PerformanceContent as a String.
     *
     * @param student the student to be checked.
     *
     * @return the PerformanceCOntent as a String.
     */
    public String getPerformanceContent(Student student) {
        ParticipationType partType = student.getParticipationType();
        String content = partType.getPerformanceContent();

        if (content == null) {
            return "";
        } else {
            return content;
        }
    }

    /**
     * Returns the PerformanceArea as a String.
     *
     * @param student the student to be checked.
     *
     * @return the PerformanceArea as a String.
     */
    public String getPerformanceArea(Student student) {
        ParticipationType partType = student.getParticipationType();
        String area = partType.getPerformanceArea();

        if (area == null) {
            return "";
        } else {
            return area;
        }
    }

    /**
     * Getter for the state.
     *
     * @return State as string.
     */
    public String getState() {
        return state;
    }

    /**
     * Getter for the uni.
     *
     * @return Uni as string.
     */
    public String getUni() {
        return uni;
    }

    /**
     * Getter for the current course.
     *
     * @return the current course.
     */
    public Course getCourse() {
        return course;
    }
}
