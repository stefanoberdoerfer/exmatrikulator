package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Service class for the User model class
 */
@Stateless
public class UserService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Finds a user
     * @param email
     * @param password
     * @return
     */
    public User findByLogin(String email, String password){
        //TODO
        return null;
    }
}
