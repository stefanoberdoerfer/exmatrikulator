package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


/**
 * Service class for the User model class
 */
@Stateless
public class UserService extends GenericService<User>{

    /**
     * Finds a user by his login credentials.
     * @param email The inserted email by the user;
     *              must be an email and cant be null nor empty.
     * @param password The inserted email by the user;
     *              must be an email and cant be null nor empty.
     * @return The found user; Null if the user is not found.
     * @throws ServiceException If the database connection threw an exception.
     */
    public User findByLogin(String email, String password) throws ServiceException {
        List<User> userList = em.createQuery(
                "SELECT DISTINCT u " +
                "FROM User u " +
                "WHERE u.email = :email AND u.password = :password", User.class)
            .setParameter("email",email)
            .setParameter("password",password)
            .getResultList();
        if(userList.size() > 1){
            throw new ServiceException(
                    "Multiple users found with logon credentials");
        } else if (userList.isEmpty()){
            return null;
        } else {
            return userList.get(0);
        }
    }

    /**
     * Checks
     * @param email
     * @return
     */
    public boolean isEmailRegistered(String email){
        final String trimmedEmail = email.trim().toLowerCase();
        List<User> registeredUserEmail = em.createQuery(
                "SELECT DISTINCT u FROM User u WHERE TRIM(LOWER(u.email)) = :email", User.class)
                .setParameter("email",trimmedEmail)
                .getResultList();
        return !registeredUserEmail.isEmpty();
    }
    //TODO JPA User search
}
