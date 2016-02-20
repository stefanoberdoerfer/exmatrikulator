package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.controller.settings.CommonDataController;
import de.unibremen.opensores.model.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * The Backing Bean of the Course create page 'General Data'.
 * This controls the first page of the courseCreate-Flow.
 * Main functionality is implemented in the CommonDataController.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean(name = "step1")
@ViewScoped
public class CommonDataStepController implements Serializable {

    private static final long serialVersionUID = -5674550179148802338L;

    private static transient Logger log = LogManager.getLogger(CommonDataStepController.class);

    /**
     * ExamController for main functionality.
     */
    @ManagedProperty("#{commonDataController}")
    private transient CommonDataController commonDataController;

    /**
     * FlowScoped course to be created.
     */
    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    /**
     * Initialisation method to get semesters from semesterService and
     * add the courseNumber input fields to the UI.
     */
    @PostConstruct
    public void init() {
        commonDataController.setCourse(course);
        commonDataController.setCalledFromWizard(true);
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public CommonDataController getCommonDataController() {
        return commonDataController;
    }

    public void setCommonDataController(CommonDataController commonDataController) {
        this.commonDataController = commonDataController;
    }
}
