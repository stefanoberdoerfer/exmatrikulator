package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.exception.AlreadyGradedException;
import de.unibremen.opensores.exception.InvalidGradeException;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Group;
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
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    private String prevStudent;
    private String formGrading;
    private String formPrivateComment;
    private String formPublicComment;
    private boolean overwriting = false;
    private Integer formGradeType = GradeType.Pabo.getId();
    private BigDecimal formMaxPoints;

    @ManagedProperty("#{gradingController}")
    private GradingController gradingController;

    /**
     * Stores the currently logged in user.
     */
    private User user;

    /**
     * Stores the currently open course.
     */
    private Course course;

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
     */
    public void storeGroupGrading() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Try to store the grade
         */
        Exam exam = null;

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
                exam = gradingService.getExam(course, formExam);

                if (exam == null) {
                    facesContext.addMessage(null, new FacesMessage(FacesMessage
                            .SEVERITY_FATAL, bundle.getString("common.error"),
                            bundle.getString("gradings.unknownExam")));
                    return;
                }

                BigDecimal grading = null;

                if (formGrading != null) {
                    grading = new BigDecimal(formGrading.replace(',', '.'));
                }

                gradingService.storeGrade(course, user, exam, group,
                        grading, formPrivateComment, formPublicComment,
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
        } catch (InvalidGradeException | NumberFormatException e) {
            String errorMessage = bundle.getString("gradings.invalidGrading");

            if (exam != null) {
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
     */
    public void storeStudentGrading() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Try to store the grade
         */
        Exam exam = null;
        Student student = null;

        try {
            student = gradingService.findStudent(course, formStudent);

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
                exam = gradingService.getExam(course, formExam);

                if (exam == null) {
                    facesContext.addMessage(null, new FacesMessage(FacesMessage
                            .SEVERITY_FATAL, bundle.getString("common.error"),
                            bundle.getString("gradings.unknownExam")));
                    return;
                }

                BigDecimal grading = null;

                if (formGrading != null) {
                    grading = new BigDecimal(formGrading.replace(',', '.'));
                }

                gradingService.storeGrade(course, user, exam, student,
                        grading, formPrivateComment, formPublicComment,
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
        } catch (InvalidGradeException | NumberFormatException e) {
            String errorMessage = bundle.getString("gradings.invalidGrading");

            if (exam != null) {
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
        Reset values
         */
        resetFormValues();
        /*
        Success
         */
        gradingController.resetExamGradings(student);

        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.stored")));
    }

    public boolean isOverwriting() {
        return overwriting;
    }

    public List<String> completeFormStudent(String query) {
        List<Student> students =  gradingService.getStudents(course,user,query);
        return students.stream().map(this::printStudent).collect(Collectors.toList());
    }

    private String printStudent(Student student) {
        return student.getUser().toString();
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
        } else if (formExam != 0) {
            Exam exam = gradingService.getExam(course, formExam);

            if (exam != null) {
                formGradeType = exam.getGradeType();
                formMaxPoints = exam.getMaxPoints();
            } else {
                formGradeType = null;
                formMaxPoints = null;
            }
        } else {
            formGradeType = null;
            formMaxPoints = null;
        }

        formGrading = "";
        overwriting = false;

        log.debug("New grade type: " + formGradeType);
        log.debug("Max points: " + formMaxPoints);
    }

    /**
     * Called whenever the user selection changes. Resets the overwriting flag
     * only if the name really changed.
     */
    public void userSelectionChanged(SelectEvent event) {
        if (prevStudent != null && prevStudent.equals(event.getObject().toString())) {
            log.debug("User selection did not really change");
            return;
        }

        log.debug("User selection changed");
        overwriting = false;
        prevStudent = event.getObject().toString();
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

    public GradingController getGradingController() {
        return gradingController;
    }

    public void setGradingController(GradingController gradingController) {
        this.gradingController = gradingController;
    }

    public BigDecimal getFormMaxPoints() {
        return formMaxPoints;
    }
}
