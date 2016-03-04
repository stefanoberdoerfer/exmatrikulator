package de.unibremen.opensores.service;

import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.util.DateUtil;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;

/**
 * Service class for the User model class.
 *
 * @author Kevin Scheck
 * @author Sören Tempel
 * @author Stefan Oberdörfer
 * @author Matthias Reichmann
 */
@Stateless
public class UserService extends GenericService<User> {

    /**
     * Number of bits to use for creating the reset token.
     */
    private static final int TOKEN_NUMBITS = 130;

    /**
     * Radix to use for converting the BigInteger to a string.
     */
    private static final int TOKEN_RANDIX = 32;

    /**
     * Finds a user using a string id.
     *
     * @param idStr User id to use for lookup.
     * @return Associated user or null.
     */
    public User findUserById(String idStr) {
        User user;
        try {
            user = (idStr == null || idStr.trim().isEmpty()) ? null :
                find(User.class, Integer.valueOf(idStr).longValue());
        } catch (NumberFormatException e) {
            user = null;
        }

        return user;
    }

    /**
     * Finds a user by his unique email credential.
     * @param email The inserted email by the user;
     *     must be an email and cant be null nor empty.
     * @return The found user or null if the user was not found.
     */
    public User findByEmail(String email) {
        List<User> userList = em.createQuery(
                "SELECT DISTINCT u "
              + "FROM User u "
              + "WHERE u.email = :email", User.class)
            .setParameter("email", email.toLowerCase()).getResultList();

        return (userList.isEmpty()) ? null : userList.get(0);
    }

    /**
     * Checks if given email is already an registered account.
     * @param email email as typed in by the user
     * @return true if email has already been used for registration, false otherwise
     */
    public boolean isEmailRegistered(String email) {
        List<User> registeredUserEmail = em.createQuery(
                "SELECT DISTINCT u "
                + "FROM User u "
                + "WHERE u.email = :email", User.class)
                .setParameter("email",email.toLowerCase())
                .getResultList();
        return !registeredUserEmail.isEmpty();
    }

    /**
     * Returns true if the given user is a lecturer in the given course.
     *
     * @param user User to check.
     * @param course Course to check.
     * @return True if he is, false otherwise.
     */
    private boolean isLecturer(User user, Course course) {
        List<Lecturer> lecturers = em.createQuery(
                "SELECT DISTINCT l FROM Lecturer l "
                + "JOIN l.user    AS u WITH u.userId = :uid "
                + "JOIN l.course  AS c WITH c.courseId = :cid "
                + "WHERE l.isDeleted = false", Lecturer.class)
            .setParameter("uid", user.getUserId())
            .setParameter("cid", course.getCourseId())
            .getResultList();

        return !lecturers.isEmpty();
    }



    /**
     * Returns true if the given user is a lecturer in the given course.
     *
     * @param user User to check.
     * @param course Course to check.
     * @return The lecturer if found, else null.
     */
    public Lecturer getLecturer(User user, Course course) {
        List<Lecturer> lecturers = em.createQuery(
                "SELECT DISTINCT l FROM Lecturer l "
                + "JOIN l.user    AS u WITH u.userId = :uid "
                + "JOIN l.course  AS c WITH c.courseId = :cid "
                + "WHERE l.isDeleted = false", Lecturer.class)
            .setParameter("uid", user.getUserId())
            .setParameter("cid", course.getCourseId())
            .getResultList();

        return (lecturers.isEmpty()) ? null : lecturers.get(0);
    }

    /**
     * Returns true if the given user is a privileged user in the given course.
     *
     * @param user User to check.
     * @param course Course to check.
     * @return True if he is, false otherwise.
     */
    private boolean isPrivileged(User user, Course course) {
        List<PrivilegedUser> privUsers = em.createQuery(
                "SELECT DISTINCT p FROM PrivilegedUser p "
                + "JOIN p.user    AS u WITH u.userId = :uid "
                + "JOIN p.course  AS c WITH c.courseId = :cid "
                + "WHERE p.isDeleted = false", PrivilegedUser.class)
            .setParameter("uid", user.getUserId())
            .setParameter("cid", course.getCourseId())
            .getResultList();

        return !privUsers.isEmpty();
    }

    /**
     * Returns true if the given user is a student in the given course.
     *
     * @param user User to check.
     * @param course Course to check.
     * @return True if he is, false otherwise.
     */
    private boolean isStudent(User user, Course course) {
        List<Student> students = em.createQuery(
                "SELECT DISTINCT s FROM Student s "
                + "JOIN s.user    AS u WITH u.userId = :uid "
                + "JOIN s.course  AS c WITH c.courseId = :cid "
                + "WHERE s.isDeleted = false", Student.class)
            .setParameter("uid", user.getUserId())
            .setParameter("cid", course.getCourseId())
            .getResultList();

        return !students.isEmpty();
    }

