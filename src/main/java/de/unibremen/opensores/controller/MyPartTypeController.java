package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * The Backing Bean of the 'My Participationtype' page of a student.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class MyPartTypeController {

    /**
     * The log4j logger.
     */
    private static final Logger log = LogManager.getLogger(MyPartTypeController.class);

    /**
     * The logged in user.
     */
    private User user;

    /**
     * Course containing all possible ParticipationTypes.
     */
    private Course course;

    /**
     * Currently chosen partTypeId.
     */
    private Long chosenPartTypeId;

    /**
     * The ResourceBundle for strings.
     */
    private ResourceBundle bundle;

    /**
     * Student relation of the currently logged in user.
     */
    private Student student;

    private CourseService courseService;
    private StudentService studentService;
    private LogService logService;

    /**
     * Initialisation method for getting the currently logged in user, his course and
     * the corresponding student relation.
     */
    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext exContext = facesContext.getExternalContext();

        HttpServletRequest req = (HttpServletRequest) exContext.getRequest();
        HttpServletResponse res = (HttpServletResponse) exContext.getResponse();

        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        user = (User) exContext.getSessionMap().get(Constants.SESSION_MAP_KEY_USER);
        course = courseService.findCourseById(req.getParameter(Constants.HTTP_PARAM_COURSE_ID));
        if (course == null || user == null) {
            badRequest(res);
            return;
        }

        student = course.getStudentFromUser(user);
        if (student == null) {
            badRequest(res);
            return;
        }

        chosenPartTypeId = student.getParticipationType().getPartTypeId();
    }

    /**
     * Method which gets called after every change in the participationtype selectbox.
     */
    public void partTypeChanged() {
        log.debug("Participationtype changed to id " + chosenPartTypeId);
        ParticipationType partType = getPartTypeByID(chosenPartTypeId);

        if (partType != null && course.getRequiresConfirmation()) {
            log.debug("Show switch warning");
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            bundle.getString("common.warning"),
                            bundle.getString("participationType.my.warning")));
        }
    }

    /**
     * Public method called when a user wishes to save the changes of his participationtype.
     * Does nothing if participationtype didn't change.
     * Sets the user to unconfirmed if the course requires confirmation.
     */
    public void changeParticipationType() {
        log.debug("changeParticipationType called");
        if (chosenPartTypeId.equals(student.getParticipationType().getPartTypeId())) {
            return;
        }

        ParticipationType partType = getPartTypeByID(chosenPartTypeId);

        if (partType != null) {
            student.setParticipationType(partType);
            student.setConfirmed(!course.getRequiresConfirmation());
            student = studentService.update(student);
            log.debug("ParticipationType updated to " + student.getParticipationType().getName());
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "", bundle.getString("participationType.my.success")));
        }
    }

    private ParticipationType getPartTypeByID(Long partTypeId) {
        ParticipationType partType = null;
        for (ParticipationType p : course.getParticipationTypes()) {
            if (p.getPartTypeId().equals(partTypeId)) {
                partType = p;
                break;
            }
        }
        return partType;
    }

    private void badRequest(HttpServletResponse res) {
        try {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            log.fatal(e);
        }
    }

    /**
     * Injects the course service.
     * @param courseService The course service to be injected to the bean.
     */
    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Injects the logService.
     * @param logService The logService to be injected.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Injects the studentService.
     * @param studentService The studentService to be injected.
     */
    @EJB
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Long getChosenPartTypeId() {
        return chosenPartTypeId;
    }

    public void setChosenPartTypeId(Long chosenPartTypeId) {
        this.chosenPartTypeId = chosenPartTypeId;
    }
}