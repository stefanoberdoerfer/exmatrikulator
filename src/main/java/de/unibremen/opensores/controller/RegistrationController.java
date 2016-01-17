package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

/**
 * Backing bean of the registration page.
 * Manages the validation of user inputs and inserts new user to the database.
 * TODO Also sends out authentications emails to newly registered users.
 * @author Kevin Scheck
 */
@ManagedBean
@RequestScoped
public class RegistrationController {

    /**
     * The user service for connection to the database.
     */
    @EJB
    private UserService userService;

    /**
     * The typed in email of the user, shouldn't be in the system.
     */
    private String email;

    /**
     * The typed in plaintext password of the user.
     */
    private String password;

    /**
     * The zyped in first name of the user.
     */
    private String firstName;

    /**
     * The typed in last name of the user.
     */
    private String lastName;

    /**
     * Registers new users to the exmatrikulator database.
     * @return The redirection string depending if the registration succeeded.
     */
    public String register() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (userService.isEmailRegistered(email)) {
            ResourceBundle bundle =
                    ResourceBundle.getBundle("messages",
                            facesContext.getViewRoot().getLocale());
            String text = bundle.getString("registration.alreadyRegistered");
            facesContext.addMessage("", new FacesMessage(text));
            return "#";
        }
        
        final User registeredUser = initUserFromInput();
        userService.persist(registeredUser);
        //TODO Send authentication email when the email service is ready

        return "/login.xhtml?faces-redirect=true";
    }

    /**
     * Initialises the user object from the user input.
     * @Pre The user input(email,password,firstname,lastname) is valid.
     * @return The generated registered User Object.
     */
    private User initUserFromInput() {
        final String hashPW = BCrypt.hashpw(password, BCrypt.gensalt());
        final User registeredUser = new User();
        registeredUser.setEmail(email);
        registeredUser.setPassword(hashPW);
        registeredUser.setFirstName(firstName);
        registeredUser.setLastName(lastName);
        //TODO Change language by radio buttons etc.
        registeredUser.setLanguage("de");
        registeredUser.getRoles().add(Role.USER);
        return registeredUser;
    }


    /*
     * GETTERS AND SETTERS
     */

    /**
     * Gets the typed in email.
     * @return The typed in email
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
     * @param password The typed in password, not null.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the typed in first name.
     * @return The typed first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the typed in first name.
     * @param firstName The typed in first name (not null).
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the typed in last name.
     * @return The typed last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the typed in last name.
     * @param lastName The typed in last name (not null).
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
