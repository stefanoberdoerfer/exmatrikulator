package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;
import javax.ejb.Stateless;
import java.util.List;

/**
 * Service class for the User model class.
 * @todo Check if UserTransactions are better for exception handling
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
}
