package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.ExamEvent;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.ExamService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.DateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * The managed bean to manage exam events which are used for exam appointments
 * for examinations with attendance.
 * @author Kevin Scheck
 */
@ManagedBean
@ViewScoped
public class ExamEventController {

    /**
     * The log4j logger for debug, errors logs from log4j.
     * These logs are not related to actions in the exmatrikulator business domain.
     */
    private static Logger log = LogManager.getLogger(ExamEventController.class);

    /**
     * The currently selected exam event.
     */
    private ExamEvent event;

    /**
     * The LogService for adding logs related to the exmatrikulator actions.
     */
    private LogService logService;

    /**
     * The CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The UserService for database transactions related to users.
     */
    private UserService userService;

    /**
     * The ExamService for database transactions related to exams.
     */
    private ExamService examService;

    /**
     * The schedule model used by PrimeFaces for managing exam events.
     */
    private ScheduleModel examEventModel;

    /**
     * The StudentService for database transactions related to students.
     */
    private StudentService studentService;

    /**
     * The PrivilegedUserService for database transactions related to PrivilegedUsers.
     */
    private PrivilegedUserService privilegedUserService;

    /**
     * The course in which the exam is located.
     */
    private Course course;

    /**
     * The currently logged in user user the page backed by this bean.
     */
    private User loggedInUser;

    /**
     * The exam which events should be managed.
     */
    private Exam exam;

    /**
     * Boolean whether the logged in user is a student in this course.
     */
    private boolean isUserStudent;

    /**
     * Boolean whether the logged in user is a privileged user in this course.
     */
    private boolean isUserPrivUser;

    /**
     * Boolean whether the the logged in user is a lecturer.
     */
    private boolean isUserLecturer;

    /**
     * The Student object of the logged in user if the user is a student.
     */
    private Student studentUser;

    /**
     * Boolean value which checks if a student is registered to any examEvent
     * of this exam. Is not used when the logged in user is not a student.
     */
    private boolean studentRegisteredToExam;

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

    /*
     * Public methods
     */

    /**
     * Method called when the bean is initialized.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        HttpServletRequest req =
                (HttpServletRequest) FacesContext.getCurrentInstance()
                        .getExternalContext().getRequest();
        course = courseService.findCourseById(
                req.getParameter(Constants.HTTP_PARAM_COURSE_ID));
        exam = examService.findExamById(
                req.getParameter(Constants.HTTP_PARAM_EXAM_ID));
        log.debug("Course: " + course);
        log.debug("Exam: " + exam);

        dateFormatter = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm");
        dateFormatter.setTimeZone(TimeZone.getTimeZone(Constants.SYSTEM_TIMEZONE));
        boolean validationPassed = false;
        try {
            loggedInUser = (User) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(Constants.SESSION_MAP_KEY_USER);
        } catch (ClassCastException e) {
            log.error(e);
        }
        if (loggedInUser != null && course != null && exam != null
                && course.getExams().contains(exam)) {
            isUserLecturer = userService.hasCourseRole(loggedInUser, Role.LECTURER, course);

            log.debug("User is lecturer: " + isUserLecturer);
            if (!isUserLecturer) {
                log.debug("User is not a lecturer");
                PrivilegedUser privilegedUser = courseService
                        .findPrivileged(course, loggedInUser.getEmail());
                log.debug("Found privilegedUser: " + privilegedUser);
                isUserPrivUser = privilegedUser != null && privilegedUser
                        .hasPrivilege(Privilege.CreateExamEvents);
                log.debug("User is privileged user: " + isUserPrivUser);

                if (!isUserPrivUser) {
                    studentUser = courseService.findStudent(course, loggedInUser.getEmail());
                    log.debug("Found student in course: " + studentUser);
                    validationPassed = isUserStudent = studentUser != null;
                } else {
                    validationPassed = true;
                }
            } else {
                validationPassed = true;
            }
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
                log.error(e);
                log.fatal("Could not redirect to " + Constants.PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }

        if (examEventModel == null) {
            log.debug("examEventModel gets created");
            examEventModel = new DefaultScheduleModel();
        }
        for (ExamEvent examEvent: exam.getEvents()) {
            examEventModel.addEvent(examEvent);
        }
    }

    /**
     * Selects an event to the currently edited event.
     * @param selectEvent The selectEvent triggered by the PrimeFaces scheduler.
     */
    public void onEventSelect(SelectEvent selectEvent) {
        log.debug("onEventSelect called with " + selectEvent);
        event = (ExamEvent) selectEvent.getObject();
        oldEventStartDate = event.getStartDate();
        oldEventEndDate = event.getEndDate();
    }



