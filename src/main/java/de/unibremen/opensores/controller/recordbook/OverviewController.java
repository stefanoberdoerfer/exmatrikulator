package de.unibremen.opensores.controller.recordbook;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.RecordBookEntry;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.RecordBookService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller to display the record book.
 * @author Matthias Reichmann
 */
@ManagedBean(name = "recordbook")
@ViewScoped
public class OverviewController {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(OverviewController.class);

    /**
     * Stores if the currently logged in user may see the record books of
     * others.
     */
    private boolean allowedToSeeOthers = false;

    /**
     * The course which exams get edited.
     */
    private Course course;

    /**
     * Currently logged in user.
     */
    private User user;

    /**
     * Storing the filtered entries.
     */
    private List<RecordBookEntry> filteredEntries;

    /**
     * Storing the overall duration of the filtered entries.
     */
    private Integer overallDuration = 0;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * RecordBookService for database transactions related to record books.
     */
    @EJB
    private RecordBookService recordBookService;

    /**
     * UserService for database transactions related to users.
     */
    @EJB
    private UserService userService;

    /**
     * Method called after initialisation.
     * Gets the corresponding course from the http param.
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
        Store if the currently logged in user may see the record books of
        others.
         */
        allowedToSeeOthers = userService.hasCourseRole(user, Role.LECTURER,
                course);

        if (!allowedToSeeOthers && userService.hasCourseRole(user,
                Role.PRIVILEGED_USER, course)) {
            PrivilegedUser privilegedUser = course.getPrivilegedUserFromUser(
                    user);

            allowedToSeeOthers = privilegedUser.hasPrivilege(
                    Privilege.ManageRecordBooks);
        }
    }

    public Course getCourse() {
        return course;
    }

    public User getUser() {
        return user;
    }

    /**
     * Returns all entries of the currently logged in user.
     * @return List of record book entries.
     */
    public List<RecordBookEntry> getEntries() {
        List<RecordBookEntry> entries;

        if (!allowedToSeeOthers) {
            entries = recordBookService.getEntries(course, user);

            this.overallDuration = 0;
            
            for (RecordBookEntry entry : entries) {
                this.overallDuration += entry.getDuration();
            }
        } else {
            entries = recordBookService.getEntries(course);
        }

        return entries;
    }

    public boolean isAllowedToSeeOthers() {
        return allowedToSeeOthers;
    }

    public List<RecordBookEntry> getFilteredEntries() {
        return filteredEntries;
    }

    /**
     * Sets the filtered entries and also generates the overall count.
     * @param filteredEntries Filtered entries to display
     */
    public void setFilteredEntries(List<RecordBookEntry> filteredEntries) {
        this.filteredEntries = filteredEntries;

        if (filteredEntries != null) {
            this.overallDuration = 0;

            for (RecordBookEntry entry : filteredEntries) {
                this.overallDuration += entry.getDuration();
            }
        }
    }

    public Integer getOverallDuration() {
        return overallDuration;
    }
}
