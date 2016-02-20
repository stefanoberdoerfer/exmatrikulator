package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.controller.settings.PartTypeController;
import de.unibremen.opensores.model.Course;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * The Backing Bean of the Course create page 'ParticipationTypes'.
 * This controls the third page of the courseCreate-Flow.
 * Main functionality is implemented in the ParttypeController.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean(name = "step3")
@ViewScoped
public class ParttypeStepController implements Serializable {

    private static final long serialVersionUID = 1778954138733333537L;

    /**
     * FlowScoped course to be created.
     */
    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    /**
     * PartTypeController for main functionality.
     */
    @ManagedProperty("#{partTypeController}")
    private transient PartTypeController partTypeController;


    @PostConstruct
    public void init() {
        partTypeController.setCourse(course);
        partTypeController.setCalledFromWizard(true);
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public PartTypeController getPartTypeController() {
        return partTypeController;
    }

    public void setPartTypeController(PartTypeController partTypeController) {
        this.partTypeController = partTypeController;
    }
}
