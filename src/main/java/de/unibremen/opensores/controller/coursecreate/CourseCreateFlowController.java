package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Named;
import java.io.Serializable;

/**
 * The Controller for the course creation wizard.
 * Is invoked when the first page of the course creation flow is called.
 * @author Stefan Oberdörfer
 * @author Kevin Scheck
 */

@Named
@FlowScoped("coursecreate")
public class CourseCreateFlowController implements Serializable {

    private static final long serialVersionUID = 4242275867511550487L;

    private static transient Logger log = LogManager.getLogger(CourseCreateFlowController.class);

    /**
     * The course created by the user.
     */
    private transient Course course;

    /**
     * The currently logged in user.
     */
    private transient User user;

    @EJB
    private transient CourseService courseService;

    /**
     * Initialises the course object which should be set up with its properties.
     * Adds the creating user as a lecturer of this lecture in this method.
     * This method gets called automatically because it's mentioned in the
     * CourseCreateDefinition class.
     */
    public void initialize() {
        log.debug("Course Create flow initialized");
        course = new Course();
        Lecturer lecturerCreator = new Lecturer();
        user = (User) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("user");
        lecturerCreator.setUser(user);
        lecturerCreator.setCourse(course);
        course.getLecturers().add(lecturerCreator);
    }

    public void finalizeFlow() {
        log.info("Course Create flow finalized");
    }


    /**
     * Saves the created course in the database.
     * @Pre All properties of the course are set.
     * @Post The created course is in the database.
     *      The user is redirect to the index page.
     * @Return The name of the return page after the course has been saved.
     */
    public String saveCourse() {
        log.debug("saveCourse() called");
        logDebugData();
        courseService.persist(course);
        return getReturnValue();
    }

    /**
     * The page visted after ending the wizard.
     * @return The name of the page after ending the flow.
     */
    public String getReturnValue() {
        return "/course/overview";
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    private void logDebugData() {
        //log.debug("Course numbers size: " + courseNumbers.size());
        //courseNumbers.stream().forEach
        //        (number -> log.debug("Course number: " + number.getValue()));
        //log.debug("Course name: " + course.getName());
        //log.debug("Course semester: " + semester.getName());
        //log.debug("Course Students can see formula: "
        //        + course.getStudentsCanSeeFormula());
    }



}
