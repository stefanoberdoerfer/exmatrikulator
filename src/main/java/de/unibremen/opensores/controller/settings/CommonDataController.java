package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Field;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * The Backing Bean of the Course create/edit page 'General Data'.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class CommonDataController {

    private static Logger log = LogManager.getLogger(CommonDataController.class);

    /**
     * List of courseNumber strings each wrapped in a Field-object.
     * Wrapper Class needed for dynamic input-field generation.
     */
    private List<Field> courseNumbers;

    /**
     * LecturerSearch resultlist.
     */
    private List<User> searchResultList;

    /**
     * Id of chosen semester.
     */
    private long semesterId;

    /**
     * List of available semesters in Exmatrikulator.
     */
    private List<Semester> semesters;

    /**
     * The logged in user.
     */
    private User loggedInUser;

    /**
     * course to be edited/created.
     */
    private Course course;

    /**
     * The SemesterService for getting all semesters.
     */
    private SemesterService semesterService;

    /**
     * The UserService for searching in all lecturers.
     */
    private UserService userService;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;

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
     * The ResourceBundle to get internationalised messages from.
     */
    private ResourceBundle bundle;

    /**
     * Initialisation method to get semesters from semesterService and
     * add the courseNumber input fields to the UI.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)
                context.getExternalContext().getRequest();

        bundle = ResourceBundle.getBundle("messages",
                context.getViewRoot().getLocale());

        course = courseService.findCourseById(req.getParameter("course-id"));
        log.debug("Loaded course object: " + course);

        loggedInUser = (User) context.getExternalContext()
                .getSessionMap().get(Constants.SESSION_MAP_KEY_USER);

        semesters = semesterService.listSemesters();

        if (course != null) {
            reloadCourseNumbers();
        }
    }

    /**
     * This method hands the given data from the 'General Data' page over to the flowscoped
     * course of the CourseCreateFlowController or updates the course in the database
     * (when not called from wizard) before this view gets destroyed.
     */
    @PreDestroy
    public void preDestroy() {
        log.debug("preDestroy called");
        checkCourseIsNull();

        for (Semester s : semesters) {
            if (s.getSemesterId().equals(semesterId)) {
                course.setSemester(s);
                break;
            }
        }
        updateCourseNumbers();

        if (!calledFromWizard) {
            log.debug("updating course from Settings");
            courseService.update(course);
            String description = "Common Data of this course was edited";
            logService.persist(Log.from(loggedInUser,course.getCourseId(),description));
        }
    }

    /**
     * Reloads the courseNumbers from the to be created course object.
     * This method is needed to update the courseNumbers correctly, if data is
     * updated from the copyCourse-dialog.
     */
    public void reloadCourseNumbers() {
        log.debug("loaded courseNumbers from course object");
        checkCourseIsNull();

        courseNumbers = course.getNumbers().stream()
                .map((Field::new))
                .collect(Collectors.toList());

        if (courseNumbers.isEmpty()) {
            courseNumbers.add(new Field(""));
        }
    }

    /**
     * Updates the courseNumbers attribute of the to be created Course and
     * adds one empty CourseNumber-Field to the UI.
     */
    public void addCourseNumber() {
        checkCourseIsNull();
        updateCourseNumbers();

        if (!courseNumbers.get(courseNumbers.size() - 1).getData().isEmpty()) {
            courseNumbers.add(new Field(""));
        }
    }

    /**
     * Updates Numbers attribute of the course to be created.
     */
    private void updateCourseNumbers() {
        checkCourseIsNull();
        ArrayList<String> list = new ArrayList<>();
        courseNumbers.stream()
                .filter(f -> !f.getData().isEmpty())
                .forEach(f -> list.add(f.getData()));
        course.setNumbers(list);
    }

    /**
     * Searches for users in the whole system who are lecturers with the UserInput query string
     * given via ValueChangeEvent.
     */
    public void searchForLecturers(ValueChangeEvent vce) {
        String query = (String) vce.getNewValue();
        log.debug("searchForLecturers called with input: " + query);
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        searchResultList = userService.searchForLecturers(query);
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

    /**
     * Creates a new Lecturer object with the chosen User and adds this Lecturer to the
     * course if he isn't in there already. Also adds him as a PrivilegedUser to this course.
     * After successful adding, this method removes given user from the searchResultList.
     */
    public void addAsLecturer(User user) {
        if (user == null) {
            return;
        }
        checkCourseIsNull();

        log.debug("addAsLecturer called with user: " + user);

        //check if not already a lecturer or already a deleted lecturer + PrivUser
        Lecturer alreadyLec = course.getLecturerFromUser(user);
        PrivilegedUser alreadyPriv = course.getPrivilegedUserFromUser(user);

        if (alreadyLec != null) {
            alreadyLec.setDeleted(false);
            log.debug("Already a lecturer - setting deleted to false");
        } else {
            Lecturer lec = new Lecturer();
            lec.setUser(user);
            lec.setHidden(false);
            lec.setCourse(course);
            lec.setIsCourseCreator(false);
            course.getLecturers().add(lec);
            log.debug("Created new lecturer");
        }

        if (alreadyPriv != null) {
            alreadyPriv.setDeleted(false);
            log.debug("Already a Tutor - setting deleted to false");
        } else {
            PrivilegedUser privUser = new PrivilegedUser();
            privUser.setUser(user);
            privUser.setHidden(false);
            privUser.setSecretary(false);
            privUser.setCourse(course);
            course.getTutors().add(privUser);
            log.debug("Already new Tutor");
        }

        searchResultList.remove(user);
    }

    /**
     * Removes one Lecturer from the courses list of lecturers and also removes him as
     * a Privileged User.
     */
    public void removeLecturer(Lecturer lecturer) {
        log.debug("Remove Lecturer: " + lecturer.getUser());
        checkCourseIsNull();
        lecturer.setDeleted(true);

        PrivilegedUser pu = course.getPrivilegedUserFromUser(lecturer.getUser());
        if (pu != null) {
            pu.setDeleted(true);
        }

        if (calledFromWizard) {
            log.debug("Real removal (not only deleted = true)");
            course.getLecturers().remove(lecturer);
            if (pu != null) {
                course.getTutors().remove(pu);
            }
        }
    }

    /**
     * Small save method needed to save changes in the courseNumbers when the controller
     * is used from the settings page.
     */
    public void saveCourseNumbers() {
        checkCourseIsNull();
        updateCourseNumbers();
    }

    /**
     * Validates if a shortcut input is valid for a exam.
     * The shortcut of the exam can only contain characters and numerical
     * values and must be at least one character long.
     * It must also be unique in the course, if an other exam has the exact
     * same shortcut, the new shortcut is not valid.
     *
     * @param ctx The FacesContext for which the validation occurs.
     * @param comp The corresponding ui component.
     * @param value The value of the input (the shortcut).
     */
    public void validIdentifier(FacesContext ctx,
                                 UIComponent comp,
                                 Object value) {
        log.debug("validIdentifier() called");

        //skips partial updates of the same page
        if (FacesContext.getCurrentInstance().isPostback()) {
            return;
        }

        List<FacesMessage> msgs = new ArrayList<>();
        if (!(value instanceof String) || ((String)value).trim().isEmpty()) {
            msgs.add(new FacesMessage(bundle.getString("courses.create.messageInputIdentifier")));
            throw new ValidatorException(msgs);
        }

        final String stringValue = (String) value;
        log.debug("Input identifier value: " + stringValue);

        Course otherCourse = courseService.findCourseByIdentifier(stringValue);

        if (otherCourse != null) {
            log.debug("Failing validation for identifier (already taken): " + stringValue);
            msgs.add(new FacesMessage(
                    bundle.getString("courses.create.messageIdentifierTaken")));
            throw new ValidatorException(msgs);
        }

        for (char c: stringValue.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                log.debug("Failing validation for shortcut(characters): " + stringValue);
                msgs.add(new FacesMessage(
                        bundle.getString("courses.create.messageInvalidChar")));
                throw new ValidatorException(msgs);
            }
        }
    }

    /**
     * Injects the logService into CommonDataController.
     * @param logService The logService to be injected.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Injects the user service to the CommonDataController.
     * @param userService The user service to be injected to the bean.
     */
    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Injects the semester service to the CommonDataController.
     * @param semesterService The user service to be injected to the bean.
     */
    @EJB
    public void setSemesterService(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    /**
     * Injects the course service to the ExamController.
     * @param courseService The course service to be injected to the bean.
     */
    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public List<Field> getCourseNumbers() {
        return courseNumbers;
    }

    public void setCourseNumbers(List<Field> courseNumbers) {
        this.courseNumbers = courseNumbers;
    }

    public List<User> getSearchResultList() {
        return searchResultList;
    }

    public void setSearchResultList(List<User> searchResultList) {
        this.searchResultList = searchResultList;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
        reloadCourseNumbers();
    }

    public long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(long semesterId) {
        this.semesterId = semesterId;
    }

    public List<Semester> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<Semester> semesters) {
        this.semesters = semesters;
    }

    public boolean isCalledFromWizard() {
        return calledFromWizard;
    }

    public void setCalledFromWizard(boolean calledFromWizard) {
        this.calledFromWizard = calledFromWizard;
    }
}
