package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the Controller for managing the logged in user.
 *
 * @author Stefan Oberdörfer
 * @author Kevin Scheck
 */
@SessionScoped
@ManagedBean
public class UserController {
    /**
     * The currently logged in user.
     */
    private User user;

    private Map<Semester, List<Course>> coursesBySemester;

    private static transient Logger log = LogManager.getLogger(UserController.class);

    /**
     * The UserService for database connection.
     */
    @EJB
    private UserService userService;

    /**
     * Gets the currently logged in user from the Session and loads
     * his active courses from the database.
     */
    @PostConstruct
    public void init() {
        user = (User) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("user");
        updateUserCourses();
    }

    /**
     * Logs the user out of the current session.
     * Invalidates the current session map.
     * Redirects to the login page.
     * TODO Maybe move this method to NavigationController
     * @return The of the login page.
     */
    public String logout() {
        FacesContext.getCurrentInstance()
                .getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    /**
     * Saves the current user in the database.
     * @Pre The user has valid fields.
     */
    public String saveUser() {
        userService.update(user);
        return "profile.xhtml";
    }

    /**
     * Gets the currently logged in user.
     * @return The currently logged in user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the current user to a new user object.
     * @param user A new logged in user, must have valid fields.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns a list of active courses for the current user.
     *
     * @return List of active courses or null.
     */
    public List<Course> getActiveCourses() {
        return userService.getCourses(getUser(), false);
    }

    /**
     * Returns a list of hidden courses for the current user.
     *
     * @return List of hidden courses or null.
     */
    public List<Course> getHiddenCourses() {
        return userService.getCourses(getUser(), true);
    }

    /**
     * loads a map from semesters to active courses from the db.
     */
    public void updateUserCourses() {
        Map<Semester, List<Course>> map = new HashMap<>();

        List<Course> courses = userService.getCourses(getUser(), false);
        for (Course course : courses) {
            Semester se = course.getSemester();
            List<Course> cs = map.get(se);
            if (cs == null) {
                cs = new ArrayList<>();
            }

            cs.add(course);
            map.put(se, cs);
        }

        log.debug("Updated Active User Couses");

        coursesBySemester = map;
    }

    public Map<Semester, List<Course>> getCoursesBySemester() {
        return coursesBySemester;
    }

    public void setCoursesBySemester(Map<Semester, List<Course>> coursesBySemester) {
        this.coursesBySemester = coursesBySemester;
    }
}
