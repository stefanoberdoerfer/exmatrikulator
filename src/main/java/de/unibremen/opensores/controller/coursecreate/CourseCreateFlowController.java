package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The Controller for the course creation wizard.
 * Flow is defined in CourseCreateDefinition.
 * Is invoked when the first page of the course creation flow is called.
 * Gets destroyed when a page which is not in the Flow is called.
 * @author Stefan Oberdörfer
 * @author Kevin Scheck
 */

@Named
@FlowScoped("coursecreate")
public class CourseCreateFlowController implements Serializable {

    private static final long serialVersionUID = 4242275867511550487L;

    private static transient Logger log = LogManager.getLogger(CourseCreateFlowController.class);

    /**
     * Boolean flags for completion status of the 5 different wizard steps.
     */
    private boolean[] stepFinished = new boolean[5];

    /**
     * The course created by the user.
     */
    private transient Course course;

    /**
     * The currently logged in user.
     */
    private transient User user;

    /**
     * List of Users who will be created if this course-creation succeeds.
     */
    private transient List<User> usersToBeCreated;

    @EJB
    private transient CourseService courseService;

    @EJB
    private transient UserService userService;

    /**
     * Initialises the course object which should be set up with its properties.
     * Adds the creating user as a lecturer and a privileged user of this course.
     * This method gets called automatically because it's mentioned in the
     * CourseCreateDefinition class.
     */
    public void initialize() {
        log.debug("Course Create flow initialized");
        course = new Course();
        user = (User) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("user");

        Lecturer lecturerCreator = new Lecturer();
        lecturerCreator.setUser(user);
        lecturerCreator.setCourse(course);
        lecturerCreator.setIsCourseCreator(true);
        course.getLecturers().add(lecturerCreator);

        PrivilegedUser privUser = new PrivilegedUser();
        privUser.setUser(user);
        privUser.setCourse(course);
        privUser.setHidden(false);
        privUser.setSecretary(false);
        course.getTutors().add(privUser);

        usersToBeCreated = new ArrayList<>();
    }

    /**
     * Only for debug information. This method gets called automatically because it's
     * mentioned in the CourseCreateDefinition class.
     */
    public void finalizeFlow() {
        log.info("Course Create flow finalized");
    }


    /**
     * Saves the created course in the database.
     * @pre All properties of the course are set.
     * @post The created course is in the database.
     *      The user is redirect to the index page.
     * @return The name of the flow return node after the course has been saved.
     */
    public String saveCourse() {
        log.debug("saveCourse() called");

        logDebugData();

        //persist new users
        usersToBeCreated.stream().forEach(userService::persist);

        //persist whole course
        courseService.persist(course);

        log.debug("Created new Course: " + course.getName() + " with id: " + course.getCourseId());

        return getReturnValue();
    }

    /**
     * The return-node of the flow which has to be visited to end the wizard.
     * @return The name of the flow return node.
     */
    public String getReturnValue() {
        return "return-node";
    }

    private void logDebugData() {
        log.debug("Created Course: " + course.getName());
        log.debug("#Numbers: " + course.getNumbers().size());
        log.debug("#Lecturers: " + course.getLecturers().size());
        log.debug("#PartTypes: " + course.getParticipationTypes().size());
        log.debug("#Tutors: " + course.getTutors().size());
        log.debug("#Tutorials: " + course.getTutorials().size());
        log.debug("#Exams: " + course.getExams().size());
        log.debug("#Students: " + course.getStudents().size());
    }

    /**
     * Utility method to set a stepFinished boolean flag of specified step to true.
     * Counting steps starts at 1.
     *
     * @param step Number of the finished step
     */
    public void setStepFinished(int step) {
        log.debug("stepFinished called for step " + step);
        int index = step - 1;
        if (index < 0 || index >= stepFinished.length) {
            return;
        }
        stepFinished[index] = true;
    }

    /**
     * Returns the finished-status of the specified wizard step.
     * Counting steps starts at 1.
     *
     * @param step Number of the step to check
     * @return true if specified step exists and is finished, false otherwise.
     */
    public boolean isStepFinished(int step) {
        int index = step - 1;
        return !(index < 0 || index >= stepFinished.length) && stepFinished[index];
    }

    public List<User> getUsersToBeCreated() {
        return usersToBeCreated;
    }

    public void setUsersToBeCreated(List<User> usersToBeCreated) {
        this.usersToBeCreated = usersToBeCreated;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
