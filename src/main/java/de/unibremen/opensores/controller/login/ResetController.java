package de.unibremen.opensores.controller.login;

import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.UserService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;
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
     * Amount of time a token is valid (in hours).
     */
    private static final int TOKEN_VALID_TIME = 3;

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
     * The log service for for Exmatrikulator logs.
     */
    @EJB
    private LogService logService;

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

        PasswordReset passReset = userService.initPasswordReset(user, TOKEN_VALID_TIME);
        user.setToken(passReset);
        logService.persist(Log.withoutCourse(user, "Has reset his password."));
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
        String url = userService.getPasswordResetURL(request, user, user.getToken());

        String fmt = bundle.getString("passwordReset.mailBody");
        String text = new MessageFormat(fmt).format(new Object[]{
                user.getFirstName(), url});

        String subject = bundle.getString("passwordReset.mailSubject");
        user.sendEmail(subject, text);
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
