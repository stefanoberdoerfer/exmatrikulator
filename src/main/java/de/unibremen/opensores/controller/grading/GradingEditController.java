package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.exception.*;
import de.unibremen.opensores.model.*;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

/**
 * @author Matthias Reichmann
 */
@ManagedBean
@ViewScoped
public class GradingEditController {
    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            GradingEditController.class);

    /**
     * Data set by the formula of the modal to insert grades.
     */
    private Exam exam;
    private Student student;
    private String formGrading;
    private String formPaboGrading;
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

    public PaboGrade[] getPaboGrades() {
        return PaboGrade.values();
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

    public Student getStudent() {
        return student;
    }

    public Exam getExam() {
        return exam;
    }

    public String getFormPaboGrading() {
        return formPaboGrading;
    }

    public void setFormPaboGrading(String formPaboGrading) {
        this.formPaboGrading = formPaboGrading;
    }

    /**
     * Stores the grading for the given course.
     * @param course Course that is related to the exam
     */
    public void updateExamGrading(Course course) throws
            OverwritingGradeException {

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
        Try to store the grade
         */
        try {
            gradingService.storeGrade(course, user, exam, student,
                    formGrading, formPrivateComment, formPublicComment,
                    true);
        } catch (NoAccessException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.noAccess")));
            return;
        } catch (StudentNotFoundException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.unknownStudent")));
            return;
        } catch (NotGradableException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.notGradable")));
        } catch (ExamNotFoundException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.unknownExam")));
            return;
        } catch (InvalidGradingException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.invalidGrading")));
            return;
        }
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    public void updateFinalGrading(Course course) throws
            OverwritingGradeException {

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
        Try to store the grade
         */
        try {
            log.debug("Storing pabo grading: " + formPaboGrading);
            PaboGrade paboGrade = PaboGrade.valueOfName(formPaboGrading);

            gradingService.storePaboGrade(course, user, this.student,
                    paboGrade, formPrivateComment, formPublicComment,
                    true);
        } catch (NoAccessException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.noAccess")));
            return;
        } catch (StudentNotFoundException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.unknownStudent")));
            return;
        } catch (IllegalArgumentException e) {
            log.debug("IllegalArgument: " + e.toString());
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.invalidGrading")));
            return;
        }
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    public void setExamGrading(final Student student, final Exam exam) {
        log.debug("Use existing grading for " + student.getUser().getFirstName()
                + " and exam " + exam.getName());
        /*
        Load the grading
        */
        Grading grading = gradingService.getGrading(student, exam);
        /*
        Set the data
         */
        this.exam = exam;
        this.student = student;

        log.debug("Already graded? " + (grading != null ? "yes" : "no"));

        if (grading != null) {
            this.formGrading = grading.getGrade().getValue().toString();
            this.formPrivateComment = grading.getPrivateComment();
            this.formPublicComment = grading.getPublicComment();
        }
        else {
            this.formGrading = "";
            this.formPrivateComment = "";
            this.formPublicComment = "";
        }
    }

    public void setFinalGrading(final Student student) {
        this.student = student;
        this.formPaboGrading = student.getPaboGrade();
        this.formPrivateComment = student.getPrivateComment();
        this.formPublicComment = student.getPublicComment();
    }
}
