package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.util.DateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the basic log controlle for displaying logs of a course.
 *
 * @author Kevin Scheck
 * @author Sören Tempel
 */
@ViewScoped
@ManagedBean
public class LogController {
    /**
     * The path to the course overview site.
     */
    private static final String PATH_TO_COURSE_OVERVIEW =
            "/course/overview.xhtml?faces-redirect=true";

    /**
     * The http parameter key by which the course id gets passed.
     */
    private static final String HTTP_PARAM_COURSE_ID = "course-id";

    /**
     * The log4j logger for debug, errors logs from log4j.
     * These logs are not related to actions in the exmatrikulator business domain.
     */
    private static Logger log = LogManager.getLogger(LogController.class);

    /**
     * The user service for connection to the database.
     */
    private transient UserService userService;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * LogService for database transactions related to exmatrikulator logs.
     */
    private LogService logService;

    /**
     * The course for which the overview page gets accessed.
     */
    private Course course;

    /**
     * List of all available courses.
     */
    private List<Course> courses;

    /**
     * A list of the raw logs unfiltered by dates or PrimeFaces.
     */
    private List<Log> rawLogs = null;

    /**
     * The list of all logs of the course.
     */
    private List<Log> logs = new ArrayList<>();

    /**
     * The filtered list of logs for PrimeFaces datatable filtering.
     */
    private List<Log> filteredLogs;

    /**
     * Beginning date for filtering logs by date.
     */
    private Date beginDate;

    /**
     * Stop date for filtering logs by date.
     */
    private Date endDate;

    /**
     * The currently logged in user.
     */
    private transient User user;

    /**
     * Gets the Course from the Course-id http parameter.
     */
    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext exContext = facesContext.getExternalContext();

        HttpServletRequest req = (HttpServletRequest) exContext.getRequest();
        HttpServletResponse res = (HttpServletResponse) exContext.getResponse();

        user = (User) exContext.getSessionMap().get("user");
        if (user.hasGlobalRole(GlobalRole.ADMIN)) {
            courses = courseService.listCourses();
            rawLogs = logService.listLogs();

            resetDateRange();
            return;
        }

        course = courseService.findCourseById(req.getParameter(HTTP_PARAM_COURSE_ID));
        if (course == null) {
            try {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e) {
                log.fatal(e);
            }
            return;
        }

        if (userService.hasCourseRole(user, Role.LECTURER, course)) {
            rawLogs = logService.getLogFromCourse(course);
        } else {
            try {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (IOException e) {
                log.fatal(e);
            }
            return;
        }

        resetDateRange();
    }

    /**
     * Deletes the logs in the currently selected range.
     */
    public void deleteLogRange() {
        ListIterator<Log> it = logs.listIterator();
        while (it.hasNext()) {
            Log cl = it.next();
            log.debug("Removing log with id " + cl.getLogId());

            rawLogs.remove(cl);
            logService.remove(cl);

            it.remove();
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        String msg = bundle.getString("settings.logs.removeSuccess");
        facesContext.addMessage(null, new FacesMessage(FacesMessage
            .SEVERITY_INFO, bundle.getString("common.success"), msg));
    }

    /**
     * Sets the start and end date range to the first log and the current time.
     */
    public void setDefaultDateRange() {
        log.debug("setDefaultDateRange()");
        //Assuming the logs are sorted by date with newest date first
        beginDate = (rawLogs.isEmpty())
                ? DateUtil.getDateTime()
                : DateUtil.removeTime(rawLogs.get(rawLogs.size() - 1).getDate());
        endDate = DateUtil.getDateTime();

        log.debug("Begin date:" + beginDate);
        log.debug("End date: " + endDate);
    }

    /**
     * Filters the raw logs by the start and stop dates.
     */
    public void filterByDates() {
        log.debug("filterByDates() called");
        logs = rawLogs.stream()
                .filter(log -> log.getDate().after(beginDate)
                        && log.getDate().before(endDate))
                .collect(Collectors.toList());
        log.debug("logs size: " + logs.size());
    }

    /**
     * Resets the date range and filters the logs by the dates.
     */
    public void resetDateRange() {
        setDefaultDateRange();
        filterByDates();
    }


    /*
     * Getters and Setters
     */

    @EJB
    public void setUserService(UserService service) {
        this.userService = service;
    }

    @EJB
    public void setCourseService(CourseService service) {
        this.courseService = service;
    }

    @EJB
    public void setLogService(LogService service) {
        this.logService = service;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public List<Log> getFilteredLogs() {
        return filteredLogs;
    }

    public void setFilteredLogs(List<Log> filteredLogs) {
        this.filteredLogs = filteredLogs;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Date getBeginDate() {
        return new Date(beginDate.getTime());
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = new Date(beginDate.getTime());
    }

    public Date getEndDate() {
        return new Date(endDate.getTime());
    }

    public void setEndDate(Date endDate) {
        this.endDate = new Date(endDate.getTime());
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
