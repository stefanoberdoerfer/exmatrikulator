package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.service.UserService;

import java.math.BigInteger;
import java.util.ResourceBundle;
import java.util.Calendar;
import java.util.Date;
import java.sql.Time;
import java.security.SecureRandom;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.bean.ManagedBean;

/**
 * Password reset controller.
 *
 * @author Sören Tempel
 */
@ManagedBean
@RequestScoped
public class ResetController {
    /**
     * The user service for connection to the database.
     */
    @EJB
    private UserService userService;

    /**
     * Number of bits to use for creating the reset token.
     */
    private final static int numBits = 130;

    /**
     * Radix to use for converting the BigInteger to a string.
     */
    private final static int radix = 32;

    /**
     * Amount of time a token is valid (in hours).
     */
    private final static int validTime = 3;

    /**
     * The email of the user who requested a password reset.
     */
    private String email;

    /**
     * Creates a new password reset token.
     */
    public void createToken() {
        User user = userService.findByEmail(email);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                    facesContext.getViewRoot().getLocale());

        String msg = null;
        if (user == null) {
            msg = bundle.getString("passwordReset.fail");
        } else if (user.getToken() != null) {
            if (user.getToken().hasExpired()) {
                user.setToken(null);
            } else {
                msg = bundle.getString("passwordReset.exists");
            }
        }

        if (msg != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        user.setToken(initPasswordReset(user));
        userService.update(user);

        facesContext.addMessage(null, new FacesMessage(FacesMessage.
                    SEVERITY_INFO, bundle.getString("common.success"),
                    bundle.getString("passwordReset.success")));
    }

    /**
     * Creates a new PasswordReset entity.
     *
     * @param user User the password reset token belongs to.
     * @return PasswordReset entity.
     */
    public PasswordReset initPasswordReset(User user) {
        SecureRandom rand = new SecureRandom();
        String strTok = new BigInteger(numBits, rand).toString(radix);

        PasswordReset passReset = new PasswordReset();
        passReset.setToken(strTok);
        passReset.setUser(user);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, validTime);
        passReset.setExpires(new Time(cal.getTime().getTime()));

        return passReset;
    }

    /**
     * Returns the email of the user who requested a password reset.
     *
     * @return Email of the user who wants to reset his password.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email for the password reset.
     *
     * @return Email of the user who wants to reset his password.
     */
    public void setEmail(final String email) {
        this.email = email;
    }
}
