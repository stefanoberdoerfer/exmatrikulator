package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.ServiceException;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

/**
 * The Backing Bean of the login page.
 * Manages the authentication of the user.
 * @author Sören Tempel
 * @author Stefan Oberdörfer
 * @author Kevin Scheck
 */
@ManagedBean
@RequestScoped
public class LoginController {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(LoginController.class);

    /**
     * The user service for connection to the database.
     */
    @EJB
    private UserService userService;

    /**
     * The typed in email of the user who wants to log in.
     */
    private String email;

    /**
     * The typed in plaintext password of the user who wants to log in.
     */
    private String password;

    /**
     * Login method which tries to lookup user with given email and password.
     * @return redirection link if user was successfully logged in. '#' if not.
     */
    public String login() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        User user = null;
        try {
            user = userService.findByLogin(email, password);
            if (user == null) {
                ResourceBundle bundle =
                        ResourceBundle.getBundle("messages",
                                facesContext.getViewRoot().getLocale());
                String text = bundle.getString("login.fail");
                facesContext.addMessage("", new FacesMessage(text));
                return "#";
            } else {
                FacesContext.getCurrentInstance()
                        .getExternalContext().getSessionMap().put("user", user);
                return "index.xhtml";
            }
        } catch (ServiceException e) {
            log.error(e);
            return "#";
        }
    }

    /**
     * Gets the typed in email.
     * @return The typed in email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the typed in email.
     * @param email The typed in email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the typed in plaintext password.
     * @return The typed in plaintext password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the plaintext password.
     * @param password The typed in password, not null
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
