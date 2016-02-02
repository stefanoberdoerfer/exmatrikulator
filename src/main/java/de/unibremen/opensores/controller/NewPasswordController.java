package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.bean.ManagedBean;

/**
 * Password reset token controller.
 *
 * @author Sören Tempel
 */
@ManagedBean
@RequestScoped
public class NewPasswordController {
    /**
     * The user service for connection to the database.
     */
    @EJB
    private UserService userService;

    /**
     * Unique id for the user who wants to change his password.
     */
    private String userId;

    /**
     * Token submitted for password change.
     */
    private String tokenStr;

    /**
     * The new password the user wants to use.
     */
    private String password;

    /**
     * The confirmed version of the new password.
     */
    private String passwordConfirmed;

    /**
     * Changes the user password.
     */
    public void changePassword() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        User user = (userId == null || userId.trim().isEmpty()) ? null :
            userService.find(Integer.valueOf(userId).longValue());

        String msg = null;
        if (user == null || !verify(user.getToken(), tokenStr)) {
            msg = bundle.getString("newPassword.fail");
        } else if (!password.equals(passwordConfirmed)) {
            msg = bundle.getString("registration.passwordDoNotMatch");
        }

        if (msg != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        final String hashpw = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setToken(null);
        user.setPassword(hashpw);
        userService.update(user);

        facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_INFO, bundle.getString("common.success"),
                    bundle.getString("newPassword.success")));
    }

    /**
     * Verify a string token using a PasswordReset token from the DB.
     *
     * @param token Token from the DB.
     * @param text String token submitted by the user.
     * @return True if the tokens match, false otherwise.
     */
    public boolean verify(PasswordReset token, String text) {
        if (token == null || text == null) {
            return false;
        }

        return (token.hasExpired()) ? false : token.getToken().equals(text);
    }

    /**
     * Gets the typed in plain text password.
     *
     * @return The typed in plain text password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the typed in confirmed plain text password.
     *
     * @return The confirmed plain text password.
     */
    public String getPasswordConfirmed() {
        return passwordConfirmed;
    }

    /**
     * Sets the plain text password.
     *
     * @param password The typed in password, not null.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the confirmed plain text password.
     *
     * @param passwordConfirmed The confirmed password, not null.
     */
    public void setPasswordConfirmed(String passwordConfirmed) {
        this.passwordConfirmed = passwordConfirmed;
    }

    /**
     * Returns the unique usere id.
     *
     * @return Unique user id.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique user id.
     *
     * @param userId Unique user id to use.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the token string value.
     *
     * @param Token string value.
     */
    public String getTokenStr() {
        return tokenStr;
    }

    /**
     * Sets the token string value.
     *
     * @param tokenStr Token string value to use.
     */
    public void setTokenStr(String tokenStr) {
        this.tokenStr = tokenStr;
    }
}
