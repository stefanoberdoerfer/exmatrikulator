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
import java.util.stream.Collectors;
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
import javax.validation.constraints.NotNull;

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
     * Name of the current tutorial.
     */
    public String tutorialName;

    /**
     * New tutorial name, used when the tutorial is being edited.
     */
    public String newTutorialName = null;

    /**
     * Comma seperated string of tutor emails for the current tutorial.
     */
    public String tutorialTutors;

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
        tutorial.setName(tutorialName);

        try {
            tutorial = updateTutors(tutorial);
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
     * Changes the current tutorial context.
     *
     * @param tutorial Tutorial to switch to.
     */
    public void changeCurrentTutorial(@NotNull Tutorial tutorial) {
        tutorialName = tutorial.getName();
        tutorialTutors = tutorial.getTutors().stream()
            .map(pu -> pu.getUser().getEmail())
            .collect(Collectors.joining(","));
    }

    /**
     * Edits an existing tutorial.
     */
    public void editTutorial() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        Tutorial tutorial = courseService.findTutorial(course, tutorialName);
        tutorial.setName(newTutorialName);
        newTutorialName = null;

        try {
            tutorial = updateTutors(tutorial);
        } catch (ValidationException e) {
            String fmt = bundle.getString("common.studentDoesNotExist");
            String msg = new MessageFormat(fmt).format(new Object[]{e.toString()});

            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        course.getTutorials().add(tutorial);
        course = courseService.update(course);
    }

    /**
     * Updates tutors for the given tutorial in this course.
     *
     * @param tutorial Tutorial to update tutors for.
     * @return Updated Tutorial.
     * @throws ValidationException If a user with the given email didn't exist.
     */
    private Tutorial updateTutors(Tutorial tutorial) {
        String[] emails = tutorialTutors.split(",");
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

            if (tutorial.getTutors().contains(tutor)) {
                tutor.getTutorials().add(tutorial);
                tutorial.getTutors().add(tutor);
                course.getTutors().add(tutor);
            }
        }

        return tutorial;
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
     * Sets the name for the current tutorial.
     *
     * @param tutorialName Tutorial name.
     */
    public void setTutorialName(String tutorialName) {
        this.tutorialName = tutorialName;
    }

    /**
     * Returns the name of the current tutorial.
     *
     * @return Name of the current tutorial.
     */
    public String getTutorialName() {
        return tutorialName;
    }

    /**
     * Sets the new name for the current tutorial.
     *
     * @param newTutorialName New Tutorial name.
     */
    public void setNewTutorialName(String newTutorialName) {
        this.newTutorialName = newTutorialName;
    }

    /**
     * Returns the new name for the current tutorial.
     *
     * @return New or current tutorial name.
     */
    public String getNewTutorialName() {
        return (newTutorialName == null) ? tutorialName : newTutorialName;
    }

    /**
     * Sets the tutors for the current tutorial.
     *
     * @param tutorialTutors Seperated list of tutor emails.
     */
    public void setTutorialTutors(String tutorialTutors) {
        this.tutorialTutors = tutorialTutors;
    }

    /**
     * Returns the tutors for the current tutorial.
     *
     * @return Comma seperated string of emails.
     */
    public String getTutorialTutors() {
        return tutorialTutors;
    }
}
