package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.service.UserService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.ResourceBundle;
import java.util.Calendar;
import java.util.Date;
import java.sql.Time;
import java.security.SecureRandom;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.io.IOException;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;

/**
 * Password reset controller.
 *
 * @author Sören Tempel
 */
@ManagedBean
@RequestScoped
public class ResetController {
    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(ResetController.class);

    /**
     * The user service for connection to the database.
     */
    @EJB
    private UserService userService;

    /**
     * The faces context.
     */
    FacesContext facesContext = FacesContext.getCurrentInstance();

    /**
     * ResourceBundle for access to message properties.
     */
    ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

    /**
     * Number of bits to use for creating the reset token.
     */
    private static final int numBits = 130;

    /**
     * Radix to use for converting the BigInteger to a string.
     */
    private static final int radix = 32;

    /**
     * Amount of time a token is valid (in hours).
     */
    private static final int validTime = 3;

    /**
     * The email of the user who requested a password reset.
     */
    private String email;

    /**
     * Creates a new password reset token.
     */
    public void createToken() {
        User user = userService.findByEmail(email);

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

        PasswordReset passReset = initPasswordReset(user);
        user.setToken(passReset);
        user = userService.update(user);

        try {
            sendEmail(user);
        } catch (MessagingException | IOException e) {
            log.error(e);
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("passwordReset.mailFail")));
            return;
        }

        facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_INFO, bundle.getString("common.success"),
                    bundle.getString("passwordReset.success")));
    }

    /**
     * Send the password token to the given user using an email.
     *
     * @param user User to which the email should be send.
     * @throws MessagingException If the message couldn't be send.
     * @throws IOException If an IO related error occured.
     */
    private void sendEmail(User user) throws MessagingException, IOException {
        HttpServletRequest request = (HttpServletRequest) facesContext
                .getExternalContext().getRequest();
        String url = resetURL(request, user, user.getToken());

        String fmt = bundle.getString("passwordReset.mailBody");
        String text = new MessageFormat(fmt).format(new Object[]{
                user.getFirstName(), url});

        String subject = bundle.getString("passwordReset.mailSubject");
        user.sendEmail(subject, text);
    }

    /**
     * Returns an URL to reset the password for the given user.
     *
     * @param request Request context to extract URL from.
     * @param user Owner of this password reset.
     * @param token Token used for this password reset.
     *
     * @return Absolute URL for a password reset.
     * @throws MalformedURLException If a properly formatted URL couldn't
     *      be created from the given data.
     */
    public String resetURL(HttpServletRequest request, User user, PasswordReset token)
            throws MalformedURLException {
        // XXX can this be tricked into returning another URL?
        StringBuffer requestUrl = request.getRequestURL();

        int baseIdx = requestUrl.lastIndexOf("/");
        String baseUrl = requestUrl.substring(0, baseIdx);

        return String.format("%s/%s?id=%d&token=%s", baseUrl,
                "new-password.xhtml", user.getUserId(), token.getToken());
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
