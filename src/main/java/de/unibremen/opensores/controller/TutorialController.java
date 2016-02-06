package de.unibremen.opensores.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.CourseService;

import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.io.Serializable;
import java.text.MessageFormat;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

/**
 * Controller for managing tutorials.
 *
 * @author Sören Tempel
 */
@ManagedBean
@ViewScoped
public class TutorialController implements Serializable {
    /**
     * Unique serial version uid.
     */
    private static final long serialVersionUID = -626531390355925094L;

    /**
     * The log4j logger.
     */
    private Logger log = LogManager.getLogger(TutorialController.class);

    /**
     * The user service for connection to the database.
     */
    @EJB
    private transient UserService userService;

    /**
     * The course service for connection to the database.
     */
    @EJB
    private transient CourseService courseService;

    /**
     * Name for a new tutorial.
     */
    public String createTutorialName;

    /**
     * Comma seperated string of tutor emails;
     */
    public String createTutorialTutors;

    /**
     * Course for this tutorial.
     */
    private transient Course course;

    /**
     * Method called on bean initialization.
     */
    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        HttpServletRequest req = (HttpServletRequest)
            facesContext.getExternalContext().getRequest();
        String idStr = req.getParameter("course-id");

        try {
            course = (idStr == null || idStr.trim().isEmpty()) ? null : courseService
                .find(Course.class, Integer.valueOf(idStr).longValue());
        } catch (NumberFormatException e) {
            course = null;
        }

        /*
         * TODO verify that the user is allowed to access this course
         */

        if (course == null) {
            String msg = bundle.getString("courses.fail");
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));

            /*
             * TODO decide where the user should be redirected to.
             */

            return;
        }
    }

    /**
     * Creates a new tutorial.
     */
    public void createTutorial() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        if (course == null) {
            String msg = bundle.getString("courses.fail");
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        Tutorial tutorial = new Tutorial();
        tutorial.setCourse(course);
        tutorial.setName(createTutorialName);

        try {
            updateTutors(tutorial);
        } catch (ValidationException e) {
            String fmt = bundle.getString("common.studentDoesNotExist");
            String msg = new MessageFormat(fmt).format(new Object[]{e.toString()});

            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
             return;
        }

        course.getTutorials().add(tutorial);
        course = courseService.update(course);

        log.debug("Created new tutorial " + tutorial.getName());
    }

    /**
     * Edits an existing tutorial.
     */
    public void editTutorial() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        Tutorial tutorial = courseService.findTutorial(course, createTutorialName);
        tutorial.setName(createTutorialName);

        try {
            updateTutors(tutorial);
        } catch (ValidationException e) {
            String fmt = bundle.getString("common.studentDoesNotExist");
            String msg = new MessageFormat(fmt).format(new Object[]{e.toString()});

            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
             return;
        }
    }

    /**
     * Updates tutors for the given tutorial in this course.
     *
     * @param tutorial Tutorial to update tutors for.
     * @throws ValidationException If a user with the given email didn't exist.
     */
    private void updateTutors(Tutorial tutorial) {
        String[] emails = createTutorialTutors.split(",");
        for (String email : emails) {
            email = email.trim();
            User user = userService.findByEmail(email);
            if (user == null) {
                throw new ValidationException(email);
            }

            PrivilegedUser tutor = courseService.findTutor(course, user);
            if (tutor == null) {
                log.debug("Made user with email " + user.getEmail() + " a tutor");
                tutor = new PrivilegedUser();

                tutor.setSecretary(false);
                tutor.setCourse(course);
                tutor.setUser(user);
                tutor.setHidden(false);
            }

            tutor.getTutorials().add(tutorial);
            tutorial.getTutors().add(tutor);
            course.getTutors().add(tutor);
        }
    }

    /**
     * Returns a list of all tutorials for this course.
     *
     * @return List of tutorials or null if non exist.
     */
    public List<Tutorial> getTutorials() {
        return (course == null) ? null : course.getTutorials();
    }

    /**
     * Sets the course for this tutorial.
     *
     * @param course Course to use.
     */
    public void setCourse(Course course) {
        this.course = course;
    }

    /**
     * Returns the course for this tutorial.
     *
     * @return Course for this tutorial.
     */
    public Course getCourse() {
        return this.course;
    }

    /**
     * Sets the name for a new tutorial.
     *
     * @param createTutorialName Tutorial name.
     */
    public void setCreateTutorialName(String createTutorialName) {
        this.createTutorialName = createTutorialName;
    }

    /**
     * Returns the name for a new tutorial.
     *
     * @return Name for a new tutorial.
     */
    public String getCreateTutorialName() {
        return createTutorialName;
    }

    /**
     * Sets the tutors for a new tutorial.
     *
     * @param createTutorialTutors Tutor name.
     */
    public void setCreateTutorialTutors(String createTutorialTutors) {
        this.createTutorialTutors = createTutorialTutors;
    }

    /**
     * Returns the tutors for a new tutorial.
     *
     * @return Comma seperated string of emails.
     */
    public String getCreateTutorialTutors() {
        return createTutorialTutors;
    }
}
