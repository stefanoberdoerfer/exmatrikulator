package de.unibremen.opensores.controller.admin;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Backup;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeFormulaService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.LecturerService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.BackupService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.DateUtil;
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
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElementDecl;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * BackupService for finding old Backups.
     */
    private BackupService backupService;

    /**
     * The UserService for listing all users.
     */
    private UserService userService;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * Services for database transactions for user->course relations.
     */
    private LecturerService lecturerService;
    private PrivilegedUserService privilegedUserService;
    private StudentService studentService;

    /**
     * Other Services needed for user merging.
     */
    private GradingService gradingService;
    private GradeFormulaService gradeFormulaService;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;

    /**
     * A list of courses which are 10 years old.
     */
    private List<Course> oldCourses;

    /**
     * A list of users which have not been active for 10 years.
     */
    private List<User> oldUsers;

    /**
     * A list of backups that are older than 10 years.
     */
    private List<Backup> oldBackups;

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

        oldCourses = courseService.getOldCourses();
        oldUsers = userService.getOldUsers();
        oldBackups = backupService.getOldBackups();
    }

    /**
     * Returns true if old data is available.
     *
     * @return boolean if old data is available.
     */
    public boolean oldData() {
        log.debug("oldData() called.");
        if ((oldCourses != null) && (oldCourses.size() > 0)) {
            return true;
        } else if ((oldUsers != null) && (oldUsers.size() > 0)) {
            return true;
        } else if ((oldBackups != null) && (oldBackups.size() > 0)) {
            return true;
        } else {
            log.debug("No old data found.");
            return false;
        }
    }

    /**
     * Deletes the inactive courses with its associations.
     */
    public void deleteOldData() {
        log.debug("deleteOldData() called");

        for (Course c : oldCourses) {
            courseService.deleteCourseWithAssociatons(c);
        }
        oldCourses.clear();

        for (User u : oldUsers) {
            deleteUser(u);
        }
        oldUsers.clear();

        for (Backup b : oldBackups) {
            backupService.remove(b);
        }
        oldBackups.clear();
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

    private void updateUserList() {
        users = userService.getUsers();
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
        log.debug("Admin: " + globalRoleAdmin + " Lecturer: " + globalRoleLecturer
            + " User: " + globalRoleUser);
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
        log.debug("onCreateUserCalled invoked");
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
        selectedUser.setLastActivity(DateUtil.getDateTime());

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
        updateUserList();
    }

    /**
     * Deletes a user by overwriting all of his/her attributes and setting all
     * relations to courses to 'deleted'.
     * This is beneficial to not destroy many relations by deleting the
     * user completely.
     */
    public void deleteUser() {

        for (Course c : userService.getAllCourses(selectedUser)) {
            Lecturer lec = c.getLecturerFromUser(selectedUser);
            PrivilegedUser priv = c.getPrivilegedUserFromUser(selectedUser);
            Student stud = c.getStudentFromUser(selectedUser);

            if (lec != null) {
                lec.setDeleted(true);
            }
            if (priv != null) {
                priv.setDeleted(true);
            }
            if (stud != null) {
                stud.setDeleted(true);
            }

            courseService.update(c);
        }

        logUserDeleted(selectedUser);
        selectedUser.setFirstName("Deleted");
        selectedUser.setLastName("User");
        selectedUser.setEmail(RandomStringUtils.randomAlphanumeric(10));
        selectedUser.setPassword(null);
        selectedUser.setMatriculationNumber("XXXXXX");
        selectedUser.setProfileInfo("");
        selectedUser.setPassword(RandomStringUtils.randomAlphanumeric(10));
        userService.update(selectedUser);
        clearFields();
        updateUserList();
    }

    /**
     * Deletes a user by overwriting all of his/her attributes.
     * This is beneficial for not destroying many relations by deleting this
     * user completely. This method overloads the deleteUser method of this
     * class.
     *
     * @param toDelete the user to be deleted
     */
    public void deleteUser(User toDelete) {
        for (Course c : userService.getAllCourses(toDelete)) {
            Lecturer lec = c.getLecturerFromUser(toDelete);
            PrivilegedUser priv = c.getPrivilegedUserFromUser(toDelete);
            Student stud = c.getStudentFromUser(toDelete);

            if (lec != null) {
                lec.setDeleted(true);
            }
            if (priv != null) {
                priv.setDeleted(true);
            }
            if (stud != null) {
                stud.setDeleted(true);
            }

            courseService.update(c);
        }

        toDelete.setFirstName("Deleted");
        toDelete.setLastName("User");
        toDelete.setEmail(RandomStringUtils.randomAlphanumeric(10));
        toDelete.setPassword(null);
        toDelete.setMatriculationNumber("XXXXXX");
        toDelete.setProfileInfo("");
        toDelete.setPassword(RandomStringUtils.randomAlphanumeric(10));
        userService.update(toDelete);
    }

    /**
     * Method which tries to merge the currently selected User with the user given
     * as a parameter. The User object of the selected User will be kept and all
     * relations of the user toBeMerged will be updated to the selected user.
     * An error will be shown if the users participate in the same courses and nothing
     * will be merged.
     * @param toBeMerged User which technically will be replaced.
     */
    public void mergeUser(User toBeMerged) {
        FacesContext context = FacesContext.getCurrentInstance();
        log.debug("User " + selectedUser + " should be merged with " + toBeMerged);

        List<Course> coursesOfOtherUser = userService.getAllCourses(toBeMerged);

        //check if the users have common courses to throw an error
        for (Course c : coursesOfOtherUser) {
            log.debug("Checking Course " + c.getName() + " of user " + toBeMerged);
            if (c.containsUser(selectedUser)) {
                log.debug("Course " + c.getName() + " contains user " + selectedUser);
                context.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_FATAL, bundle.getString("common.error"),
                        bundle.getString("users.mergeError")
                                + " (" + c.getName() + ")."));
                log.debug("merge failed");
                return;
            }
        }

        //Course-relations
        for (Course c : coursesOfOtherUser) {
            Lecturer lec = c.getLecturerFromUser(toBeMerged);
            PrivilegedUser priv = c.getPrivilegedUserFromUser(toBeMerged);
            Student stud = c.getStudentFromUser(toBeMerged);

            if (lec != null) {
                lec.setUser(selectedUser);
                lecturerService.update(lec);
            }
            if (priv != null) {
                priv.setUser(selectedUser);
                privilegedUserService.update(priv);
            }
            if (stud != null) {
                stud.setUser(selectedUser);
                studentService.update(stud);
            }
        }

        //gradings
        for (Grading g : gradingService.getGradingsByCorrector(toBeMerged)) {
            g.setCorrector(selectedUser);
            gradingService.update(g);
        }

        //gradeformulas
        for (GradeFormula g : gradeFormulaService.getFormulasByEditor(toBeMerged)) {
            g.setEditor(selectedUser);
            gradeFormulaService.update(g);
        }

        //logs
        for (Log l : logService.getLogFromUser(toBeMerged)) {
            l.setLoggedInUser(selectedUser);
            logService.update(l);
        }

        //GlobalRoles
        toBeMerged.getRoles().stream()
                .filter(i1 -> !selectedUser.getRoles().contains(i1))
                .forEach(i2 -> selectedUser.getRoles().add(i2));
        userService.update(selectedUser);
        logUserMerged(selectedUser, toBeMerged);
        log.debug("merge was successful");
        //delete other user
        selectedUser = toBeMerged;
        deleteUser();
        updateUserList();
    }

    /**
     * Prints the localised list of the users globalRoles with given ids
     * @param ids ID numbers of the users globalroles
     * @return localised string version of that given roles list.
     *         Can contain localised 'undefined' elements if a
     *         roleID element is not defined
     */
    public String getNamesOfGlobalRoles(List<Integer> ids) {
        return ids.stream()
                .map(this::getNameOfGlobalRole)
                .collect(Collectors.joining(", "));
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
        logUpdateUser(selectedUser);
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

        if (selectedUser != null && selectedUser.getUserId() != null
                && selectedUser.equals(userService.findByEmail(emailInput))) {
            // In this case, the selected User gets edited and the email adress is
            // not taken -> pass.
        } else if (userService.isEmailRegistered(emailInput)) {
            messages.add(new FacesMessage(bundle
                    .getString("registration.alreadyRegistered")));
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

    private String getNameOfGlobalRole(Integer role) {
        try {
            return bundle.getString(GlobalRole.valueOf(role).getMessage());
        } catch (Exception e) {
            return null;
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


    private void logUpdateUser(User user) {
        logAction("The user " + user + " has been updated.");
    }

    private void logUserMerged(User selectedUser, User mergeWith) {
        log.debug("User " + selectedUser + " was merged with user " + mergeWith);
    }

    private void logUserDeleted(User user) {
        logAction("The user " + user + " has been deleted.");

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

    /**
     * Injects the studentService.
     * @param studentService The course service to be injected.
     */
    @EJB
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Injects the gradingService.
     * @param gradingService The gradingService to be injected.
     */
    @EJB
    public void setGradingService(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    /**
     * Injects the gradeFormulaService.
     * @param gradeFormulaService The gradeFormulaService to be injected.
     */
    @EJB
    public void setGradeFormulaService(GradeFormulaService gradeFormulaService) {
        this.gradeFormulaService = gradeFormulaService;
    }

    /**
     * Injects the privilegedUserService.
     * @param privilegedUserService The course service to be injected.
     */
    @EJB
    public void setPrivilegedUserService(PrivilegedUserService privilegedUserService) {
        this.privilegedUserService = privilegedUserService;
    }

    /**
     * Injects the lecturerService.
     * @param lecturerService The course service to be injected.
     */
    @EJB
    public void setLecturerService(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    /**
     * Injects the backup service.
     * @param backupService The backup service to be injected to the bean.
     */
    @EJB
    public void setBackupService(BackupService backupService) {
        this.backupService = backupService;
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

    public List<Course> getOldCourses() {
        return oldCourses;
    }

    public List<User> getOldUsers() {
        return oldUsers;
    }

    public List<Backup> getOldBackups() {
        return oldBackups;
    }
}
