package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
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
     * Initialisation method to get the currently logged in user.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");

        loggedInUser = (User) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(Constants.SESSION_MAP_KEY_USER);
    }

    /**
     * Searches for courses in the whole system with given searchInput query string.
     * Courses in which the currently logged in user is already participation will get removed
     * from the result.
     */
    public void searchForCourses() {
        log.debug("searchForCourses called with input: " + courseSearchInput);
        if (courseSearchInput == null || courseSearchInput.trim().isEmpty()) {
            return;
        }

        searchResultList = courseService.searchForCourses(courseSearchInput).stream()
                .filter(c -> !c.containsUser(loggedInUser))
                .collect(Collectors.toList());
    }

    /**
     * Method which gets called when the user chooses to join a course.
     * He will join it immediately with the default participation type, but
     * can change it in the next dialog. Nothing in the database is updated
     * in this step.
     * @param course Course to join
     */
    public void courseSelected(Course course) {
        log.debug("courseSelected: " + course.getName());
        selectedCourse = course;

        newStudent = new Student();
        newStudent.setUser(loggedInUser);
        newStudent.setCourse(course);
        newStudent.setConfirmed(false);
        newStudent.setAcceptedInvitation(true);
        newStudent.setDeleted(false);
        newStudent.setTries(0);
        newStudent.setParticipationType(course.getDefaultParticipationType());

        course.getStudents().add(newStudent);
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
}
