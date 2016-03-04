package de.unibremen.opensores.controller.tutorial;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.TutorialEvent;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.TutorialService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
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
     * The UserService for database transactions related to users.
     */
    private UserService userService;

    /**
     * The schedule model used by PrimeFaces for managing tutorial events.
     */
    private ScheduleModel tutorialEventModel;

    /**
     * The currently selected tutorial event.
     */
    private TutorialEvent event;

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
     * Boolean value whether the loged in user is a lecturer in the tutorial.
     */
    private boolean isUserLecturer;

    /**
     * The old start date of a selected event.
     */
    private Date oldEventStartDate;

    /**
     * The old end date of a selected event.
     */
    private Date oldEventEndDate;

    /**
     * Formats the dates of tutEvents to the timezone of the exmatrikulator.
     */
    private SimpleDateFormat dateFormatter;

    /**
     * Initialises the bean and gets the related tutorial.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
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
        log.debug("Course: " + course);
        log.debug("Tutorial: " + tutorial);

        boolean validationPassed = false;
        try {
            loggedInUser = (User) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(Constants.SESSION_MAP_KEY_USER);
        } catch (ClassCastException e) {
            log.error(e);
        }
        log.debug("Logged in User: " + loggedInUser);
        if (course != null && tutorial != null && loggedInUser != null
               && course.getTutorials().contains(tutorial)) {
            isUserLecturer = userService.hasCourseRole(loggedInUser,
                    Role.LECTURER.name(), course);
            log.debug("Checking if user is tutor");
            isUserTutor = tutorialService.getTutorOf(tutorial, loggedInUser) != null;
            log.debug("User is tutor: " + isUserTutor);
            validationPassed = isUserLecturer || isUserTutor
                    || tutorialService.getStudentOf(tutorial, loggedInUser) != null;

        }

        if (!validationPassed) {
            log.debug("Validation hasn't passed");
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

        for (TutorialEvent event: tutorial.getEvents()) {
            if (isUserTutor && loggedInUser.getUserId() == event.getCreatorId()) {
                event.setEditable(true);
            }
            tutorialEventModel.addEvent(event);
        }

        dateFormatter = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm");
        dateFormatter.setTimeZone(TimeZone.getTimeZone(Constants.SYSTEM_TIMEZONE));

    }

    /**
     * Adds a new event to the tutorial event.
     * @param actionEvent The actionEvent triggered by the PrimeFaces scheduler.
     */
    public void addEvent(ActionEvent actionEvent) {
        log.debug("addEvent called with " + actionEvent);
        if (event.getId() == null) {
            tutorialEventModel.addEvent(event);
            log.debug("Event gets added");
            logEventCreated(event);
        } else {
            tutorialEventModel.updateEvent(event);
            log.debug("Event gets updated");
            if (event.getStartDate() != oldEventStartDate
                    || event.getEndDate() != oldEventEndDate) {
                log.debug("The event dates have changed");
                logEventMoved(event);
                mailEventMoved(event, oldEventStartDate, oldEventEndDate);
            } else {
                logEventUpdated(event);
            }
        }
        updateTutorialEvents();
        event = new TutorialEvent();
    }

    /**
     * Removes the selected tutorial event.
     * @param actionEvent The actionEvent triggered by the PrimeFaces scheduler.
     */
    public void removeEvent(ActionEvent actionEvent) {
        log.debug("removeEvent called with " + actionEvent);
        if (event == null || event.getId() == null) {
            log.debug("The PrimeFaces string id of the event is null");
        } else if (tutorialEventModel.getEvents().contains(event)) {
            log.debug("Removing the event from the EventModel");
            tutorialEventModel.deleteEvent(event);
            logEventRemoved(event);
        }
        updateTutorialEvents();
        event = new TutorialEvent();
    }

    /**
     * Selects an event to the currently edited event.
     * @param selectEvent The selectEvent triggered by the PrimeFaces scheduler.
     */
    public void onEventSelect(SelectEvent selectEvent) {
        log.debug("onEventSelect called with " + selectEvent);
        event = (TutorialEvent) selectEvent.getObject();
        oldEventStartDate = event.getStartDate();
        oldEventEndDate = event.getEndDate();
    }

    /**
     * Triggered when a date is selected. Creates a new tutorialEvent
     * @param selectEvent The selectEvent triggered by the PrimeFaces scheduler.
     */
    public void onDateSelect(SelectEvent selectEvent) {
        log.debug("onDateSelect called with " + selectEvent);
        event = new TutorialEvent(tutorial,
                loggedInUser.getUserId(),
                (Date) selectEvent.getObject(),
                (Date) selectEvent.getObject());
    }


    /**
     * Method triggered when a dialog is cancelled.
     */
    public void onDialogCancel() {
        log.debug("onDialogCancel() called");
    }

    /**
     * Gets the current locale string.
     * @return The current locale string.
     */
    public String getLocaleCountry() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale().toLanguageTag();
    }

    /*
     * Private Methods
     */

    /**
     * Mails to every associated user of the tutorial that an event has been moved.
     * @param event The moved event.
     */
    private void mailEventMoved(TutorialEvent event, Date oldEventStartDate, Date oldEventEndDate) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        String emailFailMessage = bundle.getString("tutEvent.failMessageNoMail");
        for (User user: mailList) {
            try {
                sendEventMoveEvent(user, oldEventStartDate,oldEventEndDate,
                        event.getStartDate(), event.getEndDate());
            } catch (MessagingException | IOException e) {
                log.debug(e);
                addFailMessage(emailFailMessage);
                return;
            }
        }
    }


    /**
     * Sends a newly registered user an email that they are registered now.
     */
    private void sendEventMoveEvent(User user, Date oldStartDate, Date oldEndDate,
                                    Date newStartDate, Date newEndDate)
            throws MessagingException, IOException  {

        String moverName = loggedInUser.getFirstName()
                + loggedInUser.getLastName();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());


        String textFormat = bundle.getString("tutEvent.formatMailEventMoved");
        String text = new MessageFormat(textFormat).format(new Object[]{
                user.getFirstName(), moverName, tutorial.getName(), course.getName(),
                dateFormatter.format(oldStartDate), dateFormatter.format(oldEndDate),
                dateFormatter.format(newStartDate), dateFormatter.format(newEndDate)
        });
        String subjectFormat = bundle.getString("tutEvent.subjectMailEventMoved");
        String subject = new MessageFormat(subjectFormat).format(new Object[] {
                tutorial.getName(), course.getName()
        });
        user.sendEmail(subject, text);
    }

    /**
     * Logs that an tutorial event has been created.
     * @param event The created event.
     */
    private void logEventCreated(TutorialEvent event) {
        String descr = String.format("Has created a tutorial event from %s to "
                + " %s for the tutorial %s in the course %s.",
                event.getStartDate().toString(), event.getEndDate().toString(),
                tutorial.getName(), course.getName());
        logAction(descr);
    }

    /**
     * Logs that an tutorial event has been removed.
     * @param event The removed event.
     */
    private void logEventRemoved(TutorialEvent event) {
        String descr = String.format("Has removed a tutorial event from %s to "
                        + " %s for the tutorial %s in the course %s.",
                event.getStartDate().toString(), event.getEndDate().toString(),
                tutorial.getName(), course.getName());
        logAction(descr);
    }

    /**
     * Logs that an tutorial event has been updated, but the dates havent changed.
     * @param event The removed event.
     */
    private void logEventUpdated(TutorialEvent event) {
        String descr = String.format("Has updated a tutorial event in the tutorial"
                + "%s of the course %s. The dates of the event haven't changed.",
                tutorial.getName(), course.getName());
        logAction(descr);
    }


    /**
     * Logs that an tutorial event has been moved (the dates of the event have changed).
     * @param event The removed event.
     */
    private void logEventMoved(TutorialEvent event) {
        String descr = String.format("Has moved a tutorial event to the dates from %s to "
                        + " %s for the tutorial %s of the course %s."
                        + " The old dates were: From %s to %s",
                        dateFormatter.format(event.getStartDate()),
                        dateFormatter.format(event.getEndDate()),
                        tutorial.getName(), course.getName(),
                        dateFormatter.format(oldEventStartDate),
                        dateFormatter.format(oldEventEndDate));
        logAction(descr);
    }

    /**
     * Adds a fail message to the FacesContext.
     * @param message the message to be displayed.
     */
    private void addFailMessage(String message) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_FATAL,
                bundle.getString("common.error"),
                message));
    }

    /**
     * Logs that an action has occured with the logged in user and the selected
     * course.
     * @param description The description of the action
     */
    private void logAction(String description) {
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
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

    /**
     * Checks whether the logged in user can edit the current event.
     * The event can be edited if the user is a tutor and has the same user id
     * as the creator id of the event.
     * @return True if the user has the rights to edit the tutorial, false otherwise.
     */
    public boolean canUserEditEvent() {
        return canUserEditEvent(event);
    }

    /**
     * Checks whether the logged in user can edit the current event.
     * The event can be edited if the user is a tutor and has the same user id
     * as the creator id of the event.
     * @param event The tutorial event which should be checked for.
     * @return True if the user has the rights to edit the tutorial, false otherwise.
     */
    public boolean canUserEditEvent(TutorialEvent event) {
        return event != null && (isUserTutor || isUserLecturer)
                && (event.getId() == null
                || loggedInUser.getUserId() == event.getCreatorId());
    }

    /**
     * Updates the events of the tutorial in the database.
     */
    private void updateTutorialEvents() {
        tutorial.getEvents().clear();
        for (ScheduleEvent event: tutorialEventModel.getEvents()) {
            TutorialEvent tutEvent = (TutorialEvent) event;
            tutEvent.setEditable(false);
            tutorial.getEvents().add(tutEvent);
        }
        tutorial = tutorialService.update(tutorial);
        for (ScheduleEvent scheduleEvent: tutorialEventModel.getEvents()) {
            TutorialEvent tutEvent = (TutorialEvent) scheduleEvent;
            tutEvent.setEditable(canUserEditEvent(tutEvent));
        }
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

    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
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

    public Course getCourse() {
        return course;
    }

    public TutorialEvent getEvent() {
        return event;
    }

    public void setEvent(TutorialEvent event) {
        this.event = event;
    }

    public boolean isUserLecturer() {
        return isUserLecturer;
    }

}
