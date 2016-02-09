package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.model.*;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;

import org.primefaces.context.RequestContext;

import java.util.ResourceBundle;

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
    private Long formExam;
    private String formStudent;
    private String formPaboGrading;
    private String formGrading;
    private String formPrivateComment;
    private String formPublicComment;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * UserService for database transactions related to users.
     */
    @EJB
    private UserService userService;

    /**
     * CourseService for database transactions related to gradings.
     */
    @EJB
    private GradingService gradingService;

    public void storeUserGrading(Course course, boolean overwrite) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Load the user
         */
        User user = (User)FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("user");
        /*
        Check if the user is a lecturer or tutors
         */
        if (!userService.hasCourseRole(user, "PRIVILEGED_USER", course) &&
                !userService.hasCourseRole(user, "LECTURER", course)) {

            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.noAccess")));
            return;
        }
        /*
        Check if the student exists and if he/she is part of this course.
         */
        if (formStudent == null || formStudent.trim().length() == 0) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.noStudent")));
            return;
        }

        log.debug("Grading: searching for student " + formStudent);

        Student student = gradingService.findStudent(course, formStudent);

        log.debug("Grading: found student? " + (student == null ? "no" : "yes"));

        if (student == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.unknownStudent")));
            return;
        }
        /*
        If the user is a tutor, check if he may grade this student
         */
        if (userService.hasCourseRole(user, "LECTURER", course) &&
                !gradingService.mayGradeStudent(user, student)) {

            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.notGradable")));
            return;
        }
        /*
        -1 means final/pabo grade. Validation for this is different.
         */
        if (formExam != -1) {
            /*
            Load the exam.
             */
            Exam exam = gradingService.getExam(course, formExam);

            if (exam == null) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("gradings.unknownExam")));
                return;
            }
            /*
            Check if there is already a grading for this student
             */
            Grading grading = gradingService.getGrading(student, exam);

            log.debug("Overwrite? " + (overwrite ? "yes" : "no"));

            if (grading != null && !overwrite) {
                throw new IllegalArgumentException("GRADE_ALREADY_EXISTS");
            }
            /*
            Check if the grading is valid
             */
            if (!exam.isValidGrading(formGrading)) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("gradings.invalidGrading")));
                return;
            }
            /*
            Store the grading
             */
            if (grading == null) {
                gradingService.storeGrade(user, student, exam, formGrading,
                        formPublicComment, formPrivateComment);
            }
            else {
                gradingService.storeGrade(user, grading, formGrading,
                        formPublicComment, formPrivateComment);
            }
        }
        else {
            /*
            Check if there is already a final grade for this student. Throwing
            an exception so the ajax error function gets called.
             */
            log.debug("Overwrite? " + (overwrite ? "yes" : "no"));

            if (student.getPaboGrade() != null && !overwrite) {
                throw new IllegalArgumentException("GRADE_ALREADY_EXISTS");
            }
            /*
            Check the grading
             */
            PaboGrade paboGrade;

            try {
                paboGrade = PaboGrade.valueOf(formPaboGrading);
            } catch (Exception e) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("gradings.invalidGrading")));
                return;
            }
            /*
            Store the final grade
             */
            gradingService.storeGrade(student, paboGrade,
                    formPublicComment, formPrivateComment);
        }
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    public PaboGrade[] getPaboGrades() {
        return PaboGrade.values();
    }

    public void setFormExam(Long formExam) {
        this.formExam = formExam;
    }

    public Long getFormExam() {
        return this.formExam;
    }

    public void setFormStudent(String formStudent) {
        this.formStudent = formStudent;
    }

    public String getFormStudent() {
        return this.formStudent;
    }

    public void setFormPaboGrading(String formPaboGrading) {
        this.formPaboGrading = formPaboGrading;
    }

    public String getFormPaboGrading() {
        return this.formPaboGrading;
    }

    public String getFormGrading() {
        return formGrading;
    }

    public void setFormGrading(String formGrading) {
        this.formGrading = formGrading;
    }

    public String getFormPrivateComment() {
        return formPrivateComment;
    }

    public void setFormPrivateComment(String formPrivateComment) {
        this.formPrivateComment = formPrivateComment;
    }

    public String getFormPublicComment() {
        return formPublicComment;
    }

    public void setFormPublicComment(String formPublicComment) {
        this.formPublicComment = formPublicComment;
    }
}
