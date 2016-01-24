package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;

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
     * Returns a list of courses this user takes part in.
     *
     * @param user User to lookup courses for.
     * @param hidden Whether hidden courses should be included.
     * @return List of courses or null if none where found.
     */
    public List<Course> getCourses(final User user, boolean hidden) {
        List<Course> courses = em.createQuery(
                "SELECT c FROM Course c"
                + " INNER JOIN c.students AS s"
                + " WHERE s.user = :user"
                + " AND s.isHidden = :hidden", Course.class)
            .setParameter("user", user)
            .setParameter("hidden", hidden)
            .getResultList();

        return (courses.isEmpty()) ? null : courses;
    }
}
