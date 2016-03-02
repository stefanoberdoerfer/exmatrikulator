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
        checkUser(user);
        checkDescription(description);
        Log log = withoutCourse(user, description);
        log.courseId = courseId;
        return log;
    }

    /**
     * Static constructor method to create a log given the user.
     * This log is not associated with a course.
     * The current date is used as action date.
     * @return The log with the user, the current date
     */
    public static Log withoutCourse(User user, String description) {
        checkUser(user);
        checkDescription(description);
        Log log = new Log();
        log.loggedInUser = user;
        log.actionDescription = description;
        log.date = DateUtil.getDateTime();
        return log;
    }

    /**
     * Creates an anonymous log for actions like failed attempts to login in or
     * actions in which the identiy of the logged in user should be hidden, and
     * the course is not know.
     * @param description The description of the action, can't be empty.
     * @return The log with the description of the action and the current time
     *         as date.
     */
    public static Log anonymous(String description) {
        checkDescription(description);
        Log log = new Log();
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
        return new Date(date.getTime());
    }

    public void setDate(Date date) {
        this.date = new Date(date.getTime());
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

    /**
     * Checks if the user is not null.
     * @param user The user to be checked
     * @throws IllegalArgumentException If the user is null.
     */
    private static void checkUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("The user can't be null.");
        }
    }

    /**
     * Checks if the description of the log is nut null nor empty.
     * @param description The desription of the log.
     * @throws IllegalArgumentException If the description is null or empty.
     */
    private static void checkDescription(String description) {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("The must be a non empty string.");
        }
    }
}
