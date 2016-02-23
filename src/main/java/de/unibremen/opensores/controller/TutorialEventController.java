package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.TutorialEvent;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.TutorialService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The controller for managing tutorial events.
 * @author Kevin Scheck.
 */
@ManagedBean
@ViewScoped
public class TutorialEventController {

    /**
     * The log4j logger for debug, errors logs from log4j.
     * These logs are not related to actions in the exmatrikulator business domain.
     */
    private static Logger log = LogManager.getLogger(TutorialEventController.class);

    /**
     * The tutorial which events get edited.
     */
    private Tutorial tutorial;

    /**
     * The course in which the tutorial is located.
     */
    private Course course;

    /**
     * The currently logged in user user the page backed by this bean.
     */
    private User loggedInUser;

    /**
     * The LogService for adding logs related to the exmatrikulator actions.
     */
    private LogService logService;

    /**
     * The TutorialService for database transactions related to tutorials.
     */
    private TutorialService tutorialService;

    /**
     * The CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The schedule model used by PrimeFaces for managing tutorials.
     */
    private ScheduleModel tutorialEventModel;

    /**
     * The currently selected tutorial event.
     */
    private TutorialEvent selectedTutorialEvent;

    /**
     * A list of users which get notified by mail that a tutorialEvent has changed.
     * Includes all tutors and students from the tutorial excluding the logged in user.
     */
    private List<User> mailList;

    /**
     * Boolean value whether the logged in user is a tutor in the tutorial.
     */
    private boolean isUserTutor;

    /**
     * Initialises the bean and gets the related tutorial.
     */
    @PostConstruct
    public void init() {
        if (tutorialEventModel == null) {
            tutorialEventModel = new DefaultScheduleModel();
        }
        HttpServletRequest req =
                (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        course = courseService.findCourseById(
                req.getParameter(Constants.HTTP_PARAM_COURSE_ID));
        tutorial = tutorialService.findTutorialById(
                req.getParameter(Constants.HTTP_PARAM_TUTORIAL_ID));

        boolean validationPassed = false;
        if (course != null && tutorial != null && course.getTutorials().contains(tutorial)) {
            isUserTutor = tutorialService.getTutorOf(tutorial, loggedInUser) != null;
            validationPassed = isUserTutor
                    || tutorialService.getStudentOf(tutorial, loggedInUser) != null;
        }

        if (!validationPassed) {
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

        mailList = getMailList();
    }

    /**
     * Adds a new event to the tutorial event.
     * @param actionEvent The actionEvent triggered by the PrimeFaces scheduler.
     */
    public void addEvent(ActionEvent actionEvent) {
        log.debug("addEvent called with " + actionEvent);
        if (selectedTutorialEvent.getId() == null) {
            tutorialEventModel.addEvent(selectedTutorialEvent);
        } else {
            tutorialEventModel.updateEvent(selectedTutorialEvent);

        }
        selectedTutorialEvent = new TutorialEvent();
    }

    /**
     * Selects an event to the currently edited event.
     * @param selectEvent The selectEvent triggered by the PrimeFaces scheduler.
     */
    public void onEventSelect(SelectEvent selectEvent) {
        log.debug("onEventSelect called with " + selectEvent);
        selectedTutorialEvent = (TutorialEvent) selectEvent.getObject();
    }

    /**
     * Triggered when a date is selected. Creates a new tutorialEvent
     * @param selectEvent The selectEvent triggered by the PrimeFaces scheduler.
     */
    public void onDateSelect(SelectEvent selectEvent) {
        log.debug("onDateSelect called with " + selectEvent);
        selectedTutorialEvent = new TutorialEvent(tutorial,
                (Date) selectEvent.getObject(),
                (Date) selectEvent.getObject());
    }

    /**
     * Triggered when an event is moved.
     * @param event The ScheduleEntryMoveEvent which is triggered by the PrimeFaces
     *              scheduler.
     */
    public void onEventMove(ScheduleEntryMoveEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Event moved", "Day delta:" + event.getDayDelta() + ","
                + " Minute delta:" + event.getMinuteDelta());
        log.debug(event.getScheduleEvent().getTitle());
        addMessage(message);
    }

    /**
     * Triggered when an event time duration is resized.
     * @param event The ScheduleEntryResizeEvent triggered by the PrimeFaces scheduler
     */
    public void onEventResize(ScheduleEntryResizeEvent event) {
        log.debug("onEventResize called with " + event);
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized",
                "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());
        addMessage(message);
    }

    /*
     * Private Methods
     */

    private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    /**
     * Gets a List of Users which are not the logged in user from the tutorial.
     * @return The list of users which are not the logged in user.
     */
    private List<User> getMailList() {
        return Stream.concat(
                    tutorial.getStudents().stream().map(student -> student.getUser()),
                    tutorial.getTutors().stream().map(tutor -> tutor.getUser()))
                .filter(user -> !user.equals(loggedInUser))
                .collect(Collectors.toList());

    }

    /*
     * Getters and Setters
     */

    @EJB
    public void setTutorialService(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public ScheduleModel getTutorialEventModel() {
        return tutorialEventModel;
    }

    public void setTutorialEventModel(ScheduleModel tutorialEventModel) {
        this.tutorialEventModel = tutorialEventModel;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public Tutorial getTutorial() {
        return tutorial;
    }

    public void setTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

    public boolean isUserTutor() {
        return isUserTutor;
    }
}
