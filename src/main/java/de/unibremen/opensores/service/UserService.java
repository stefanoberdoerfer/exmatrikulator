package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PrivilegedUser;

import java.util.List;
import javax.ejb.Stateless;

/**
 * Service class for the User model class.
 * @todo JPA User search
 *
 * @author Kevin Scheck
 * @author Sören Tempel
 */
@Stateless
public class UserService extends GenericService<User> {

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
     * Finds a user by his unique id.
     *
     * @param id Id which should be used for lookup.
     * @return The user or null if a user with this id doesn't exist.
     */
    public User findById(long id) {
        List<User> userList = em.createQuery(
                "SELECT DISTINCT u "
              + "FROM User u "
              + "WHERE u.userId = :id", User.class)
            .setParameter("id", id).getResultList();

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
     * @param User User to check.
     * @param Course Course to check.
     * @return True if he is, false otherwise.
     */
    private boolean isLecturer(User user, Course course) {
        List<Lecturer> lecturers = em.createQuery(
                "SELECT DISTINCT l FROM Lecturer"
                + "JOIN l.user    AS u WITH u.userId = :id"
                + "JOIN l.course  AS c WITH c.courseId = :id", Lecturer.class)
            .setParameter("uid", user.getUserId())
            .setParameter("cid", course.getCourseId())
            .getResultList();

        return lecturers.isEmpty();
    }

    /**
     * Returns true if the given user is a privileged user in the given course.
     *
     * @param User User to check.
     * @param Course Course to check.
     * @return True if he is, false otherwise.
     */
    private boolean isPrivileged(User user, Course course) {
        List<PrivilegedUser> privUsers = em.createQuery(
                "SELECT DISTINCT p FROM PrivilegedUser"
                + "JOIN p.user    AS u WITH u.userId = :uid"
                + "JOIN p.course  AS c WITH c.courseId = :cid", PrivilegedUser.class)
            .setParameter("uid", user.getUserId())
            .setParameter("cid", course.getCourseId())
            .getResultList();

        return privUsers.isEmpty();
    }

    /**
     * Returns true if the given user is a student in the given course.
     *
     * @param User User to check.
     * @param Course Course to check.
     * @return True if he is, false otherwise.
     */
    private boolean isStudent(User user, Course course) {
        List<Student> students = em.createQuery(
                "SELECT DISTINCT s FROM Student"
                + "JOIN s.user    AS u WITH u.userId = :uid"
                + "JOIN s.course  AS c WITH c.courseId = :cid", Student.class)
            .setParameter("uid", user.getUserId())
            .setParameter("cid", course.getCourseId())
            .getResultList();

        return students.isEmpty();
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
        Role role = Role.valueOf(roleStr);
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
     * Returns a list of courses this user takes part in.
     *
     * @param user User to lookup courses for.
     * @param hidden Whether hidden courses should be included.
     * @return List of courses or null if none where found.
     */
    public List<Course> getCourses(final User user, boolean hidden) {
        // We need to use the userId here instead of the user object
        // Kown Bug: https://hibernate.atlassian.net/browse/HHH-2772

        List<Course> courses = em.createQuery(
                "SELECT DISTINCT c FROM Course c"
                + " LEFT JOIN c.students AS s"
                + " WITH s.user.userId = :id"
                + " LEFT JOIN c.tutors AS t"
                + " WITH t.user.userId = :id"
                + " LEFT JOIN c.lecturers AS l"
                + " WITH l.user.userId = :id"
                + " WHERE s.isHidden = :hidden OR"
                + " l.isHidden = :hidden OR"
                + " t.isHidden = :hidden", Course.class)
            .setParameter("id", user.getUserId())
            .setParameter("hidden", hidden)
            .getResultList();

        return (courses.isEmpty()) ? null : courses;
    }
}
