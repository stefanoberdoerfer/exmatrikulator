package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 * This class implements the Controller for managing the logged in user.
 */
@SessionScoped
@ManagedBean
public class UserController {

    /**
     * The currently logged in user.
     */
    private User user;

    /**
     * The UserService for database connection.
     */
    @EJB
    private UserService userService;

    @PostConstruct
    public void init() {
        user = (User) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("user");
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
}
