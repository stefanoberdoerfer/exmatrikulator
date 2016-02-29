package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.DateUtil;
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
 *
 * @author Kevin Scheck
 * @author Sören Tempel
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
     * The typed in plain text password of the user.
     */
    private String password;

    /**
     * The confirmed password in plain text.
     */
    private String passwordConfirmed;

    /**
     * The typed in first name of the user.
     */
    private String firstName;

    /**
     * The typed in last name of the user.
     */
    private String lastName;

    /**
     * The typed in salution of the user.
     */
    private String salution;

    /**
     * The typed in matriculationNumber (optional).
     */
    private String matriculationNumber;

    /**
     * Registers new users to the exmatrikulator database.
     * @return The redirection string depending if the registration succeeded.
     */
    public String register() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());

        String msg = null;
        if (userService.isEmailRegistered(email)) {
            msg = bundle.getString("registration.alreadyRegistered");
        } else if (!password.equals(passwordConfirmed)) {
            msg = bundle.getString("registration.passwordDoNotMatch");
        }

        if (msg != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return "";
        }

        /* TODO wait until the admin confirmed the account and send an email to
         * the user as soon as he did. */

        userService.persist(initUserFromInput());
        facesContext.addMessage(null, new FacesMessage(FacesMessage
            .SEVERITY_INFO, bundle.getString("common.success"),
            bundle.getString("registration.success")));

        facesContext.getExternalContext().getFlash().setKeepMessages(true);
        return "/login.xhtml?faces-redirect=true";
    }

    /**
     * Initialises the user object from the user input.
     * @Pre The user input(email,password,firstname,lastname) is valid.
     * @return The generated registered User Object.
     */
    private User initUserFromInput() {
        final String hashPW = BCrypt.hashpw(password, BCrypt.gensalt());
        final User newUser = new User();

        /* Don't set language here. Only set the language column if the user
         * explicitly requested it by changing his settings. JSF determines the
         * locale it should use by looking at the HTTP Accept-Language Header.
         * If it isn't set it uses the default-locale set in our
         * faces-config.xml. */

        newUser.setEmail(email);
        newUser.setPassword(hashPW);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setSalution(salution);
        newUser.setMatriculationNumber(matriculationNumber);
        newUser.addRole(GlobalRole.USER);
        newUser.setLastActivity(DateUtil.getDateTime());
        return newUser;
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
     * Gets the typed in plain text password.
     * @return The typed in plain text password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the typed in confirmed plain text password.
     * @return The confirmed plain text password.
     */
    public String getPasswordConfirmed() {
        return passwordConfirmed;
    }

    /**
     * Sets the plain text password.
     * @param password The typed in password, not null.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the confirmed plain text password.
     * @param passwordConfirmed The confirmed password, not null.
     */
    public void setPasswordConfirmed(String passwordConfirmed) {
        this.passwordConfirmed = passwordConfirmed;
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

    /**
     * Gets the typed in salution.
     * @return The typed in salution.
     */
    public String getSalution() {
        return salution;
    }

    /**
     * Sets the typed in salution.
     * @param saltuion The typed in salution.
     */
    public void setSalution(String salution) {
        this.salution = salution;
    }

    /**
     * Gets the typed in matriculationNumber.
     * @return The typed matriculationNumber.
     */
    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    /**
     * Sets the typed in matriculationNumber.
     * @param matriculationNumber The typed in matriculationNumber (optional).
     */
    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }
}
