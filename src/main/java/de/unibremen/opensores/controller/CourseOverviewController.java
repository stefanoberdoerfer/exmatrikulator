package de.unibremen.opensores.controller;

import de.unibremen.opensores.controller.common.UserController;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.Backup;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LecturerService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.BackupService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The Backing Bean of the Course overview page. Covers logic which shouldn't
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
     * UserService for finding old users.
     */
    private UserService userService;

    /**
     * BackupService for finding old Backups.
     */
    private BackupService backupService;

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
     * Newly to be created students for joining a course.
     */
    private List<Student> newStudents;

    /**
     * The logged in user.
     */
    private User loggedInUser;

    /**
     * A list of courses which are 10 years old.
     */
    private List<Course> oldCourses;

    /**
     * A list of users which have not been active for 10 years.
     */
    private List<User> oldUsers;

    /**
     * A list of backups that are older than 10 years.
     */
    private List<Backup> oldBackups;

    /**
     * ResourceBundle for getting localised messages.
     */
    private ResourceBundle bundle;

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

        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        oldCourses = courseService.getOldCourses(loggedInUser);
        oldUsers = new ArrayList<User>();
        oldBackups = new ArrayList<Backup>();
        newStudents = new ArrayList<>();

        if (loggedInUser.hasGlobalRole(GlobalRole.ADMIN)) {
            oldCourses = courseService.getOldCourses();
            oldUsers = userService.getOldUsers();
            oldBackups = backupService.getOldBackups();
        }
    }

    /**
     * Deletes the inactive courses with its associations.
     */
    public void deleteOldData() {
        log.debug("deleteOldData() called");

        for (Course c : oldCourses) {
            logService.persist(Log.from(loggedInUser, c.getCourseId(),
                    String.format("Deleting the course %s with its Associations",
                            c.getName())));
            courseService.deleteCourseWithAssociatons(c);

        }
        oldCourses.clear();

        for (User u : oldUsers) {
            deleteUser(u);
        }
        oldUsers.clear();

        for (Backup b : oldBackups) {
            backupService.remove(b);
        }
        oldBackups.clear();
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
            //course creators can't leave the course
            if (lec.isCourseCreator()) {
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                bundle.getString("common.error"),
                                bundle.getString("courses.creatorCantLeave")));
                return;
            } else {
                lec.setDeleted(true);
                log.debug("Lecturer relation updated");
                lecturerService.update(lec);
            }
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

        logService.persist(Log.from(loggedInUser, courseToLeave.getCourseId(), "Left the course"));
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
        Student stud = joinCourse(course,loggedInUser);
        if (stud != null) {
            newStudents.add(stud);
        }
        searchResultList.clear();
    }

    /**
     * Logic to make the given user join the given course. If the user
     * has been a student in this course before his participation is restored.
     * @param course Course to join
     * @param user User to participate in the course
     * @return Newly created or restored Student object
     */
    private Student joinCourse(Course course, User user) {
        if (course == null) {
            return null;
        }
        boolean deletedStudent = false;

        Student stud = course.getStudentFromUser(user);
        if (stud != null) {
            if (stud.isDeleted()) {
                log.debug("User has already been a student");
                deletedStudent = true;
            } else {
                //non deleted student found
                return null;
            }
        } else {
            log.debug("Creating new student");
            stud = new Student();
            stud.setUser(user);
            stud.setCourse(course);
            stud.setTries(0);
        }

        stud.setConfirmed(!course.getRequiresConfirmation());
        stud.setAcceptedInvitation(true);
        stud.setDeleted(false);
        stud.setParticipationType(course.getDefaultParticipationType());

        if (!deletedStudent) {
            course.getStudents().add(stud);
        }

        return stud;
    }

    /**
     * Method which gets called when the user chooses a participation type of
     * the course he wants to join. His selection will be put in the new student
     * object and the course will now get updated in the database.
     */
    public void parttypeSelected() {
        if (selectedCourse == null) {
            return;
        }
        ParticipationType partType = null;
        for (ParticipationType p : selectedCourse.getParticipationTypes()) {
            if (p.getPartTypeId().equals(chosenPartTypeId)) {
                partType = p;
            }
        }
        if (partType == null) {
            log.debug("parttype is null");
            return;
        }

        log.debug("parttypeSelected: " + partType.getName());

        for (Student s : newStudents) {
            s.setParticipationType(partType);
            logUserJoinedCourse(partType,s.getUser());
        }

        courseService.update(selectedCourse);
        selectedCourse = null;
        newStudents = new ArrayList<>();
        chosenPartTypeId = 0;
        log.debug("Course updated with new user as student");
        userController.updateUserCourses();
    }

    /**
     * Method which gets called when the user chooses to let multiple users
     * join the course. All members will join it immediately with the default
     * participation type, but this can be changed in the next dialog.
     * Nothing in the database is updated in this step.
     * If one user is already a student in this course then
     * his participation gets restored and set to 'not confirmed'.
     * @param groupUsers Users to join a course
     */
    public void joinAsGroup(List<User> groupUsers) {
        for (User u : groupUsers) {
            Student stud = joinCourse(selectedCourse,u);
            if (stud != null) {
                newStudents.add(stud);
            }
        }
    }

    /**
     * Exmatrikulator Domain logging method to log when a user joins a course.
     * The log text is different for the cases if the selected Course requires the student
     * to be confirmed or not.
     * @param pt Chosen participationtype
     * @param user User who joined the course
     */
    private void logUserJoinedCourse(ParticipationType pt, User user) {
        String description;
        if (selectedCourse.getRequiresConfirmation()) {
            description = "User " + user + " wants to join the course "
                    + selectedCourse.getName() + " with participationtype: "
                    + pt.getName() + ". Awaits confirmation.";
        } else {
            description = "User " + user + " joined the course " + selectedCourse.getName()
                    + " with participatontype: " + pt.getName() + ".";
        }
        logService.persist(Log.from(loggedInUser,selectedCourse.getCourseId(),description));
    }

    /**
     * Returns true if old data is available.
     *
     * @return boolean if old data is available.
     */
    public boolean oldData() {
        if ((oldCourses != null) && (oldCourses.size() > 0)) {
            return true;
        } else if ((oldUsers != null) && (oldUsers.size() > 0)) {
            return true;
        } else if ((oldBackups != null) && (oldBackups.size() > 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deletes a user by overwriting all of his/her attributes.
     * This is beneficial for not destroying many relations by deleting this
     * user completely.
     *
     * @param user the user to be deleted
     */
    public void deleteUser(User user) {
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setEmail(RandomStringUtils.randomAlphanumeric(64));
        user.setMatriculationNumber("XXXXXX");
        user.setProfileInfo("");
        user.setBlocked(true);
        user.setPassword(RandomStringUtils.randomAlphanumeric(10));
        logService.persist(Log.from(loggedInUser, selectedCourse.getCourseId(),
                String.format("User %s has been deleted.", user)));
        userService.update(user);
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

    /**
     * Injects the user service.
     * @param userService The user service to be injected to the bean.
     */
    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Injects the backup service.
     * @param backupService The backup service to be injected to the bean.
     */
    @EJB
    public void setBackupService(BackupService backupService) {
        this.backupService = backupService;
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

    public List<Course> getOldCourses() {
        return oldCourses;
    }

    public List<User> getOldUsers() {
        return oldUsers;
    }

    public List<Backup> getOldBackups() {
        return oldBackups;
    }
}
