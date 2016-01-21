package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * The Backing Bean of the login page.
 * Manages the authentication of the user.
 *
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
     *
     * @return Redirection link.
     */
    public String login() {
        initDummyUsers();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        User user = userService.findByEmail(email);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages",
                    facesContext.getViewRoot().getLocale());
            String text = bundle.getString("login.fail");
            facesContext.addMessage(null, new FacesMessage(text));
            return "";
        }

        facesContext.getExternalContext().getSessionMap().put("user", user);
        return "index.xhtml";
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

    /**
     * Inserts dummy users in the database at startup
     * @TODO Delete before deadline :^)
     */
    private void initDummyUsers(){
        if (userService.findByEmail("user@uni-bremen.de") != null) {
            return;
        }

        final User newUser = new User();
        newUser.setEmail("user@uni-bremen.de");
        newUser.setPassword(BCrypt.hashpw("user",BCrypt.gensalt()));
        newUser.setFirstName("Ute");
        newUser.setLastName("User");
        newUser.setLanguage("de");
        newUser.getRoles().add(Role.USER.getId());

        final User newLecturer = new User();
        newLecturer.setEmail("lecturer@uni-bremen.de");
        newLecturer.setPassword(BCrypt.hashpw("lecturer",BCrypt.gensalt()));
        newLecturer.setFirstName("Leo");
        newLecturer.setLastName("Lektor");
        newLecturer.getRoles().add(Role.LECTURER.getId());
        newLecturer.getRoles().add(Role.USER.getId());

        final User newAdmin = new User();
        newAdmin.setEmail("admin@uni-bremen.de");
        newAdmin.setPassword(BCrypt.hashpw("admin",BCrypt.gensalt()));
        newAdmin.setFirstName("Adolf");
        newAdmin.setLastName("Admin");
        newAdmin.getRoles().add(Role.ADMIN.getId());
        newAdmin.getRoles().add(Role.USER.getId());

        userService.persist(newUser);
        userService.persist(newLecturer);
        userService.persist(newAdmin);
    }

}
