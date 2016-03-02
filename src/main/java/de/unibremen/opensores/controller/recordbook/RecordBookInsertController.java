package de.unibremen.opensores.controller.recordbook;

import de.unibremen.opensores.exception.AlreadyGradedException;
import de.unibremen.opensores.exception.InvalidGradeException;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.RecordBookService;
import de.unibremen.opensores.service.UserService;
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
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Controller for the modal dialog to insert an entry in the record book.
 * @author Matthias Reichmann
 */
@ManagedBean(name = "recordbookInsert")
@ViewScoped
public class RecordBookInsertController {
    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            RecordBookInsertController.class);

    /**
     * Data set by the formula of the modal to insert grades.
     */
    private Long formExam;
    private Date formDate = new Date();
    private Integer formDuration;
    private String formComment = "";

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
     * RecordBookService for database transactions related to record books.
     */
    @EJB
    private RecordBookService recordBookService;

    /**
     * The logService for exmatrikulator logs.
     */
    @EJB
    private LogService logService;

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
        }
    }

    public Long getFormExam() {
        return formExam;
    }

    public void setFormExam(Long formExam) {
        this.formExam = formExam;
    }

    public User getUser() {
        return user;
    }

    public Course getCourse() {
        return course;
    }

    public Integer getFormDuration() {
        return formDuration;
    }

    public void setFormDuration(Integer formDuration) {
        this.formDuration = formDuration;
    }

    public String getFormComment() {
        return formComment;
    }

    public void setFormComment(String formComment) {
        this.formComment = formComment;
    }

    /**
     * Returns the date. Custom getter necessary to not expose internal
     * representation.
     * @return Selected date
     */
    public Date getFormDate() {
        if (formDate == null) {
            return null;
        } else {
            return new Date(formDate.getTime());
        }
    }

    /**
     * Sets the date. Custom setter necessary to not expose internal
     * representation.
     * @param formDate New date
     */
    public void setFormDate(final Date formDate) {
        if (formDate == null) {
            this.formDate = null;
        } else {
            this.formDate = new Date(formDate.getTime());
        }
    }

    /**
     * Stores the group grading for the given course.
     */
    public void store() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Try to store the entry
         */
        try {
            Exam exam = recordBookService.getExam(course, formExam);

            if (exam == null) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("gradings.unknownExam")));
                return;
            }


            recordBookService.addEntry(course, user, exam, formDate,
                    formDuration, formComment);
            logService.persist(Log.from(user, course.getCourseId(),
                    String.format("Has inserted an record book entry of the user %s "
                                    + "with the date %s and duration %d.",
                            user, formDate.toString(), formDuration)));

        } catch (IllegalAccessException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.noAccess")));

            return;
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("recordbook.duration.invalid")));
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
                bundle.getString("recordbook.stored")));
    }

    /**
     * Resets the form values. Exam will not be reset so the user can keep
     * on grading.
     */
    private void resetFormValues() {
        formComment = "";
        formExam = null;
        formDuration = null;
        formDate = new Date();
    }
}
