package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.controller.settings.StudentImportController;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.User;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

/**
 * The Backing Bean of the Course create page 'StudentImport'.
 * This controls the fourth page of the courseCreate-Flow.
 * Main functionality is implemented in the StudentImportController.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean(name = "step4")
@ViewScoped
public class StudentImportStepController implements Serializable {

    private static final long serialVersionUID = 2550340343684226647L;

    /**
     * FlowScoped course to be created.
     */
    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    @ManagedProperty("#{courseCreateFlowController.usersToBeCreated}")
    private transient List<User> usersToBeCreated;

    /**
     * PartTypeController for main functionality.
     */
    @ManagedProperty("#{studentImportController}")
    private transient StudentImportController studentImportController;

    /**
     * Initialises the main StudentImportController with corresponding attributes from the
     * flowscoped Course.
     */
    @PostConstruct
    public void init() {
        studentImportController.setCourse(course);
        studentImportController.setUsersToBeCreated(usersToBeCreated);
        studentImportController.setCalledFromWizard(true);
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public StudentImportController getStudentImportController() {
        return studentImportController;
    }

    public void setStudentImportController(StudentImportController studentImportController) {
        this.studentImportController = studentImportController;
    }

    public List<User> getUsersToBeCreated() {
        return usersToBeCreated;
    }

    public void setUsersToBeCreated(List<User> usersToBeCreated) {
        this.usersToBeCreated = usersToBeCreated;
    }
}
