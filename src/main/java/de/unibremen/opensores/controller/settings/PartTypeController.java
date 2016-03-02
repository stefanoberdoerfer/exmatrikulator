package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.DateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * The Backing Bean of the Course create page 'Participationtypes'.
 * This controls the third page of the courseCreate-Flow.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class PartTypeController {

    private static Logger log = LogManager.getLogger(PartTypeController.class);

    /**
     * Currently edited ParticipationType.
     */
    private ParticipationType currentPartType;

    /**
     * Flag if an existing ParticipationType or a newly create one is being edited.
     */
    private boolean editmode;

    /**
     * The logged in user.
     */
    private User loggedInUser;

    /**
     * Flag to indicate that this controller was initiated from the courseCreateWizard.
     * No db transactions have to be made if this attribute is 'true'.
     */
    private boolean calledFromWizard;

    /**
     * The FacesContext of the bean, provides ResourceBundles, HTTPRequests etc.
     */
    private FacesContext context;

    /**
     * ResourceBundle for getting localised messages.
     */
    private ResourceBundle bundle;

    /**
     * course to be edited/created.
     */
    private Course course;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;

    /**
     * Initialisation method to insert a default ParticipationType if the current
     * to be created course has no ParticipationTypes.
     */
    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)
                context.getExternalContext().getRequest();

        loggedInUser = (User) context.getExternalContext()
                .getSessionMap().get(Constants.SESSION_MAP_KEY_USER);

        course = courseService.findCourseById(req.getParameter("course-id"));

        updateCurrentPartType();
    }

    /**
     * This method loads the courses ParticipationTypes if the course is not null
     * and inserts a default ParticipationType if the course doesn't have any.
     */
    private void updateCurrentPartType() {
        initNewPartType();
        if (course != null && course.getParticipationTypes().isEmpty()) {
            currentPartType.setIsDefaultParttype(true);
            currentPartType.setName(bundle.getString("participationType.defaultPartType"));
            course.getParticipationTypes().add(currentPartType);
            editmode = true;
        }
    }

    /**
     * This method updates the course in the database when not called from wizard
     * before this view gets destroyed.
     */
    @PreDestroy
    public void preDestroy() {
        log.debug("preDestroy called");
        checkCourseIsNull();

        if (!calledFromWizard) {
            log.debug("updating course from Settings");
            course = courseService.update(course);
            String description = "Participationtypes of this course were edited";
            logService.persist(Log.from(loggedInUser,course.getCourseId(),description));
        }
    }

    /**
     * Adds one ParticipationType to the list if a name was given in the UI and
     * the ParticipationType is not already in the list (editmode).
     */
    public void addPartType() {
        checkCourseIsNull();
        if (currentPartType.getName() == null || currentPartType.getName().trim().isEmpty()) {
            return;
        }

        if (editmode) {
            editmode = false;
        } else {
            if (course.getParticipationTypes().isEmpty()) {
                currentPartType.setIsDefaultParttype(true);
            }
            course.getParticipationTypes().add(currentPartType);
        }
        log.debug("Save ParticipationType: " + currentPartType.getName());
        initNewPartType();
    }

    /**
     * initialises a new Participationtype to be edited from the UI.
     */
    private void initNewPartType() {
        currentPartType = new ParticipationType();
        currentPartType.setCourse(course);
        currentPartType.setIsDefaultParttype(false);
        GradeFormula gradeFormula = new GradeFormula();
        gradeFormula.setFormula(Constants.DEFAULT_GRADE_SCRIPT);
        gradeFormula.setEditDescription(Constants.DEFAULT_SCRIPT_EDIT_MESSAGE);
        gradeFormula.setEditor(loggedInUser);
        gradeFormula.setValid(false);
        gradeFormula.setSaveDate(DateUtil.getDateTime());
        gradeFormula.setParticipationType(currentPartType);
        currentPartType.getGradeFormulas().add(gradeFormula);
    }

    /**
     * Loads the given ParticipationType into the UI to get edited by the user.
     *
     * @param partType ParticipationType to get edited
     */
    public void editPartType(ParticipationType partType) {
        checkCourseIsNull();
        editmode = true;
        currentPartType = partType;
        log.debug("Edit ParticipationType: " + partType.getName());
    }

    /**
     * Removes given ParticipationType from the to be created course.
     *
     * @param partType ParticipationType to get removed from the course
     */
    public void removePartType(ParticipationType partType) {
        checkCourseIsNull();
        course.getParticipationTypes().remove(partType);
        partType.setCourse(null);
        if (editmode) {
            editmode = false;
            initNewPartType();
        }
        log.debug("Removing ParticipationType: " + partType.getName());
    }

    /**
     * Method to redirect if needed Course-object is null. Check can't be placed in
     * the initialisation method because in die CourseCreateWizard the Course-object will
     * get set from a wrapping '-StepController'.
     */
    private void checkCourseIsNull() {
        if (course == null) {
            try {
                ((HttpServletResponse) context.getExternalContext().getResponse())
                        .sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e) {
                log.fatal(e);
            }
        }
    }

    public ParticipationType getCurrentPartType() {
        return currentPartType;
    }

    public void setCurrentPartType(ParticipationType currentPartType) {
        this.currentPartType = currentPartType;
    }

    public boolean isEditmode() {
        return editmode;
    }

    public void setEditmode(boolean editmode) {
        this.editmode = editmode;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
        updateCurrentPartType();
    }

    public void setCalledFromWizard(boolean calledFromWizard) {
        this.calledFromWizard = calledFromWizard;
    }

    public boolean isCalledFromWizard() {
        return calledFromWizard;
    }

    /**
     * Injects the course service to the PartTypeController.
     * @param courseService The course service to be injected to the bean.
     */
    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Injects the logService into PartTypeController.
     * @param logService The logService to be injected.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }
}
