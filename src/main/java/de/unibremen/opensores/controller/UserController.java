package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 * This class implements the controller of the
 */
@SessionScoped
@ManagedBean
public class UserController {

    /**
     * The currently logged in user
     */
    private User user;

    @EJB
    private UserService userService;

    @PostConstruct
    public void init(){
        user = (User) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("user");
    }

    /**
     * Logs the user out of the current session
     * @return The of the login page
     */
    public String logout(){
        FacesContext.getCurrentInstance()
                .getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

}
