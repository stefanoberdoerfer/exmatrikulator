package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
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
     * Finds a user by his login credentials.
     * @param email The inserted email by the user;
     *     must be an email and cant be null nor empty.
     * @param password The inserted email by the user;
     *     must be an email and cant be null nor empty.
     * @return The found user or null if the user was not found.
     */
    public User findByLogin(String email, String password) {
        List<User> userList = em.createQuery(
                "SELECT DISTINCT u "
              + "FROM User u "
              + "WHERE u.email = :email", User.class)
            .setParameter("email", email.toLowerCase()).getResultList();

        if (userList.isEmpty()) {
            return null;
        }

        final User user = userList.get(0);
        if (BCrypt.checkpw(password, user.getPassword())) {
            return user;
        } else {
            return null;
        }
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
