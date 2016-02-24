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
 * Controller for the modal dialog to remove an entry from the record book.
 * @author Matthias Reichmann
 */
@ManagedBean(name = "recordbookRemove")
@ViewScoped
public class RecordBookRemoveController {
    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            RecordBookRemoveController.class);

    /**
     * Entry to remove.
     */
    private RecordBookEntry entry;

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

    /**
     * Called to remove an entry in the record book.
     */
    public void remove() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Try to store the entry
         */
        try {
            recordBookService.removeEntry(course, user, entry);
        } catch (IllegalAccessException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.noAccess")));

            return;
        }
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("recordbook.removed")));
    }

    /**
     * Uses the data of the given record book entry.
     * @param entry Entry to use
     */
    public void use(RecordBookEntry entry) {
        this.entry = entry;
    }

    public RecordBookEntry getEntry() {
        return entry;
    }

    public void setEntry(RecordBookEntry entry) {
        this.entry = entry;
    }
}