    /**
     * Triggered when a date is selected. Creates a new ExamEvent
     * @param selectEvent The selectEvent triggered by the PrimeFaces scheduler.
     */
    public void onDateSelect(SelectEvent selectEvent) {
        log.debug("onDateSelect called with " + selectEvent);
        event = new ExamEvent(exam,
                (Date) selectEvent.getObject(),
                (Date) selectEvent.getObject());
    }


    /**
     * Adds a new event to the exam event.
     * @param actionEvent The actionEvent triggered by the PrimeFaces scheduler.
     */
    public void addEvent(ActionEvent actionEvent) {
        log.debug("addEvent called with " + actionEvent);
        if (event.getId() == null) {
            examEventModel.addEvent(event);
            log.debug("Event gets added");
            logEventCreated(event);
        } else {
            examEventModel.updateEvent(event);
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
        updateExamEvents();
        event = createDefaultEvent();
    }

    /**
     * Removes the selected exam event.
     * @param actionEvent The actionEvent triggered by the PrimeFaces scheduler.
     */
    public void removeEvent(ActionEvent actionEvent) {
        log.debug("removeEvent called with " + actionEvent);
        if (event.getId() == null) {
            log.debug("The PrimeFaces string id of the event is null");
        } else if (examEventModel.getEvents().contains(event)) {
            log.debug("Removing the event from the EventModel");

            examEventModel.deleteEvent(event);
            PrivilegedUser examiner = event.getExaminer();
            examiner.getExamEvents().remove(event);
            privilegedUserService.update(examiner);

            List<Student> oldGradedStudents = event.getExaminedStudents();
            for (Student student: oldGradedStudents) {
                student.getExamEvents().remove(event);
                studentService.update(student);
            }
            logEventRemoved(event);
        }
        updateExamEvents();
        event = createDefaultEvent();
    }

    /**
     * Registers the current user as student to the selected exam event.
     * This method is called when the logged in user is a student and and hasn't
     * registered to an event of the exam yet.
     */
    public void registerToExamEvent() {
        if (!isUserStudent()) {
            log.error("RegisterToExamEvent called but user is not a student.\n Doing nothing.");
            return;
        }
        if (event.isFull()) {
            log.error("RegisterToExamEvent called but the event is full. \n Doing nothing.");
            return;
        }
        if (studentRegisteredToExam) {
            log.error("RegisterToExamEvent called but student is already "
                    + "registered to an event of this exam. \n Doing nothing.");
            return;
        }

        event.getExaminedStudents().add(studentUser);
        studentUser.getExamEvents().add(event);
        updateExamEvents();
        studentUser = studentService.update(studentUser);
        studentRegisteredToExam = true;
        logStudentRegisteredToEvent(event);
        event = createDefaultEvent();
    }

    /**
     * Unregisters the current user as student from the selected exam event.
     * This method is called when the logged in user is a student and and has registered
     * to the selected event.
     */
    public void unregisterFromExamEvent() {
        if (!isUserStudent()) {
            log.error("RegisterToExamEvent called but user is not a student.\nDoing nothing.");
            return;
        }
        if (event.getExaminedStudents().contains(studentUser)) {
            event.getExaminedStudents().remove(studentUser);
            studentUser.getExamEvents().remove(event);
            updateExamEvents();
            studentUser = studentService.update(studentUser);
            studentRegisteredToExam = false;
            logStudentUnregisteredFromEvent(event);
            event = createDefaultEvent();
        } else {
            log.error("Called unregisterFromExamEvent() even though student is"
                    + " not registered.\nDoing nothing");
        }
    }

    /**
     * Checks whether the logged in user can edit the current exam event.
     * The event can be edited if the user is a lecturer or is the examiner
     * of the exam event.
     * @return True if the user has
     */
    public boolean canUserEditEvent() {
        return canUserEditEvent(event);
    }

    /**
     * Checks whether the logged in user can edit the current exam event.
     * The event can be edited if the user is a lecturer or is the examiner
     * of the exam event.
     * @return True if the user has
     */
    public boolean canUserEditEvent(ExamEvent event) {
        boolean canUserEditEvents = event != null && (isUserPrivUser || isUserLecturer)
                //TODO Change here if lecturer can also edit other events
                && (event.getId() == null
                || loggedInUser.equals(event.getExaminer().getUser()));
        return canUserEditEvents;
    }

    /**
     * Validates that the end date of the selected event is after the start date.
     * @pre event Is not null and has a not null startDate
     * @param context The FacesContext in which the validation is done.
     * @param component The UIComponent for which the validation is done.
     * @param value The value of the end date
     * @throws ValidatorException If the input string doesnt match the First name followed
     *                            by the last name of the user of the to be
     *                            deleted participation class.
     */
    public void validateEndDateAfterStartDate(FacesContext context,
                                              UIComponent component,
                                              Object value) {

        log.debug("validateEndDateAfterStartDate called with : " + value);

        List<FacesMessage> messages = new ArrayList<>();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        addFailMessage(bundle.getString("tutEvent.validatorMessageEndDate"));

        if (!(value instanceof Date) || event.getStartDate() == null) {
            log.debug("Returning.. value instance of Date? " + (value instanceof Date)
                    + " is start date null? " + event.getStartDate());
            return;
        }

        Date endDate = (Date) value;
        log.debug("End Date: " + endDate);
        log.debug("Start Date: " + event.getStartDate());
        if (!endDate.after(event.getStartDate())) {
            throw new ValidatorException(messages);
        }
        log.debug("Date is valid");
    }

    /**
     * Checks if the deadline is due for registration to
     * @return True if the deadline is over, false otherwise.
     */
    public boolean isDeadlineDue() {
        return exam.getDeadline() != null && DateUtil.getDateTime().after(exam.getDeadline());
    }

    /**
     * Gets the current locale string.
     * @return The current locale string.
     */
    public String getLocaleCountry() {
        String locale = FacesContext.getCurrentInstance().getViewRoot().getLocale().toLanguageTag();
        return locale;
    }


    /**
     * Gets a list of students of the current course which have no events in this exam.
     * @return The list of students with no events.
     */
    public List<Student> getStudentsWithoutEvents() {
        return course.getStudents().stream().filter(
            student ->
            {
                for (ExamEvent event : student.getExamEvents()) {
                    if (event.getExam().equals(exam)) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
    }

    /*
     * Private methods
     */

    /**
     * Creates an exam event with the current exam as exam property.
     * @return The created exam.
     */
    private ExamEvent createDefaultEvent() {
        ExamEvent event = new ExamEvent();
        event.setExam(exam);
        return event;
    }

    /**
     * Mails to every associated user of the exam that an event has been moved.
     * @param event The moved event.
     */
    private void mailEventMoved(ExamEvent event, Date oldEventStartDate, Date oldEventEndDate) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        String emailFailMessage = bundle.getString("tutEvent.failMessageNoMail");

        for (User user: getMailListOfExamEvent(event)) {
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

    private List<User> getMailListOfExamEvent(ExamEvent examEvent) {
        List<User> users = examEvent.getExaminedStudents().stream()
                .map(student -> student.getUser())
                .collect(Collectors.toList());
        users.add(examEvent.getExaminer().getUser());
        users.remove(loggedInUser);
        return users;
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


        String textFormat = bundle.getString("examEvent.formatMailEventMoved");
        String text = new MessageFormat(textFormat).format(new Object[]{
                user.getFirstName(), moverName, exam.getName(), course.getName(),
                dateFormatter.format(oldStartDate), dateFormatter.format(oldEndDate),
                dateFormatter.format(newStartDate), dateFormatter.format(newEndDate)
        });
        String subjectFormat = bundle.getString("examEvent.subjectMailEventMoved");
        String subject = new MessageFormat(subjectFormat).format(new Object[] {
                exam.getName(), course.getName()
        });
        user.sendEmail(subject, text);
    }

    /**
     * Updates the events of the exam in the database.
     */
    private void updateExamEvents() {
        log.debug("updateExamEvents() called");
        exam.getEvents().clear();
        for (ScheduleEvent scheduleEvent: examEventModel.getEvents()) {
            ExamEvent examEvent = (ExamEvent) scheduleEvent;
            log.debug("Exam event with start date: " + examEvent.getStartDate()
                    + " and end date: " + examEvent.getEndDate());
            examEvent.setEditable(false);
            exam.getEvents().add(examEvent);
        }
        exam = examService.update(exam);
        for (ScheduleEvent scheduleEvent: examEventModel.getEvents()) {
            ExamEvent examEvent = (ExamEvent) scheduleEvent;
            examEvent.setEditable(canUserEditEvent(examEvent));
        }
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


    /*
     * Logs
     */

    /**
     * Logs that an exam event has been created.
     * @param event The created event.
     */
    private void logEventCreated(ExamEvent event) {
        String descr = String.format("Has created an exam event from %s to "
                        + " %s for the exam %s in the course %s (Student capacity: %d).",
                event.getStartDate().toString(), event.getEndDate().toString(),
                exam.getName(), course.getName(), event.getMaxNumStudents());
        logAction(descr);
    }

    /**
     * Logs that an exam event has been removed.
     * @param event The removed event.
     */
    private void logEventRemoved(ExamEvent event) {
        String descr = String.format("Has removed an exam event from %s to "
                        + " %s for the exam %s in the course %s (Student capacity: %d)",
                event.getStartDate().toString(), event.getEndDate().toString(),
                exam.getName(), course.getName(), event.getMaxNumStudents());
        logAction(descr);
    }

    /**
     * Logs that an exam event has been updated, but the dates havent changed.
     * @param event The removed event.
     */
    private void logEventUpdated(ExamEvent event) {
        String descr = String.format("Has updated an exam event in exam"
                        + "%s of the course %s. The dates of the event haven't changed."
                        + "New student capacity: %d",
                exam.getName(), course.getName(), event.getMaxNumStudents());
        logAction(descr);
    }

    /**
     * Logs that an exam event has been moved (the dates of the event have changed).
     * @param event The removed event.
     */
    private void logEventMoved(ExamEvent event) {
        String descr = String.format("Has moved an exam event to the dates from %s to "
                        + " %s for the exam %s of the course %s."
                        + " The old dates were: From %s to %s",
                dateFormatter.format(event.getStartDate()),
                dateFormatter.format(event.getEndDate()),
                exam.getName(), course.getName(),
                dateFormatter.format(oldEventStartDate),
                dateFormatter.format(oldEventEndDate));
        logAction(descr);
    }

    /**
     * Logs that a student has unregistered from an exam event.
     * @param event The exam event from which the student has unregistered.
     */
    private void logStudentRegisteredToEvent(ExamEvent event) {
        String descr = String.format("The student %s has registered to the exam event "
                        + "%s of the exam %s of the course %s",
                loggedInUser, event.getTitle(), exam.getName(), course.getName());
        logAction(descr);
    }

    /**
     * Logs that a student has unregistered from an exam event.
     * @param event The exam event from which the student has unregistered.
     */
    private void logStudentUnregisteredFromEvent(ExamEvent event) {
        String descr = String.format("The student %s has unregistered from the exam event "
                        + "%s of the exam %s of the course %s",
                loggedInUser, event.getTitle(), exam.getName(), course.getName());
        logAction(descr);
    }

    /**
     * Logs that an action has occured with the logged in user and the selected
     * course.
     *
     * @param description The description of the action
     */
    private void logAction(String description) {
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
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

    @EJB
    public void setExamService(ExamService examService) {
        this.examService = examService;
    }

    @EJB
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    @EJB
    public void setPrivilegedUserService(PrivilegedUserService privilegedUserService) {
        this.privilegedUserService = privilegedUserService;
    }

    public void setExamEventModel(ScheduleModel eventModel) {
        this.examEventModel = eventModel;
    }

    public ScheduleModel getExamEventModel() {
        return examEventModel;
    }

    public boolean isUserStudent() {
        return isUserStudent;
    }

    public boolean isUserPrivUser() {
        return isUserPrivUser;
    }

    public boolean isUserLecturer() {
        return isUserLecturer;
    }

    public ExamEvent getEvent() {
        return event;
    }

    public void setExamEvent(ExamEvent event) {
        this.event = event;
    }

    public Course getCourse() {
        return course;
    }

}
