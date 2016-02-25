package de.unibremen.opensores.controller.admin;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * The Backing Bean of the admin user overview of all system users.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class UserOverviewController {

    /**
     * The expiration duration of a reset token of the newly created user.
     */
    private static final int RESET_TOKEN_EXPIRATION = 14 * 24;

    private static Logger log = LogManager.getLogger(UserOverviewController.class);

    private User loggedInUser;

    private List<User> users;

    private List<User> filteredUsers;

    private User selectedUser;

    private boolean globalRoleAdmin;
    private boolean globalRoleLecturer;
    private boolean globalRoleUser;

    /**
     * User ResourceBundle for internationalisation information.
     */
    private ResourceBundle bundle;

    /**
     * The UserService for listing all users.
     */
    private UserService userService;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;

    /**
     * Initialisation method of the bean. Gets the currently logged in user
     * from the session and initialises the localized ResourceBundle.
     */
    @PostConstruct
    public void init() {
        loggedInUser = (User) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(Constants.SESSION_MAP_KEY_USER);

        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    /**
     * Loads all users from the database.
     * @return List of users
     */
    public List<User> getUsers() {
        if (users == null || users.isEmpty()) {
            users = userService.getUsers();
        }
        return users;
    }

    /**
     * Method which gets called when the admin clicks on edit user button.
     * Refreshes the selected user and the corresponding fields in the dialog.
     * @param user User to be edited
     */
    public void onEditUserCalled(User user) {
        selectedUser = user;
        globalRoleAdmin = user.hasGlobalRole(GlobalRole.ADMIN);
        globalRoleLecturer = user.hasGlobalRole(GlobalRole.LECTURER);
        globalRoleUser = user.hasGlobalRole(GlobalRole.USER);
    }

    /**
     * Method which gets called when the admin clicks on a userspecific button
     * other than 'edit'.
     * Refreshes the selected user.
     */
    public void onUserDialogCalled(User user) {
        selectedUser = user;
    }

    /**
     * Sends a new password reset link to an already existing user.
     */
    public void sendPasswordLink() {
        selectedUser.setPassword(null);
        PasswordReset passwordReset = userService
                .initPasswordReset(selectedUser, RESET_TOKEN_EXPIRATION);
        selectedUser.setToken(passwordReset);
        selectedUser = userService.update(selectedUser);

        logUserGotPasswordReset(selectedUser);

        try {
            sendRegistrationMail();
        } catch (MessagingException | IOException e) {
            log.error(e);
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            bundle.getString("common.error"),
                            bundle.getString("passwordReset.mailFail")));
        }
        clearFields();
    }

    /**
     * Method which gets called when the admin clicks on create new user button.
     * Refreshes the selected user and the corresponding fields in the dialog.
     */
    public void onCreateUserCalled() {
        selectedUser = new User();
    }

    /**
     * Creates a new user from the user input data.
     */
    public void createNewUser() {
        log.debug("createNewUser() called");

        if (globalRoleAdmin) {
            selectedUser.addRole(GlobalRole.ADMIN);
        }
        if (globalRoleLecturer) {
            selectedUser.addRole(GlobalRole.LECTURER);
        }
        if (globalRoleUser) {
            selectedUser.addRole(GlobalRole.USER);
        }

        selectedUser.setPassword(null);
        PasswordReset passwordReset = userService
                .initPasswordReset(selectedUser, RESET_TOKEN_EXPIRATION);
        selectedUser.setToken(passwordReset);
        userService.persist(selectedUser);

        logUserRegistedToSystem(selectedUser);

        try {
            sendRegistrationMail();
        } catch (MessagingException | IOException e) {
            log.error(e);
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            bundle.getString("common.error"),
                            bundle.getString("passwordReset.mailFail")));
        }
        clearFields();
    }

    /**
     * Deletes a user by overwriting all of his/her attributes.
     * This is beneficial for not destroying many relations by deleting this
     * user completely.
     */
    public void deleteUser() {
        selectedUser.setFirstName("Deleted");
        selectedUser.setLastName("User");
        selectedUser.setEmail("nomail@no.tld");
        selectedUser.setMatriculationNumber("XXXXXX");
        selectedUser.setProfileInfo("");
        selectedUser.setPassword(RandomStringUtils.randomAlphanumeric(10));
        userService.update(selectedUser);
        clearFields();
    }

    /**
     * Saves the changes from editing an existing user.
     * @Pre All user inputs for editing are validated (e.g. Name, Email etc.).
     */
    public void saveEditChanges() {
        log.debug("saveEditChanges called");

        selectedUser.getRoles().clear();

        if (globalRoleAdmin) {
            selectedUser.addRole(GlobalRole.ADMIN);
        }
        if (globalRoleLecturer) {
            selectedUser.addRole(GlobalRole.LECTURER);
        }
        if (globalRoleUser) {
            selectedUser.addRole(GlobalRole.USER);
        }

        userService.update(selectedUser);

        clearFields();
    }

    private void clearFields() {
        globalRoleUser = false;
        globalRoleLecturer = false;
        globalRoleAdmin = false;
        selectedUser = null;
    }

    /**
     * Sends a newly registered user an email that they are registered now.
     */
    private void sendRegistrationMail() throws MessagingException, IOException  {
        HttpServletRequest request = (HttpServletRequest)
                FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = userService.getPasswordResetURL(request, selectedUser,
                selectedUser.getToken());

        String registratorName = loggedInUser.toString();
        String fmt = bundle.getString("registration.mailByLecturer");
        String text = new MessageFormat(fmt).format(new Object[]{
                selectedUser.getFirstName(), registratorName, url});
        String subject = bundle.getString("passwordReset.mailSubject");
        selectedUser.sendEmail(subject, text);
    }

    /**
     * Validates the email of the selected user.
     * The email must match a standard email REGEX pattern.
     * If the selected user is already
     * registered in the exmatrikulator, the email can be the same as the last
     * registered email.
     * If a new user gets created, the email must be unused in the Exmatrikulator.
     * @param context The FacesContext in which the validation is done.
     * @param component The UIComponent for which the validation is done.
     * @param value The value from the UI Component
     * @throws ValidatorException If the email doesnt match the pattern, or it
     *                            it is already used in the system when the
     *                            email owner is not currently edited.
     */
    public void validateEmail(FacesContext context,
                              UIComponent component,
                              Object value)   {
        log.debug("validateDeletionNameInput called: " + value);
        List<FacesMessage> messages = new ArrayList<>();

        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        if (!(value instanceof String) || !Pattern.matches(Constants.EMAIL_REGEX, (String)value)) {
            messages.add(new FacesMessage(bundle
                    .getString("registration.invalidEmail")));
            log.debug("Value is not a string or is empty string, throwing exception");
            throw new ValidatorException(messages);
        }

        final String emailInput = (String) value;

        if (userService.isEmailRegistered(emailInput)) {
            messages.add(new FacesMessage(bundle
                    .getString("registration.alreadyRegistered")));
            log.debug("Value is not a string or is empty string, throwing exception");
            throw new ValidatorException(messages);
        }
    }

    /**
     * Validates in the deletion dialog that the user has written the first and last name
     * of the deleted user from the given association with the course.
     * @param context The FacesContext in which the validation is done.
     * @param component The UIComponent for which the validation is done.
     * @param value The value from the UI Component
     * @throws ValidatorException If the input string doesnt match the First name followed
     *                            by the last name of the user of the to be
     *                            deleted participation class.
     */
    public void validateDeletionNameInput(FacesContext context,
                                          UIComponent component,
                                          Object value)   {
        log.debug("validateDeletionNameInput called: " + value);
        List<FacesMessage> messages = new ArrayList<>();
        messages.add(new FacesMessage(bundle
                .getString("participants.remove.messageInputNotValid")));

        if (!(value instanceof String) || ((String) value).isEmpty()) {
            log.debug("Value is not a string or is empty string, throwing exception");
            throw new ValidatorException(messages);
        }

        final String stringValue = (String) value;
        final String expectedValue = selectedUser.getFirstName()
                + " " + selectedUser.getLastName();

        if (!stringValue.equals(expectedValue)) {
            log.debug("Expected value: [" + expectedValue + "] does not match"
                    + " value from user: [" + stringValue + "]; throwing exception");
            throw new ValidatorException(messages);

        }
    }

    /**
     * Small wrapper method to persist logs with LogService.
     * Logs the actions with the logService, knowing that the
     * logged in User will stay the same.
     * @param description The description of the action, not null.
     */
    private void logAction(String description) {
        logService.persist(Log.from(loggedInUser, -1L, description));
    }

    private void logUserRegistedToSystem(User user) {
        logAction("The user " + user + " has been registered to Exmatrikulator.");
    }

    private void logUserGotPasswordReset(User user) {
        logAction("The user " + user + " has been send a new password reset link.");
    }

    /**
     * Injects the logService.
     * @param logService The logService to be injected.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Injects the user service.
     * @param userService The user service to be injected.
     */
    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Injects the course service.
     * @param courseService The course service to be injected.
     */
    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<User> getFilteredUsers() {
        return filteredUsers;
    }

    public void setFilteredUsers(List<User> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public boolean isGlobalRoleAdmin() {
        return globalRoleAdmin;
    }

    public void setGlobalRoleAdmin(boolean globalRoleAdmin) {
        this.globalRoleAdmin = globalRoleAdmin;
    }

    public boolean isGlobalRoleLecturer() {
        return globalRoleLecturer;
    }

    public void setGlobalRoleLecturer(boolean globalRoleLecturer) {
        this.globalRoleLecturer = globalRoleLecturer;
    }

    public boolean isGlobalRoleUser() {
        return globalRoleUser;
    }

    public void setGlobalRoleUser(boolean globalRoleUser) {
        this.globalRoleUser = globalRoleUser;
    }
}
