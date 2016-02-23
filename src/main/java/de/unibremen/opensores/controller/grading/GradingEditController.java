package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.exception.AlreadyGradedException;
import de.unibremen.opensores.exception.InvalidGradeException;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Controller for the modal dialog to update a grading for a single student.
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
    private String formPrivateComment;
    private String formPublicComment;
    private boolean overwriting = false;
    private Integer formGradeType;

    /**
     * Stores if the currently logged in user is lecturer.
     */
    private boolean isLecturer = false;

    /**
     * Stores the currently logged in user.
     */
    private User user;

    /**
     * Stores the currently open course.
     */
    private Course course;

    /**
     * Method called after initialisation.
     */
    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext exContext = facesContext.getExternalContext();

        HttpServletRequest req = (HttpServletRequest) exContext.getRequest();
        HttpServletResponse res = (HttpServletResponse) exContext.getResponse();

        user = (User) exContext.getSessionMap().get("user");
        course = courseService.findCourseById(req.getParameter("course-id"));
        if (course == null || user == null) {
            try {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e) {
                log.fatal(e);
            }
            return;
        }
        /*
        Check if he is a lecturer
         */
        isLecturer = userService.hasCourseRole(user, Role.LECTURER, course);
    }

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

    /**
     * Stores the grading for the given course.
     */
    public void updateExamGrading() {
        log.debug("Updating exam grading");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Check student
         */
        log.debug("Checking student");

        if (student == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.unknownStudent")));
            return;
        }
        /*
        Check exam
         */
        log.debug("Checking exam");

        if (exam == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.unknownExam")));
            return;
        }
        /*
        Try to store the grade
         */
        try {
            log.debug("Using gradingService to store the grade");

            final Boolean overwrite = overwriting;
            overwriting = false;

            log.debug("formGrading: " + formGrading);

            gradingService.storeGrade(course, user, exam, student,
                    formGrading, formPrivateComment, formPublicComment,
                    overwrite);
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
        } catch (InvalidGradeException e) {
            String errorMessage = bundle.getString("gradings.invalidGrading");

            if (exam.hasGradeType(GradeType.Boolean)) {
                errorMessage += bundle.getString(
                        "gradings.invalidGrading.boolean");
            } else if (exam.hasGradeType(GradeType.Numeric)) {
                errorMessage += bundle.getString(
                        "gradings.invalidGrading.numeric");
            } else if (exam.hasGradeType(GradeType.Percent)) {
                errorMessage += bundle.getString(
                        "gradings.invalidGrading.percent");
            } else if (exam.hasGradeType(GradeType.Point)) {
                errorMessage += bundle.getString(
                        "gradings.invalidGrading.point");

                errorMessage = MessageFormat.format(errorMessage,
                        exam.getMaxPoints());
            }

            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    errorMessage));
            return;
        } catch (AlreadyGradedException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_WARN, bundle.getString("common.warning"),
                    bundle.getString("gradings.overwriting")));
            overwriting = true;
            return;
        }
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    /**
     * Updates the final grading of a single student.
     */
    public void updateFinalGrading() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Check student
         */
        if (student == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("gradings.unknownStudent")));
            return;
        }
        /*
        Try to store the grade
         */
        try {
            log.debug("Storing pabo grading: " + formGrading);
            PaboGrade paboGrade = PaboGrade.valueOf(formGrading);

            final Boolean overwrite = overwriting;
            overwriting = false;

            gradingService.storePaboGrade(course, user, this.student,
                    paboGrade, formPrivateComment, formPublicComment,
                    overwrite);
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
        } catch (IllegalArgumentException e) {
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
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    /**
     * Sets the form values to display the correct student and exam when
     * opening the modal to edit an existing grading. Grading not used but
     * loaded again so it will display the most recent value.
     * @param student Student whose grading shall be updated
     * @param exam Exam that shall be graded
     */
    public void setExamGrading(final Student student, final Exam exam) {
        log.debug("Use existing grading for " + student.getUser().getFirstName()
                + " and exam " + exam.getName());
        /*
        Load the grading
        */
        final Grading grading = gradingService.getGrading(student, exam);
        /*
        Set the data
         */
        this.exam = exam;
        this.student = student;
        this.overwriting = false;
        this.formGradeType = exam.getGradeType();

        log.debug("Already graded? " + (grading != null ? "yes" : "no"));

        if (grading != null && (isLecturer
                || user.equals(grading.getCorrector()))) {
            this.formGrading = grading.getGrade().getValue().toString();
            this.formPrivateComment = grading.getPrivateComment();
            this.formPublicComment = grading.getPublicComment();
        } else {
            this.formGrading = "";
            this.formPrivateComment = "";
            this.formPublicComment = "";
        }

        log.debug("formGrading: " + formGrading);
    }

    /**
     * Sets the form values to display the correct pabo grading in the modal
     * to edit an existing final grade.
     * @param student Student whose final grade shall be edited
     */
    public void setFinalGrading(final Student student) {
        this.student = student;
        this.formGrading = student.getPaboGrade();
        this.formPrivateComment = student.getPrivateComment();
        this.formPublicComment = student.getPublicComment();
    }

    public boolean isOverwriting() {
        return overwriting;
    }

    public Integer getFormGradeType() {
        return formGradeType;
    }
}
