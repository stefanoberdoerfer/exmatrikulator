package de.unibremen.opensores.controller.recordbook;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.RecordBookEntry;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
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
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Controller for the modal dialog to edit an entry in the record book.
 * @author Matthias Reichmann
 */
@ManagedBean(name = "recordbookEdit")
@ViewScoped
public class RecordBookEditController {
    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            RecordBookEditController.class);

    /**
     * Data set by the formula of the modal to insert grades.
     */
    private RecordBookEntry entry;
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

            recordBookService.editEntry(entry, user, exam, formDate,
                    formDuration, formComment);
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
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("recordbook.stored")));
    }

    /**
     * Uses the data of the given record book entry.
     * @param entry Entry to use
     */
    public void use(RecordBookEntry entry) {
        log.debug("Using existing entry...");
        this.entry = entry;
        this.formComment = entry.getComment();
        this.formDate = entry.getDate();
        this.formDuration = entry.getDuration();
        this.formExam = entry.getExam().getExamId();
    }
}