    /**
     * Returns true if the user has the given role in the given course.
     *
     * @param user User whos roles should be checked.
     * @param role Role the user should have.
     * @param course Course context for the role.
     * @return True if the user has the given role, false otherwise.
     */
    public boolean hasCourseRole(User user, Role role, Course course) {
        switch (role) {
            case LECTURER:
                return isLecturer(user, course);
            case PRIVILEGED_USER:
                return isPrivileged(user, course);
            case STUDENT:
                return isStudent(user, course);
            default:
                return false;
        }
    }

    /**
     * Returns true if the user has the given role in the given course.
     *
     * @param user User whos roles should be checked.
     * @param roleStr Role (as a string) the user should have.
     * @param course Course context for the role.
     * @return True if the user has the given role, false otherwise.
     */
    public boolean hasCourseRole(User user, String roleStr, Course course) {
        return hasCourseRole(user, Role.valueOf(roleStr), course);
    }

    /**
     * Returns a list of courses this user takes part in.
     *
     * @param user User to lookup courses for.
     * @param hidden Whether hidden or unhidden courses should be in the list.
     * @return List of courses or null if none where found.
     */
    public List<Course> getCourses(final User user, boolean hidden) {
        // We need to use the userId here instead of the user object
        // Known Bug: https://hibernate.atlassian.net/browse/HHH-2772

        List<Course> courses = em.createQuery(
                "SELECT DISTINCT c FROM Course c"
                + " LEFT JOIN c.students AS s"
                + " WITH s.user.userId = :id"
                + " LEFT JOIN c.tutors AS t"
                + " WITH t.user.userId = :id"
                + " LEFT JOIN c.lecturers AS l"
                + " WITH l.user.userId = :id"
                + " WHERE ((s.isHidden = :hidden and s.isDeleted = false) OR"
                + " (l.isHidden = :hidden and l.isDeleted = false) OR"
                + " (t.isHidden = :hidden and t.isDeleted = false))"
                + " AND c.deleted = false", Course.class)
            .setParameter("id", user.getUserId())
            .setParameter("hidden", hidden)
            .getResultList();

        return (courses.isEmpty()) ? null : courses;
    }

    /**
     * Returns a list of all courses (hidden/shown/deleted) this user takes part in.
     *
     * @param user User to lookup courses for.
     * @return List of courses or empty list if none where found.
     */
    public List<Course> getAllCourses(final User user) {
        // We need to use the userId here instead of the user object
        // Known Bug: https://hibernate.atlassian.net/browse/HHH-2772

        return em.createQuery(
                "SELECT DISTINCT c FROM Course c"
                        + " LEFT JOIN c.students AS s"
                        + " WITH s.user.userId = :id"
                        + " LEFT JOIN c.tutors AS t"
                        + " WITH t.user.userId = :id"
                        + " LEFT JOIN c.lecturers AS l"
                        + " WITH l.user.userId = :id"
                        + " WHERE l.user.userId IS NOT NULL"
                        + " OR t.user.userId IS NOT NULL"
                        + " OR s.user.userId IS NOT NULL", Course.class)
                .setParameter("id", user.getUserId())
                .getResultList();
    }

    /**
     * Returns a list of courses this user is a lecturer of.
     *
     * @param user User to lookup courses for.
     * @return List of courses with the user as lecturer or null
     *         if none where found.
     */
    public List<Course> getLecturerCourses(final User user) {

        List<Course> courses = em.createQuery(
                "SELECT DISTINCT c FROM Course c"
                        + " LEFT JOIN c.lecturers AS l"
                        + " WITH (l.user.userId = :id and l.isDeleted = false)"
            + " WHERE c.deleted = false", Course.class)
                .setParameter("id", user.getUserId())
                .getResultList();

        return (courses.isEmpty()) ? null : courses;
    }

    /**
     * Searches for users by their email, firstName, lastName or a combination
     * of their first and last name. One of these options should be passed as
     * searchInput parameter string.
     * @param searchInput The search input, representing only the
     *                    email, firstName, lastName or a combination of the
     *                    first and last name of the user.
     * @return A list of users which match the search input. An empty List if
     *         the searchInput is null or empty.
     */
    public List<User> searchForUsers(String searchInput) {
        if (searchInput == null || searchInput.trim().isEmpty()) {
            return new ArrayList<>();
        }
        final String trimSearchInput = searchInput.trim().toLowerCase();
        if (trimSearchInput.contains(" ") && trimSearchInput.split(" ").length >= 2) {
            final String[] names = trimSearchInput.split(" ");
            return em.createQuery(
                "SELECT DISTINCT u FROM User u WHERE "
                + "(TRIM(LOWER(u.firstName)) LIKE :firstNameSplit "
                     + "AND TRIM(LOWER(u.lastName)) LIKE :lastNameSplit) "
                + "OR TRIM(LOWER(u.firstName)) LIKE :searchInput "
                + "OR TRIM(LOWER(u.lastName)) LIKE :searchInput "
                + "OR TRIM(LOWER(u.email)) LIKE :searchInput "
                + "OR TRIM(LOWER(u.matriculationNumber)) LIKE :searchInput", User.class)
                .setParameter("firstNameSplit", names[0])
                .setParameter("lastNameSplit", names[1])
                .setParameter("searchInput", trimSearchInput)
                .getResultList();
        } else {
            return em.createQuery(
                "SELECT DISTINCT u FROM User u WHERE "
                        + "TRIM(LOWER(u.email)) LIKE :searchInput "
                        + "OR TRIM(LOWER(u.firstName)) LIKE :searchInput "
                        + "OR TRIM(LOWER(u.lastName)) LIKE :searchInput "
                        + "OR TRIM(LOWER(u.matriculationNumber)) LIKE :searchInput", User.class)
                .setParameter("searchInput", trimSearchInput + "%")
                .getResultList();
        }
    }


