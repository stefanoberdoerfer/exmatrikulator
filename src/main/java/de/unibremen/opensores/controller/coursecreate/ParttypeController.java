package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.ParticipationType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * The Backing Bean of the Course create page 'Participationtypes'.
 * This controls the third page of the courseCreate-Flow.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class ParttypeController implements Serializable {

    private static final long serialVersionUID = -6787083097815672788L;

    private static transient Logger log = LogManager.getLogger(ParttypeController.class);

    /**
     * Currently edited ParticipationType.
     */
    private transient ParticipationType currentPartType;

    /**
     * Flag if an existing ParticipationType or a newly create one is being edited.
     */
    private boolean editmode;

    /**
     * FlowScoped course to be created.
     */
    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    /**
     * Initialisation method to insert a default ParticipationType if the current
     * to be created course has no ParticipationTypes.
     */
    @PostConstruct
    public void init() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        currentPartType = new ParticipationType();
        if (course.getParticipationTypes().isEmpty()) {
            currentPartType.setIsDefaultParttype(true);
            currentPartType.setName(bundle.getString("participationType.defaultPartType"));
            course.getParticipationTypes().add(currentPartType);
            editmode = true;
        }
    }

    /**
     * Adds one ParticipationType to the list.
     */
    public void addPartType() {
        if (!editmode) {
            if (course.getParticipationTypes().isEmpty()) {
                currentPartType.setIsDefaultParttype(true);
            }
            course.getParticipationTypes().add(currentPartType);
        } else {
            editmode = false;
        }
        log.debug("Save ParticipationType: " + currentPartType.getName());
        currentPartType = new ParticipationType();
    }

    /**
     * Loads the given ParticipationType into the UI to get edited by the user.
     *
     * @param partType ParticipationType to get edited
     */
    public void editPartType(ParticipationType partType) {
        editmode = true;
        currentPartType = partType;
        log.debug("Edit ParticipationType: " + partType.getName());
    }

    /**
     * Removes given ParticipationType from the to be created course.
     *
     * @param partType ParticipationType to get removed from the course
     */
    public void removePartType(ParticipationType partType) {
        course.getParticipationTypes().remove(partType);
        if (editmode) {
            editmode = false;
            currentPartType = new ParticipationType();
        }
        log.debug("Removing ParticipationType: " + partType.getName());
    }

    public ParticipationType getCurrentPartType() {
        return currentPartType;
    }

    public void setCurrentPartType(ParticipationType currentPartType) {
        this.currentPartType = currentPartType;
    }

    public boolean isEditmode() {
        return editmode;
    }

    public void setEditmode(boolean editmode) {
        this.editmode = editmode;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
