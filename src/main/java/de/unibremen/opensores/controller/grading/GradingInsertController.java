package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.model.*;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;

import org.primefaces.context.RequestContext;

/**
 * @author Matthias Reichmann
 */
@ManagedBean
@ViewScoped
public class GradingInsertController {
    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            GradingInsertController.class);

    /**
     * Data set by the formula of the modal to insert grades.
     */
    private int gradingExam;
    private String gradingStudent;
    private int gradingPabo;
    private String gradingValue;
    private String gradingPrivateComment;
    private String gradingPublicComment;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private GradingService gradingService;

    public int getGradingExam() {
        return gradingExam;
    }

    public void setGradingExam(int gradingExam) {
        this.gradingExam = gradingExam;
    }

    public String getGradingStudent() {
        return gradingStudent;
    }

    public void setGradingStudent(String gradingStudent) {
        this.gradingStudent = gradingStudent;
    }

    public int getGradingPabo() {
        return gradingPabo;
    }

    public void setGradingPabo(int gradingPabo) {
        this.gradingPabo = gradingPabo;
    }

    public String getGradingValue() {
        return gradingValue;
    }

    public void setGradingValue(String gradingValue) {
        this.gradingValue = gradingValue;
    }

    public PaboGrade[] getPaboGrades() {
        return PaboGrade.values();
    }

    public String getGradingPrivateComment() {
        return gradingPrivateComment;
    }

    public void setGradingPrivateComment(String gradingPrivateComment) {
        this.gradingPrivateComment = gradingPrivateComment;
    }

    public String getGradingPublicComment() {
        return gradingPublicComment;
    }

    public void setGradingPublicComment(String gradingPublicComment) {
        this.gradingPublicComment = gradingPublicComment;
    }

    public void store() {
        log.debug("store: called without parameter");
    }
}
