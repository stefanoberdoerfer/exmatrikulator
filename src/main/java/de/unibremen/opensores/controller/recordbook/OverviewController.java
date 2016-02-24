package de.unibremen.opensores.controller.recordbook;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.RecordBookEntry;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.RecordBookService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * The course which exams get edited.
     */
    private Course course;

    /**
     * Currently logged in user.
     */
    private User user;

    /**
     * Storing all entries.
     */
    private List<RecordBookEntry> entries;

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
        if (entries == null) {
            entries = recordBookService.getEntries(course, user);
        }

        return entries;
    }

    /**
     * Returns all entries of the given student.
     * @param student Student whose entries shall be listed.
     * @return List of record book entries.
     */
    public List<RecordBookEntry> getEntries(Student student) {
        if (entries == null) {
            entries = recordBookService.getEntries(course, student);
        }

        return entries;
    }
}
