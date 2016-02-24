package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LecturerService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Backing Bean of the Course cverview page. Covers logic which shouldn't
 * be implemented in UserController.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class CourseOverviewController {

    private static Logger log = LogManager.getLogger(CourseOverviewController.class);

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;

    /**
     * Services to update User->Course relation classes.
     */
    private LecturerService lecturerService;
    private PrivilegedUserService privilegedUserService;
    private StudentService studentService;

    /**
     * String typed in by the user to find courses.
     */
    private String courseSearchInput;

    /**
     * Resultlist of courses for handling search.
     */
    private List<Course> searchResultList;

    /**
     * Selected course to join.
     */
    private Course selectedCourse;

    /**
     * Course to leave.
     */
    private Course courseToLeave;

    /**
     * Id of chosen ParticipationType.
     */
    private long chosenPartTypeId;

    /**
     * Newly to be created student for joining a course.
     */
    private Student newStudent;

    /**
     * The logged in user.
     */
    private User loggedInUser;

    /**
     * Sessionscoped Usercontroller injected here to simplify
     * synchronisation of active courses.
     */
    @ManagedProperty(value = "#{userController}")
    private UserController userController;

    /**
     * Initialisation method to get the currently logged in user.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");

        loggedInUser = (User) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(Constants.SESSION_MAP_KEY_USER);
    }

    /**
     * Sets the relation between the given course and the logged in user
     * to the value given with the boolean flag 'hide'.
     * @param course Course to hide/show
     * @param hide flag to indicate if course should
     *             be hidden (true) or shown again (false)
     */
    public void setCourseHiddenStatus(Course course, boolean hide) {
        log.debug("Setting hidden course " + course.getName() + "  to " + hide);
        Lecturer lec = course.getLecturerFromUser(loggedInUser);
        if (lec != null) {
            lec.setHidden(hide);
            log.debug("Lecturer relation updated");
            lecturerService.update(lec);
        }

        PrivilegedUser priv = course.getPrivilegedUserFromUser(loggedInUser);
        if (priv != null) {
            priv.setHidden(hide);
            log.debug("Privileged user relation updated");
            privilegedUserService.update(priv);
        }

        Student stud = course.getStudentFromUser(loggedInUser);
        if (stud != null) {
            stud.setHidden(hide);
            log.debug("Student relation updated");
            studentService.update(stud);
        }

        userController.updateUserCourses();
    }

    /**
     * Sets the relation between the given course and the logged in user
     * to deleted in order for the user to leave the course.
     */
    public void leaveCourse() {
        if (courseToLeave == null) {
            return;
        }
        log.debug("User " + loggedInUser + " leaves course " + courseToLeave.getName());
        Lecturer lec = courseToLeave.getLecturerFromUser(loggedInUser);
        if (lec != null) {
            lec.setDeleted(true);
            log.debug("Lecturer relation updated");
            lecturerService.update(lec);
        }

        PrivilegedUser priv = courseToLeave.getPrivilegedUserFromUser(loggedInUser);
        if (priv != null) {
            priv.setDeleted(true);
            log.debug("Privileged user relation updated");
            privilegedUserService.update(priv);
        }

        Student stud = courseToLeave.getStudentFromUser(loggedInUser);
        if (stud != null) {
            stud.setDeleted(true);
            log.debug("Student relation updated");
            studentService.update(stud);
        }

        userController.updateUserCourses();
    }

    /**
     * Searches for courses in the whole system with given searchInput query string.
     * Courses in which the currently logged in user is already participating (non-deleted)
     * will get removed from the result.
     */
    public void searchForCourses() {
        log.debug("searchForCourses called with input: " + courseSearchInput);
        if (courseSearchInput == null || courseSearchInput.trim().isEmpty()) {
            return;
        }

        searchResultList = new ArrayList<>();
        for (Course c : courseService.searchForCourses(courseSearchInput)) {
            if (!c.containsUser(loggedInUser)) {
                searchResultList.add(c);
            } else {
                Student stud = c.getStudentFromUser(loggedInUser);
                if (stud != null && stud.isDeleted()) {
                    searchResultList.add(c);
                }
            }
        }
    }

    /**
     * Method which gets called when the user chooses to join a course.
     * He will join it immediately with the default participation type, but
     * can change it in the next dialog. Nothing in the database is updated
     * in this step. If the user was already a student in this course then
     * his participation gets restored and set to 'not confirmed'.
     * @param course Course to join
     */
    public void courseSelected(Course course) {
        log.debug("courseSelected: " + course.getName());
        selectedCourse = course;
        boolean deletedStudent = false;

        Student stud = courseService.findStudent(selectedCourse,loggedInUser);
        if (stud != null) {
            if (stud.isDeleted()) {
                log.debug("User has already been a student");
                newStudent = stud;
                deletedStudent = true;
            } else {
                //non deleted student found
                return;
            }
        } else {
            log.debug("Creating new student");
            newStudent = new Student();
            newStudent.setUser(loggedInUser);
            newStudent.setCourse(selectedCourse);
            newStudent.setTries(0);
        }

        newStudent.setConfirmed(!selectedCourse.getRequiresConfirmation());
        newStudent.setAcceptedInvitation(true);
        newStudent.setDeleted(false);
        newStudent.setParticipationType(selectedCourse.getDefaultParticipationType());

        if (!deletedStudent) {
            selectedCourse.getStudents().add(newStudent);
        }
    }

    /**
     * Method which gets called when the user chooses a participation type of
     * the course he wants to join. His selection will be put in the new student
     * object and the course will now get updated in the database.
     */
    public void parttypeSelected() {
        ParticipationType partType = null;
        for (ParticipationType p : selectedCourse.getParticipationTypes()) {
            if (p.getPartTypeId().equals(chosenPartTypeId)) {
                partType = p;
            }
        }
        if (partType == null) {
            return;
        }

        log.debug("parttypeSelected: " + partType.getName());

        newStudent.setParticipationType(partType);

        logUserJoinedCourse(partType);

        courseService.update(selectedCourse);
        selectedCourse = null;
        chosenPartTypeId = 0;
        log.debug("Course updated with new user as student");
        userController.updateUserCourses();
    }

    /**
     * Exmatrikulator Domain logging method to log when a user joins a course.
     * The log text is different for the cases if the selected Course requires the student
     * to be confirmed or not.
     * @param pt Chosen participationtype
     */
    private void logUserJoinedCourse(ParticipationType pt) {
        String description;
        if (selectedCourse.getRequiresConfirmation()) {
            description = "User " + loggedInUser + " wants to join the course "
                    + selectedCourse.getName() + " with participationtype: "
                    + pt.getName() + ". Awaits confirmation.";
        } else {
            description = "User " + loggedInUser + " joined the course " + selectedCourse.getName()
                    + " with participatontype: " + pt.getName() + ".";
        }
        logService.persist(Log.from(loggedInUser,selectedCourse.getCourseId(),description));
    }

    /**
     * Injects the logService.
     * @param logService The logService to be injected to the bean.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
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
     * Injects the lecturer service.
     * @param lecturerService The lecturer service to be injected to the bean.
     */
    @EJB
    public void setLecturerService(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    /**
     * Injects the privileged user service.
     * @param privilegedUserService The privileged user service
     *                              to be injected to the bean.
     */
    @EJB
    public void setPrivilegedUserService(PrivilegedUserService privilegedUserService) {
        this.privilegedUserService = privilegedUserService;
    }

    /**
     * Injects the student service.
     * @param studentService The student service to be injected to the bean.
     */
    @EJB
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    public String getCourseSearchInput() {
        return courseSearchInput;
    }

    public void setCourseSearchInput(String courseSearchInput) {
        this.courseSearchInput = courseSearchInput;
    }

    public List<Course> getSearchResultList() {
        return searchResultList;
    }

    public void setSearchResultList(List<Course> searchResultList) {
        this.searchResultList = searchResultList;
    }

    public long getChosenPartTypeId() {
        return chosenPartTypeId;
    }

    public void setChosenPartTypeId(long chosenPartTypeId) {
        this.chosenPartTypeId = chosenPartTypeId;
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }

    public void setSelectedCourse(Course selectedCourse) {
        this.selectedCourse = selectedCourse;
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public void setCourseToLeave(Course courseToLeave) {
        this.courseToLeave = courseToLeave;
    }
}