    /**
     * Searches for lecturers by their email, firstName, lastName or a combination
     * of their first and last name. One of these options should be passed as
     * searchInput parameter string.
     * @param searchInput The search input, representing only the
     *                    email, firstName, lastName or a combination of the
     *                    first and last name of the user.
     * @return A list of users which match the search input. An empty List if
     *         the searchInput is null or empty.
     */
    public List<User> searchForLecturers(String searchInput) {
        if (searchInput == null || searchInput.trim().isEmpty()) {
            return new ArrayList<>();
        }
        final String trimSearchInput = searchInput.trim().toLowerCase();
        if (trimSearchInput.contains(" ") && trimSearchInput.split(" ").length >= 2) {
            final String[] names = trimSearchInput.split(" ");
            return em.createQuery(
                    "SELECT DISTINCT u FROM User u WHERE "
                            + GlobalRole.LECTURER.getId() + " IN elements(u.roles) AND"
                            + "((TRIM(LOWER(u.firstName)) LIKE :firstNameSplit "
                            + "AND TRIM(LOWER(u.lastName)) LIKE :lastNameSplit) "
                            + "OR TRIM(LOWER(u.firstName)) LIKE :searchInput "
                            + "OR TRIM(LOWER(u.lastName)) LIKE :searchInput "
                            + "OR TRIM(LOWER(u.email)) LIKE : searchInput)", User.class)
                    .setParameter("firstNameSplit", names[0])
                    .setParameter("lastNameSplit", names[1])
                    .setParameter("searchInput", trimSearchInput)
                    .getResultList();
        } else {
            return em.createQuery(
                    "SELECT DISTINCT u FROM User u WHERE "
                            + GlobalRole.LECTURER.getId() + " IN elements(u.roles) AND"
                            + "(TRIM(LOWER(u.email)) LIKE :searchInput "
                            + "OR TRIM(LOWER(u.firstName)) LIKE :searchInput "
                            + "OR TRIM(LOWER(u.lastName)) LIKE :searchInput "
                            + "OR TRIM(LOWER(u.email)) LIKE :searchInput)", User.class)
                    .setParameter("searchInput", trimSearchInput + "%")
                    .getResultList();
        }

    }

    /**
     * Creates a new PasswordReset entity.
     *
     * @param user User the password reset token belongs to.
     *
     * @param expirationHours The experiation of the token in hours
     * @return PasswordReset entity.
     */
    public PasswordReset initPasswordReset(User user, int expirationHours) {
        if (expirationHours <= 0) {
            throw new IllegalArgumentException("the experiationHours must be positive");
        }

        SecureRandom rand = new SecureRandom();
        String strTok = new BigInteger(TOKEN_NUMBITS, rand).toString(TOKEN_RANDIX);

        PasswordReset passReset = new PasswordReset();
        passReset.setToken(strTok);
        passReset.setUser(user);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, expirationHours);
        passReset.setExpires(new Time(cal.getTime().getTime()));

        return passReset;
    }

    /**
     * Returns an URL to reset the password for the given user.
     *
     * @param request Request context to extract URL from.
     * @param user Owner of this password reset.
     * @param token Token used for this password reset.
     *
     * @return Absolute URL for a password reset.
     * @throws MalformedURLException If a properly formatted URL couldn't
     *      be created from the given data.
     */
    public String getPasswordResetURL(HttpServletRequest request, User user, PasswordReset token)
            throws MalformedURLException {
        StringBuffer requestUrl = request.getRequestURL();

        int baseIdx = requestUrl.lastIndexOf("/");
        String baseUrl = requestUrl.substring(0, baseIdx);

        return String.format("%s/%s?id=%d&token=%s", baseUrl,
                "recovery/new-password.xhtml", user.getUserId(), token.getToken());
    }

    /**
     * Returns a list of all users ordered by the last name.
     * @return List of users
     */
    public List<User> getUsers() {
        return em.createQuery(
                    "SELECT DISTINCT u FROM User u "
                        + "ORDER BY u.lastName ASC",
                    User.class)
                .getResultList();
    }

    /**
     * Gets all users than have not been active for ten years.
     *
     * @return List a list of users.
     */
    public List<User> getOldUsers() {
        List<User> users = em.createQuery(
                "SELECT DISTINCT u "
                + "FROM User u "
                + "WHERE (u.lastActivity <= :date "
                + "AND u.firstName != 'Deleted'"
		+ "AND u.password is NOT NULL)", User.class)
                .setParameter("date", DateUtil.tenYearsAgo())
                .getResultList();

        return users;
    }
}
