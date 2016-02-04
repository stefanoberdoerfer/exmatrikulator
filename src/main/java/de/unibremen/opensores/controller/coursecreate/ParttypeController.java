package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Field;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.service.SemesterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    private transient ParticipationType currentPartType;

    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    /**
     * Initialisation method to get semesters from semesterService and
     * add the courseNumber input fields to the UI.
     */
    @PostConstruct
    public void init() {
        currentPartType = new ParticipationType();
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    /**
     * Adds one ParticipationType to the list.
     */
    public void addPartType() {
        course.getParticipationTypes().add(currentPartType);
        currentPartType = new ParticipationType();
    }

    public void removePartType(ParticipationType partType) {
        course.getParticipationTypes().remove(partType);
    }

    public ParticipationType getCurrentPartType() {
        return currentPartType;
    }

    public void setCurrentPartType(ParticipationType currentPartType) {
        this.currentPartType = currentPartType;
    }
}
