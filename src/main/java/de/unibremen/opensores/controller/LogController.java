package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Log;
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
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is the basic log controlle for displaying logs of a course.
 * //TODO Maybe extend for the case that an admin views all logs?
 * @author Kevin Scheck
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
     * A list of the raw logs unfiltered by dates or PrimeFaces.
     */
    private List<Log> rawLogs;

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
     * Gets the Course from the Course-id http parameter.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");

        HttpServletRequest httpReq
                = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        log.debug("Request URI: " + httpReq.getRequestURI());
        final String courseIdString = httpReq.getParameter(HTTP_PARAM_COURSE_ID);

        log.debug("course-id: " + courseIdString);
        long courseId = -1;
        if (courseIdString != null) {
            try {
                courseId = Long.parseLong(courseIdString.trim());
            } catch (NumberFormatException e) {
                log.debug("NumberFormatException while parsing courseId");
            }
        }

        if (courseId != -1) {
            course = courseService.find(Course.class, courseId);
        }

        log.debug("Loaded course object: " + course);

        if (course == null) {
            log.debug("trying to redirect to /course/overview");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + PATH_TO_COURSE_OVERVIEW);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                log.fatal("Could not redirect to " + PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }

        rawLogs = logService.getLogFromCourse(course);
        log.debug("Raw logs size: " + rawLogs);

        setDefaultDateRange();
        filterByDates();
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
}
