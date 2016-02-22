package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.exception.AlreadyGradedException;
import de.unibremen.opensores.exception.InvalidGradeException;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;

import java.util.ResourceBundle;

/**
 * Controller for the modal dialogs to insert grades for single students and
 * groups.
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
    private Long formGroup;
    private String formStudent;
    private String formGrading;
    private String formPrivateComment;
    private String formPublicComment;
    private boolean overwriting = false;
    private Integer formGradeType = GradeType.Pabo.getId();

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

    public Long getFormGroup() {
        return formGroup;
    }

    public void setFormGroup(Long formGroup) {
        this.formGroup = formGroup;
    }

    /**
     * Stores the group grading for the given course.
     * @param course Course that is related to the exam
     */
    public void storeGroupGrading(Course course) {

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
            Group group = gradingService.getGroup(course, formGroup);

            if (group == null) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("gradings.unknownGroup")));
                return;
            }

            final Boolean overwrite = overwriting;
            overwriting = false;

            if (formExam == -1) {
                log.debug("Storing pabo grading: " + formGrading);
                PaboGrade paboGrade = PaboGrade.valueOf(formGrading);

                gradingService.storePaboGrade(course, user, group,
                        paboGrade, formPrivateComment, formPublicComment,
                        overwrite);
            } else {
                Exam exam = gradingService.getExam(course, formExam);

                if (exam == null) {
                    facesContext.addMessage(null, new FacesMessage(FacesMessage
                            .SEVERITY_FATAL, bundle.getString("common.error"),
                            bundle.getString("gradings.unknownExam")));
                    return;
                }

                gradingService.storeGrade(course, user, exam, group,
                        formGrading, formPrivateComment, formPublicComment,
                        overwrite);
            }
        } catch (IllegalAccessException e) {
            if (e.getMessage().equals("NOT_GRADABLE")) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("common.notGradable")));
            } else {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("common.noAccess")));
            }

            return;
        } catch (InvalidGradeException | IllegalArgumentException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.invalidGrading")));
            return;
        } catch (AlreadyGradedException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_WARN, bundle.getString("common.warning"),
                    bundle.getString("gradings.overwriting")));
            overwriting = true;
            return;
        }
        /*
        Reset the form values
         */
        resetFormValues();
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    /**
     * Stores the student grading for the given course.
     * @param course Course that is related to the exam
     */
    public void storeStudentGrading(Course course) {

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
            Student student = gradingService.findStudent(course,
                    formStudent);

            if (student == null) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("gradings.unknownStudent")));
                return;
            }

            final Boolean overwrite = overwriting;
            overwriting = false;

            if (formExam == -1) {
                log.debug("Storing pabo grading: " + formGrading);
                PaboGrade paboGrade = PaboGrade.valueOf(formGrading);

                gradingService.storePaboGrade(course, user, student,
                        paboGrade, formPrivateComment, formPublicComment,
                        overwrite);
            } else {
                Exam exam = gradingService.getExam(course, formExam);

                if (exam == null) {
                    facesContext.addMessage(null, new FacesMessage(FacesMessage
                            .SEVERITY_FATAL, bundle.getString("common.error"),
                            bundle.getString("gradings.unknownExam")));
                    return;
                }

                gradingService.storeGrade(course, user, exam, student,
                        formGrading, formPrivateComment, formPublicComment,
                        overwrite);
            }
        } catch (IllegalAccessException e) {
            if (e.getMessage().equals("NOT_GRADABLE")) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("common.notGradable")));
            } else {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("common.noAccess")));
            }

            return;
        } catch (InvalidGradeException | IllegalArgumentException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.invalidGrading")));
            return;
        } catch (AlreadyGradedException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_WARN, bundle.getString("common.warning"),
                    bundle.getString("gradings.overwriting")));
            overwriting = true;
            return;
        }
        /*
        Reset values
         */
        resetFormValues();
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    public boolean isOverwriting() {
        return overwriting;
    }

    /**
     * Called whenever the exam selection changes. Also resets the overwriting
     * flag.
     * @param course Course in which to search for the exam.
     */
    public void changedExamSelection(Course course) {
        log.debug("Exam selection changed: " + formExam);

        if (formExam == -1) {
            formGradeType = GradeType.Pabo.getId();
        }
        else if (formExam != 0) {
            Exam exam = gradingService.getExam(course, formExam);

            if (exam != null) {
                formGradeType = exam.getGradeType();
            } else {
                formGradeType = null;
            }
        } else {
            formGradeType = null;
        }

        formGrading = "";
        overwriting = false;

        log.debug("New grade type: " + formGradeType);
    }

    public void userSelectionChanged() {
        log.debug("User selection changed");
        overwriting = false;
    }

    public void groupSelectionChanged() {
        log.debug("Group selection changed");
        overwriting = false;
    }

    public Integer getFormGradeType() {
        return formGradeType;
    }

    /**
     * Resets the form values. Exam will not be reset so the user can keep
     * on grading.
     */
    private void resetFormValues() {
        formGroup = null;
        formStudent = "";
        formGrading = "";
        formPrivateComment = "";
        formPublicComment = "";
        overwriting = false;
    }
}
