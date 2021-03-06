package de.unibremen.opensores.controller.common;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the Controller for managing the logged in user.
 *
 * @author Sören Tempel
 * @author Stefan Oberdörfer
 * @author Kevin Scheck
 * @author Matthias Reichmann
 */
@SessionScoped
@ManagedBean
public class UserController {
    /**
     * The currently logged in user.
     */
    private User user;

    private Map<Semester, List<Course>> coursesBySemester;

    private static Logger log = LogManager.getLogger(UserController.class);

    private List<Course> hiddenCourses;

    private List<Course> activeCourses;

    /**
     * Map containing the roles for each course.
     */
    private Map<Course, Role> courseRoles = new HashMap<>();

    /**
     * ApplicationController to insert session information about the rights
     * of the currently logged in user (sessionRegister).
     */
    @ManagedProperty(value = "#{applicationController}")
    private ApplicationController applicationController;

    /**
     * The currently selected user for editing the values.
     */
    private User selectedUser;

    /**
     * The user whos profile is currently being viewed.
     */
    private User profileUser;

    /**
     * User id used to set profileUser.
     */
    private String userId;

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
    public void initSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext exContext = facesContext.getExternalContext();

        user = (User) exContext.getSessionMap()
            .get(Constants.SESSION_MAP_KEY_USER);

        updateUserCourses();
        applicationController.registerSession(user,courseRoles);
    }

    /**
     * Sets the profileUser attribute.
     */
    public void initRequest() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext exContext = facesContext.getExternalContext();

        HttpServletResponse res = (HttpServletResponse) exContext.getResponse();
        if (userId != null) {
            profileUser = userService.findUserById(userId);
            if (profileUser == null) {
                try {
                    res.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (IOException e) {
                    log.fatal(e);
                }
                return;
            }
        } else {
            profileUser = user;
        }
    }

    /**
     * Removes the user from the ApplicationControllers sessionRegister.
     */
    @PreDestroy
    public void destroySession() {
        applicationController.unregisterSession(user,courseRoles);
    }

    /**
     * Whether the current user can edit the profileUsers profile.
     *
     * @return True if he can, false otherwise.
     */
    public boolean canEditProfile() {
        if (profileUser == null) {
            return false;
        }

        return user.getUserId().equals(profileUser.getUserId());
    }

    /**
     * Returns if the currently logged in user is a privileged user in the
     * given course.
     * @param course Course to check
     * @return true if he/she is privileged
     */
    public boolean isPrivilegedUser(Course course) {
        Role role = courseRoles.get(course);
        if (role == null) {
            role = determineRole(course);
        }

        return role.equals(Role.PRIVILEGED_USER);
    }

    /**
     * Returns if the currently logged in user is a lecturer in the
     * given course.
     * @param course Course to check
     * @return true if he/she is a lecturer
     */
    public boolean isLecturer(Course course) {
        Role role = courseRoles.get(course);
        if (role == null) {
            role = determineRole(course);
        }

        return role.equals(Role.LECTURER);
    }

    /**
     * Returns if the currently logged in user is a student in the
     * given course.
     * @param course Course to check
     * @return true if he/she is a student
     */
    public boolean isStudent(Course course) {
        Role role = courseRoles.get(course);
        if (role == null) {
            role = determineRole(course);
        }

        return role.equals(Role.STUDENT);
    }

    /**
     * Wrapper method for method with same signature of ApplicationController in order to
     * provide consistent call syntax in the corresponding xhtml-views.
     * @param courseId Id to specified the course which editors should be returned
     * @return List of editors for the specified course. Empty list if there aren't any.
     */
    public List<User> getEditorSessions(Long courseId) {
        return applicationController.getEditorSessions(courseId);
    }

    /**
     * Determines which highest role a user has.
     * @param course Course to check
     * @return Highest role of the user
     */
    private Role determineRole(Course course) {
        Role role;
        User user = getUser();

        if (userService.hasCourseRole(user, "LECTURER", course)) {
            role = Role.LECTURER;
        } else if (userService.hasCourseRole(user, "PRIVILEGED_USER", course)) {
            role = Role.PRIVILEGED_USER;
        } else {
            role = Role.STUDENT;
        }

        courseRoles.put(course, role);
        return role;
    }

    /**
     * Saves the current user in the database.
     * @Pre The user has valid fields.
     */
    public String saveUser() {
        user = userService.update(user);
        return "/profile/show.xhtml?faces-redirect=true";
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
        return hiddenCourses;
    }

    /**
     * Loads a map from semesters to active courses from the db.
     * Also builds up a map from courses to the corresponding Role of the currently
     * logged in user.
     */
    public void updateUserCourses() {
        Map<Semester, List<Course>> map = new HashMap<>();

        hiddenCourses = userService.getCourses(user,true);
        activeCourses = userService.getCourses(getUser(), false);

        if (activeCourses != null) {
            for (Course course : activeCourses) {
                //building map from semester to course
                Semester se = course.getSemester();
                List<Course> cs = map.get(se);
                if (cs == null) {
                    cs = new ArrayList<>();
                }

                cs.add(course);
                map.put(se, cs);

                //building map from course to role
                determineRole(course);
            }
        }

        log.debug("Updated Active User Courses");

        coursesBySemester = map;
    }

    public Map<Semester, List<Course>> getCoursesBySemester() {
        return coursesBySemester;
    }

    public void setCoursesBySemester(Map<Semester, List<Course>> coursesBySemester) {
        this.coursesBySemester = coursesBySemester;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getProfileUser() {
        return profileUser;
    }

    public void setProfileUser(User profileUser) {
        this.profileUser = profileUser;
    }
}
