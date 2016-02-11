package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.exception.*;
import de.unibremen.opensores.model.*;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.RequestScoped;
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

    /**
     * Stores the grading for the given course.
     * @param course Course that is related to the exam
     * @param overwrite If true, overwrite an existing grade
     */
    public void storeUserGrading(Course course, boolean overwrite) throws
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
            if (formExam == -1) {
                log.debug("Storing pabo grading: " + formPaboGrading);
                PaboGrade paboGrade = PaboGrade.valueOfName(formPaboGrading);

                gradingService.storePaboGrade(course, user, formStudent,
                        paboGrade, formPrivateComment, formPublicComment,
                        overwrite);
            }
            else {
                gradingService.storeGrade(course, user, formExam, formStudent,
                        formGrading, formPrivateComment, formPublicComment,
                        overwrite);
            }
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
        } catch (InvalidGradingException | IllegalArgumentException e) {
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
}
