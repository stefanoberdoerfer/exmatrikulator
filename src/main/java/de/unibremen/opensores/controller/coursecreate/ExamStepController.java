package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.controller.settings.ExamController;
import de.unibremen.opensores.model.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * The Backing Bean of the Course create page 'Exams'.
 * This controls the second page of the courseCreate-Flow.
 * Main functionality is implemented in the ExamController.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean(name = "step2")
@ViewScoped
public class ExamStepController implements Serializable {

    private static final long serialVersionUID = 1399760789905109109L;

    /**
     * FlowScoped course to be created.
     */
    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    /**
     * ExamController for main functionality.
     */
    @ManagedProperty("#{examController}")
    private transient ExamController examController;

    /**
     * Initialisation method to get semesters from semesterService and
     * add the courseNumber input fields to the UI.
     */
    @PostConstruct
    public void init() {
        examController.setCourse(course);
        examController.setCalledFromWizard(true);
    }

    public ExamController getExamController() {
        return examController;
    }

    public void setExamController(ExamController examController) {
        this.examController = examController;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
