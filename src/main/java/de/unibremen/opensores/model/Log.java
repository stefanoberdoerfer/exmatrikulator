package de.unibremen.opensores.model;

import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.DateUtil;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedEntityGraph;

/**
 * Created by kevin on 08.02.16.
 */
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This is a basic idea for an entity which logs the actions in the Exmatrikulator.
 */
@Entity
public class Log {

    /**
     * The unique id of a log.
     */
    @Id
    @GeneratedValue
    private long logId;

    /**
     * The logged in user which committed the action.
     * If the user's action must be logged, but the user shouldn't be shown,
     * because of privacy concerns, the loggedInUser will be null.
     */
    @ManyToOne
    @JoinColumn(nullable = true)
    private User loggedInUser;

    /**
     * The course in which a course specific action has ocurred.
     * If the action didn't occur in a specific course, the course will be null.
     */
    @Column(nullable = true)
    private long courseId;

    /**
     * The specific date in which the action has occurred. Can't be null.
     */
    @Column(nullable = false)
    private Date date;

    /**
     * The description of the action which has occured. Can include html tags
     * or links to the users site.
     */
    private String actionDescription;

    /**
     * Empty constructor for an entity bean.
     */
    public Log() {}


    /**
     * Static constructor method to create a log given the user, the given course id.
     * The current date is used as action date.
     * @return The log with the user, the current date and the course id.
     */
    public static Log from(User user, long courseId, String description) {
        if (user == null) {
            throw new IllegalArgumentException("The user can't be null.");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("The must be a non empty string.");
        }
        Log log = new Log();
        log.loggedInUser = user;
        log.courseId = courseId;
        log.actionDescription = description;
        log.date = DateUtil.getDateTime();
        return log;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }
}
