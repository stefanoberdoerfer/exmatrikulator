package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Controller to manage the participants of a course.
 * //TODO Update User values after editing
 * @author Kevin Scheck
 */
@ManagedBean
@ViewScoped
public class ParticipantsController {

    /**
     * The log4j logger.
     */
    private static final Logger log = LogManager.getLogger(ParticipantsController.class);

    /**
     * The expiration duration of a reset token of the newly created user.
     */
    private static final int RESET_TOKEN_EXPIRATION = 14 * 24;

    /**
     * The course which participants get managed by this controller.
     */
    private Course course;

    /**
     * The currently logged in user.
     */
    private User loggedInUser;

    /**
     * The currently selected participation type when adding / editing students.
     */
    private long selectedParticipationTypeId;

    /**
     * The ResourceBundle for strings.
     */
    private ResourceBundle bundle;

    /**
     * The course service for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * User service for database transactions related to users.
     */
    private UserService userService;

    /**
     * The student service for database transactions related to students.
     */
    private StudentService studentService;

    /**
     * The privilege user service for database transactions related to privleged users.
     */
    private PrivilegedUserService privilegedUserService;

    /**
     * The LogService for adding logs related to the exmatrikulator actions.
     */
    private LogService logService;

    /**
     * GradeService for database transactions related to grades.
     */
    @EJB
    private GradeService gradeService;

    /**
     * The currently selected user for editing the values.
     */
    private User selectedUser;

    /**
     * The role of the currently selected user.
     */
    private int selectedRoleId;

    /**
     * Boolean value if the selected user has the privilege to edit formulas.
     */
    private boolean privilegeFormula;

    /**
     * Boolean value if the selected user has the privilege to edit exams.
     */
    private boolean privilegeExams;

    /**
     * Boolean value if the selected user has the privilege to manage students.
     */
    private boolean privilegeStudents;

    /**
     * Boolean value if the selected user has the privilege to export data.
     */
    private boolean privilegeExportData;

    /**
     * Boolean value if the selected user has the privilege to manage tutorials.
     */
    private boolean privilegeTutorials;

    /**
     * Boolean if a selected privileged user can generate credits.
     */
    private boolean privilegeGenerateCredits;

    /**
     * Boolean if a selected privileged user is a secretary.
     */
    private boolean isPrivilegedUserSecretary;

    /**
     * A text input from the user for the context (e.g. search for a user,
     * or name of a user for deletion).
     */
    private String userTextInput;

    /**
     * The lecturer which got open for editing / deleting.
     */
    private Lecturer editedLecturer;

    /**
     * The student which got open to editing / deleting.
     * The student object is temporarely stored in this property so it can
     * be deleted if the user changes his roles in this course.
     */
    private Student editedStudent;

    /**
     * The Privileged User which got open to editing / deleting.
     * The PrivilegedUser object is temporarely stored in this property so it can
     * be deleted if the user changes his roles in this course.
     */
    private PrivilegedUser editedPrivilegedUser;

    /**
     * List of filtered students for PrimeFaces filtering.
     */
    private List<Student> filteredStudents;

    /**
     * List of filtered unconfirmed students for PrimeFaces filtering.
     */
    private List<Student> filteredUnconfirmedStudents;

    /**
     * List of filtered privileged users for PrimeFaces filtering.
     */
    private List<PrivilegedUser> filteredPrivUsers;

    /**
     * List of filtered lecturers for PrimeFaces filtering.
     */
    private List<Lecturer> filteredLecturers;

    /**
     * A list containing users found by a search, which are not associated
     * with the edited course in any way.
     */
    private List<User> userSearchResultList;

    /**
     * Boolean if the logged in user can manage students.
     */
    private boolean loggedInUserCanManageStudents;

    /**
     * Boolean if the logged in user can manage tutors.
     */
    private boolean loggedInUserCanManageTutors;


    /*
     * PostConstruct Method
     */

    /**
     * Method called after the bean has been created.
     * Gets the course id from the http parameter "course-id".
     */
    @PostConstruct
    public void init() {
        selectedRoleId = Role.STUDENT.getId();
        userSearchResultList = new ArrayList<>();
        log.debug("init() called");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext exContext = facesContext.getExternalContext();

        HttpServletRequest req = (HttpServletRequest) exContext.getRequest();
        HttpServletResponse res = (HttpServletResponse) exContext.getResponse();

        loggedInUser = (User) exContext.getSessionMap().get("user");
        course = courseService.findCourseById(req.getParameter("course-id"));
        if (course == null || loggedInUser == null) {
            try {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e) {
                log.fatal(e);
            }
            return;
        }

        log.debug("Loaded course object: " + course);

        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        selectedParticipationTypeId = course.getDefaultParticipationType().getPartTypeId();

        loggedInUserCanManageTutors = course.getLecturerFromUser(loggedInUser) != null;
        loggedInUserCanManageStudents = canLoggedInUserCanManageStudents();

        log.debug("Logged in user: " + loggedInUser
                + " can manage students: " + loggedInUserCanManageStudents);
        if (!loggedInUserCanManageStudents) {
            log.debug("Logged in user cant manage students, redirecting to course overview");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + Constants.PATH_TO_COURSE_OVERVIEW);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                log.fatal("Could not redirect to " + Constants.PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }
    }


    /*
     * UI Callbacks
     */


    /**
     * Method called when the user creation dialog gets opened.
     */
    public void onCreateUserDialogCalled() {
        log.debug("onCreateUserDialogCalled() Callback called");
        resetSelectedValues();
    }

    /**
     * Method called when an existing user found by search is to be added as
     * participating user in this course.
     * @param user The existing user to be added to the course.
     * @throws IllegalArgumentException If the user is null.
     */
    public void onAddExistingUserCalled(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User can't be null");
        }
        log.debug("onAddExistingUserCalled callback: " + user);
        resetSelectedValues();
        selectedUser = user;
    }

    /**
     * Method called when a privileged user of this course should get edited.
     * @param privilegedUser The to be edited privileged User (not null);
     * @throws IllegalArgumentException If the privileged user is null.
     */
    public void onEditPrivilegedUserDialogCalled(PrivilegedUser privilegedUser) {
        if (privilegedUser == null) {
            throw new IllegalArgumentException("The PrivilegedUser can't be null.");
        }
        log.debug("onEditPrivilegedUserDialogCalled called: " + privilegedUser);
        selectedUser = privilegedUser.getUser();
        selectedRoleId = Role.PRIVILEGED_USER.getId();
        editedPrivilegedUser = privilegedUser;
        updateSelectedValuesFromPrivUser(editedPrivilegedUser);
        editedStudent = course.getStudentFromUser(selectedUser); // Can be null
        if (editedStudent != null) {
            log.debug("Selected privielged User also has a student association "
                    + "in this course, loading the values of it to the the selected values");
            updateSelectedValuesFromStudent(editedStudent);
        }
    }

    /**
     * Method called when a student of this course should get edited.
     * @param student The to be edited student (not null);
     * @throws IllegalArgumentException If the student is null.
     */
    public void onEditStudentDialogCalled(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student can't be null.");
        }
        selectedUser = student.getUser();
        editedStudent = student;
        selectedRoleId = Role.STUDENT.getId();
        updateSelectedValuesFromStudent(student);
        editedPrivilegedUser = course.getPrivilegedUserFromUser(selectedUser); // Can be null
        if (editedPrivilegedUser != null) {
            log.debug("Selected Student also has a privileged user association "
                    + "in this course, loading the values of it to the the selected values");
            updateSelectedValuesFromPrivUser(editedPrivilegedUser);
        }
    }

    /**
     * Method called when the deletion dialog opens for a lecturer.
     * @param lecturer The lecturer for which the deletion dialog gets opened.
     * @throws IllegalArgumentException If the lecturer is null.
     */
    public void onDeleteLecturerDialogCalled(Lecturer lecturer) {
        if (lecturer == null) {
            throw new IllegalArgumentException("The lecturer cant be null.");
        }
        log.debug("onDeleteLecturerDialogCalled: " + lecturer);
        resetSelectedValues();
        editedLecturer = lecturer;
        selectedUser = lecturer.getUser();
    }

    /**
     * Method called when the deletion dialog opens for a privileged user.
     * @param privilegedUser The privileged User for which the deletion dialog gets opened.
     * @throws IllegalArgumentException If the privileged user is null.
     */
    public void onDeletePrivilegedUserDialogCalled(PrivilegedUser privilegedUser) {
        if (privilegedUser == null) {
            throw new IllegalArgumentException("The privileged user can't be null.");
        }
        log.debug("onDeletePrivilegedUserDialogCalled: " + privilegedUser);
        resetSelectedValues();
        editedPrivilegedUser = privilegedUser;
        selectedUser = privilegedUser.getUser();
    }

    /**
     * Method called when the deletion dialog opens for a student.
     * @param student The privileged User for which the deletion dialog gets opened.
     * @throws IllegalArgumentException If the student is null.
     */
    public void onDeleteStudentDialogCalled(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student can't be null");
        }
        log.debug("onDeletePrivilegedUserDialogCalled" + student);
        resetSelectedValues();
        editedStudent = student;
        selectedUser = student.getUser();
    }

    /**
     * Creates a new user from the user input data.
     */
    public void createNewUser() {
        log.debug("createNewUser() called");


        //Cheap workaround so that one dialog can be used for adding existing users
        //and creating completely new ones
        boolean newToSystem = selectedUser.getUserId() == null;
        if (newToSystem) {
            logUserRegistedToSystem(selectedUser);
        } else {
            logExistingUserAddedToCourse(selectedUser);
        }

        if (selectedRoleId == Role.STUDENT.getId()) {
            Student student = createNewStudentFromInput();
            course.getStudents().add(student);
            logStudentCreated(student);
        } else if (selectedRoleId == Role.PRIVILEGED_USER.getId()) {
            PrivilegedUser privilegedUser = createNewPrivUserFromInput();
            course.getTutors().add(privilegedUser);
            logPrivilegedUserCreated(privilegedUser);
        }

        if (newToSystem) {
            selectedUser.addRole(GlobalRole.USER);
            selectedUser.setPassword(null);
            PasswordReset passwordReset = userService
                    .initPasswordReset(selectedUser, RESET_TOKEN_EXPIRATION);
            selectedUser.setToken(passwordReset);
            userService.persist(selectedUser);
        } else {
            selectedUser = userService.update(selectedUser);
        }
        course = courseService.update(course);

        if (newToSystem) {
            try {
                sendRegistrationMail();
            } catch (MessagingException | IOException e) {
                log.error(e);
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                bundle.getString("common.error"),
                                bundle.getString("passwordReset.mailFail")));
            }
        }
    }

    /**
     * Deletes the currently selection participation (lecturer, privileged user, student)
     * from the course.
     * @Pre EITHER editedStudent, editedPrivUser, or editedLecturer is not null;
     *      The validation for calling this method is done (e.g. on the ui by the selected user)
     * @Post The selected participation class is set to hidden, the selected values get rebeted.
     * @throws IllegalStateException If every selected participant class is null.
     */
    public void deleteSelectedParticipation() {
        if (editedStudent != null) {
            editedStudent.setDeleted(true);
            logStudentDeleted(editedStudent);
        } else if (editedLecturer != null) {
            editedLecturer.setDeleted(true);
            PrivilegedUser privilegedUser = course
                    .getPrivilegedUserFromUser(editedLecturer.getUser());
            logLecturerDeleted(editedLecturer);
            if (privilegedUser != null) {
                privilegedUser.setDeleted(true);
                logPrivilegedUserDeleted(privilegedUser);
            }
        } else if (editedPrivilegedUser != null) {
            editedPrivilegedUser.setDeleted(true);
            logPrivilegedUserDeleted(editedPrivilegedUser);
        } else {
            throw new IllegalStateException("There is no selected student,"
                   +  " privileged user nor lecturer.");
        }
        course = courseService.update(course);
        resetSelectedValues();
    }

    /**
     * Saves the changes from editing an existing user.
     * @Pre All user inputs for editing are validated (e.g. Name, Email etc.).
     */
    public void saveEditChanges() {
        log.debug("saveEditChanges called");

        if (selectedRoleId == Role.STUDENT.getId()) {
            saveEditToStudent();
        } else { // Assuming it is Role.PRIVILEGED_USER.getId();
            saveEditToPrivilegedUser();
        }
    }

    /**
     * Confirms a student to the course.
     * @param student An unconfirmed student which is added in the list of students
     *                of the course.
     * @throws IllegalArgumentException If the student is null.
     */
    public void confirmStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student can't be null");
        }
        log.debug("confirmStudent called: " + student);
        if (student.isConfirmed()) {
            log.error("Student is already confirmed, aborting");
            return;
        }
        student.setConfirmed(true);
        course = courseService.update(course);
        logConfirmStudent(student);
    }

    /**
     * Searches for users who are not in the course given the userTextInput.
     */
    public void searchForExternalUsers() {
        userSearchResultList.clear();

        if (userTextInput == null || userTextInput.trim().isEmpty()) {
            return;
        }

        List<User> rawUserSearchResults = userService.searchForUsers(userTextInput);
        userSearchResultList.addAll(rawUserSearchResults.stream()
            .filter(user -> !course.containsUser(user))
            .collect(Collectors.toList()));
    }


    /*
     * Validations
     */


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

        if (!(value instanceof String) || !Pattern.matches(Constants.EMAIL_REGEX, (String)value)) {
            messages.add(new FacesMessage(bundle
                    .getString("registration.invalidEmail")));
            log.debug("Value is not a string or is empty string, throwing exception");
            throw new ValidatorException(messages);
        }

        final String emailInput = (String) value;

        if (selectedUser != null && selectedUser.getUserId() != null
                && selectedUser.equals(userService.findByEmail(emailInput))) {
            // In this case, the selected User gets edited and has the
            // same email address, pass.
        } else if (userService.isEmailRegistered(emailInput)) {
            messages.add(new FacesMessage(bundle
                    .getString("registration.alreadyRegistered")));
            log.debug("Value is not a string or is empty string, throwing exception");
            throw new ValidatorException(messages);
        }
    }


    /*
     * Other public methods
     */


    /**
     * Resets the values which are associated with the views.
     * @Pre The course of this class has one not null participation type in the
     *      first position of its participation type list.
     */
    public void resetSelectedValues() {
        log.debug("resetSelectedValues() called");
        selectedUser = new User();
        privilegeExams = false;
        privilegeFormula = false;
        privilegeTutorials = false;
        privilegeExportData = false;
        privilegeStudents = false;
        privilegeGenerateCredits = false;
        isPrivilegedUserSecretary = false;
        selectedRoleId = Role.STUDENT.getId();
        selectedParticipationTypeId = course.getDefaultParticipationType().getPartTypeId();
        editedStudent = null;
        editedPrivilegedUser = null;
        editedLecturer = null;
        userTextInput = "";
        userSearchResultList.clear();
    }


    /*
     * Other private Methods
     */

    /**
     * Returns a boolean whether the logged in user can manage students.
     * The logged in user can manage students if the user is a lecturer
     * in the course or if the user is a privileged user in the course with the
     * privilege to manage students.
     * @return True if the logged in user can manage students, false otherwise.
     */
    private boolean canLoggedInUserCanManageStudents() {
        return loggedInUserCanManageTutors
                || (course.getPrivilegedUserFromUser(loggedInUser) != null
                && course.getPrivilegedUserFromUser(loggedInUser)
                .hasPrivilege(Privilege.ManageStudents.name()));
    }


    /**
     * Gets the ParticipationType given the selected Participation Type id
     * (the unique id of the Participation types).
     * @return The Participation Type given selected Participation Type id
     */
    private ParticipationType getParticipationTypeFromId() {
        for (ParticipationType partType: course.getParticipationTypes()) {
            if (partType.getPartTypeId() == selectedParticipationTypeId) {
                return partType;
            }
        }
        log.error("No participation type for found for id: " + selectedParticipationTypeId);
        log.error("Returning the first participation type");
        return course.getParticipationTypes().get(0);
    }

    /**
     * Creates a new student from the create dialog.
     */
    private Student createNewStudentFromInput() {
        log.debug("creatingNewStudent() called");
        Student student = new Student();
        student.setUser(selectedUser);
        student.setCourse(course);
        student.setDeleted(false);
        student.setAcceptedInvitation(false);
        student.setConfirmed(true);
        student.setTries(0);
        student.setDeleted(false);
        student.setParticipationType(getParticipationTypeFromId());
        return student;
    }

    /**
     * Creates a new privileged user from the create dialog.
     */
    private PrivilegedUser createNewPrivUserFromInput() {
        log.debug("createNewPrivUserFromInput() called");
        PrivilegedUser privilegedUser = new PrivilegedUser();
        privilegedUser.setUser(selectedUser);
        privilegedUser.setCourse(course);
        privilegedUser.setPrivileges(getPrivilegedIds());
        privilegedUser.setSecretary(isPrivilegedUserSecretary);
        privilegedUser.setDeleted(false);
        return privilegedUser;
    }

    /**
     * Saves the changes from an edit of an user in the case the selected role
     * of the user is a student.
     */
    private void saveEditToStudent() {
        log.debug("The to be saved role is a student association");

        if (editedStudent != null) {
            log.debug("Updating the values of the existing student association");
            editedStudent.setParticipationType(getParticipationTypeFromId());
            editedStudent.setDeleted(false);
            logOnEditStudent(editedStudent);
        } else {
            log.debug("Creating a new student from user input");
            Student student = createNewStudentFromInput();
            studentService.persist(student);
            course.getStudents().add(student);
            logCreateStudentForExistingUser(student);
        }

        if (editedPrivilegedUser != null) {
            log.debug("There is an old privileged user association, setting it to hidden");
            editedPrivilegedUser.setDeleted(true);
            logHidePrivilegedUserForStudent(editedPrivilegedUser);
        }

        selectedUser = userService.update(selectedUser);
        // Assuming CascadeType.MERGE for updating the student and privileged user
        course = courseService.update(course);
    }

    /**
     * Saves the changes from an edit of an user in the case the selected role
     * of the user is a privileged user.
     */
    private void saveEditToPrivilegedUser() {
        log.debug("The to be saved role is a privileged user association");

        if (editedPrivilegedUser != null) {
            log.debug("Updating the values of the existing privileged user association");
            editedPrivilegedUser.setDeleted(false);
            editedPrivilegedUser.setSecretary(isPrivilegedUserSecretary);
            editedPrivilegedUser.setPrivileges(getPrivilegedIds());
            logOnEditPrivUser(editedPrivilegedUser);
        } else {
            log.debug("Creating a new privileged user association from user input");
            PrivilegedUser privilegedUser = createNewPrivUserFromInput();
            privilegedUserService.persist(privilegedUser);
            course.getTutors().add(privilegedUser);
            logCreatePrivUserForExistingUser(privilegedUser);
        }

        if (editedStudent != null) {
            log.debug("There is an old student association, setting it to hidden");
            editedStudent.setDeleted(true);
            logHideStudentForPrivilegedUser(editedStudent);
        }

        // Assuming CascadeType.MERGE for updating the student and privileged user
        selectedUser = userService.update(selectedUser);
        course = courseService.update(course);
    }

    /**
     * Gets the list of privilege ids from the checked booleans
     * of this controller.
     * @return The list of privilege id from the checked booleans.
     */
    private List<Integer> getPrivilegedIds() {
        List<Integer> privilegeIds = new ArrayList<>();
        if (privilegeExams) {
            privilegeIds.add(Privilege.EditExams.getId());
        }
        if (privilegeExportData) {
            privilegeIds.add(Privilege.ExportData.getId());
        }
        if (privilegeFormula) {
            privilegeIds.add(Privilege.EditFormulas.getId());
        }
        if (privilegeStudents) {
            privilegeIds.add(Privilege.ManageStudents.getId());
        }
        if (privilegeTutorials) {
            privilegeIds.add(Privilege.ManageTutorials.getId());
        }
        if (privilegeGenerateCredits) {
            privilegeIds.add(Privilege.GenerateCredits.getId());
        }
        return privilegeIds;
    }

    /**
     * Updates the selected values, which can be changed by the user, to the
     * corresponding values of a student association class.
     * @param student The student which properties should be reflected
     *                in the selected values (not null);
     */
    private void updateSelectedValuesFromStudent(Student student) {
        log.debug("updateSelectedValuesFromStudent() called: " + student);
        selectedParticipationTypeId = student.getParticipationType().getPartTypeId();
    }

    /**
     * Sets the values to be selected by the user to values given a privileged user.
     * @param privilegedUser The privileged user for which properties
     *                       the selected values should be updated to (not null);
     */
    private void updateSelectedValuesFromPrivUser(PrivilegedUser privilegedUser) {
        log.debug("updateSelectedValuesFromPrivUser() called: " + privilegedUser);
        privilegeExams = privilegedUser.hasPrivilege(Privilege.EditExams.name());
        privilegeFormula = privilegedUser.hasPrivilege(Privilege.EditFormulas.name());
        privilegeTutorials = privilegedUser.hasPrivilege(Privilege.ManageTutorials.name());
        privilegeStudents = privilegedUser.hasPrivilege(Privilege.ManageStudents.name());
        privilegeExportData = privilegedUser.hasPrivilege(Privilege.ExportData.name());
        privilegeGenerateCredits = privilegedUser.hasPrivilege(Privilege.ExportData.name());

        isPrivilegedUserSecretary = privilegedUser.isSecretary();
    }

    /**
     * Sends a newly registered user an email that they are registered now.
     */
    private void sendRegistrationMail() throws MessagingException, IOException  {
        HttpServletRequest request = (HttpServletRequest)
                FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = userService.getPasswordResetURL(request, selectedUser,
                selectedUser.getToken());

        String registratorName = loggedInUser.getFirstName()
                + loggedInUser.getLastName();
        String fmt = bundle.getString("registration.mailByLecturer");
        String text = new MessageFormat(fmt).format(new Object[]{
                selectedUser.getFirstName(), registratorName,
                course.getName(), url});
        String subject = bundle.getString("passwordReset.mailSubject");
        selectedUser.sendEmail(subject, text);
    }


    /*
     * Private logging methods
     * These methods mostly log strings with the logService and are self-explanatory
     * because of their name and the logged string.
     */

    /**
     * Small wrapper method to persist logs with LogService.
     * Logs the participation actions with the logService, knowing that the
     * logged in User and the course stay the same.
     * @param description The description of the action, not null.
     */
    private void logAction(String description) {
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
    }


    private void logUserRegistedToSystem(User user) {
        logAction("The user " + user + " has been registered to the Exmatrikulator.");
    }

    private void logExistingUserAddedToCourse(User user) {
        logAction("The existing user " + user + " has been added to"
                + " the course " + course.getName() + ".");
    }

    private void logStudentCreated(Student student) {
        logAction("The user " + student.getUser() + " has been added to"
                + " the course " + course.getName() + " as Student"
                + " with participation type " + student.getParticipationType().getName()
                + ".");
    }

    private void logPrivilegedUserCreated(PrivilegedUser privilegedUser) {
        logAction("The user " + privilegedUser.getUser() + " has been added to"
                + " the course " + course.getName() + " as priviliged user.");
    }

    private void logOnEditStudent(Student student) {
        logAction("The values of the student"
                + student.getUser() + " have been changed "
                + "in the course " + course.getName() + ".");
    }

    private void logOnEditPrivUser(PrivilegedUser privilegedUser) {
        logAction("The values of the privileged user"
                + privilegedUser.getUser() + " have been changed "
                + "in the course " + course.getName() + ".");
    }

    private void logConfirmStudent(Student student) {
        logAction("The student " + student.getUser().toString()
                + " has been accepted to the course " + course.getName() + ".");
    }

    private void logStudentDeleted(Student student) {
        logAction("The student " + student.getUser().toString()
                + " has been hidden from the course: " + course.getName() + ".");
    }

    private void logPrivilegedUserDeleted(PrivilegedUser privilegedUser) {
        logAction("The privileged user " + privilegedUser.getUser().toString()
                + " has been hidden from the course: " + course.getName() + ".");
    }

    private void logLecturerDeleted(Lecturer lecturer) {
        logAction("The lecturer " + lecturer.getUser().toString()
                + " has been hidden from the course: " + course.getName() + ".");
    }

    private void logCreateStudentForExistingUser(Student student) {
        logAction("The existing user " + student.getUser() + " in the course"
                + course.getName() + " is now a student with participation type "
                + student.getParticipationType());
    }

    private void logHidePrivilegedUserForStudent(PrivilegedUser privilegedUser) {
        logAction("The privileged user role of the user " + privilegedUser.getUser()
                + " in the course " + course.getName() + " has been hidden for the"
                + " new student role of the user");
    }

    private void logCreatePrivUserForExistingUser(PrivilegedUser privUser) {
        logAction("The existing user " + privUser.getUser() + " in the course"
                + course.getName() + " is now a privileged user.");
    }

    private void logHideStudentForPrivilegedUser(Student student) {
        logAction("The student role of the user " + student.getUser()
                + " in the course " + course.getName() + " has been hidden for the"
                + " new privileged user role of the user");
    }


    /*
     * Getters and Setters
     */

    /**
     * Sets the LogService for this controller.
     * @param logService The LogService to be used in this controller.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Sets the course service for this controller, used for mocking.
     * @param courseService The courseService to be used for this controller.
     */
    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Sets the UserService for this controller, used for mocking.
     * @param userService The userService to be used for this controller.
     */
    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Sets the UserService for this controller, used for mocking.
     */
    @EJB
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Sets the UserService for this controller, used for mocking.
     * @param privilegedUserService The privileged user service to
     *                              be used by this controller.
     */
    @EJB
    public void setPrivilegedUserService(PrivilegedUserService privilegedUserService) {
        this.privilegedUserService = privilegedUserService;
    }

    public String getPaboGradeName(final String name) {
        return gradeService.paboGradeDisplayName(name);
    }

    public Course getCourse() {
        return this.course;
    }

    public void setCourse(final Course course) {
        this.course = course;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public int getSelectedRoleId() {
        return selectedRoleId;
    }

    public void setSelectedRoleId(int id) {
        this.selectedRoleId = id;
    }

    public boolean isPrivilegeFormula() {
        return privilegeFormula;
    }

    public void setPrivilegeFormula(boolean privilegeFormula) {
        this.privilegeFormula = privilegeFormula;
    }

    public boolean isPrivilegeExams() {
        return privilegeExams;
    }

    public void setPrivilegeExams(boolean privilegeExams) {
        this.privilegeExams = privilegeExams;
    }

    public boolean isPrivilegeStudents() {
        return privilegeStudents;
    }

    public void setPrivilegeStudents(boolean privilegeStudents) {
        this.privilegeStudents = privilegeStudents;
    }

    public boolean isPrivilegeExportData() {
        return privilegeExportData;
    }

    public void setPrivilegeExportData(boolean privilegeExportData) {
        this.privilegeExportData = privilegeExportData;
    }

    public boolean isPrivilegeTutorials() {
        return privilegeTutorials;
    }

    public void setPrivilegeTutorials(boolean privilegeTutorials) {
        log.debug("setPrivilegeTutorials called: " + privilegeTutorials);
        this.privilegeTutorials = privilegeTutorials;
    }

    public boolean isPrivilegedUserSecretary() {
        return isPrivilegedUserSecretary;
    }

    public void setPrivilegedUserSecretary(boolean privilegedUserSecretary) {
        isPrivilegedUserSecretary = privilegedUserSecretary;
    }

    public long getSelectedParticipationTypeId() {
        return selectedParticipationTypeId;
    }

    public void setSelectedParticipationTypeId(long selectedParticipationTypeId) {
        this.selectedParticipationTypeId = selectedParticipationTypeId;
    }

    public List<Student> getFilteredStudents() {
        return filteredStudents;
    }

    public void setFilteredStudents(List<Student> filteredStudents) {
        this.filteredStudents = filteredStudents;
    }

    public List<PrivilegedUser> getFilteredPrivUsers() {
        return filteredPrivUsers;
    }

    public void setFilteredPrivUsers(List<PrivilegedUser> filteredPrivUsers) {
        this.filteredPrivUsers = filteredPrivUsers;
    }

    public List<Lecturer> getFilteredLecturers() {
        return filteredLecturers;
    }

    public void setFilteredLecturers(List<Lecturer> filteredLecturers) {
        this.filteredLecturers = filteredLecturers;
    }

    public String getUserTextInput() {
        return userTextInput;
    }

    public void setUserTextInput(String userTextInput) {
        this.userTextInput = userTextInput;
    }

    public List<User> getUserSearchResultList() {
        return userSearchResultList;
    }

    public void setUserSearchResultList(List<User> userSearchResultList) {
        this.userSearchResultList = userSearchResultList;
    }

    public Lecturer getEditedLecturer() {
        return editedLecturer;
    }

    public void setEditedLecturer(Lecturer editedLecturer) {
        this.editedLecturer = editedLecturer;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public List<Student> getFilteredUnconfirmedStudents() {
        return filteredUnconfirmedStudents;
    }

    public void setFilteredUnconfirmedStudents(List<Student> filteredUnconfirmedStudents) {
        this.filteredUnconfirmedStudents = filteredUnconfirmedStudents;
    }

    public boolean isLoggedInUserCanManageStudents() {
        return loggedInUserCanManageStudents;
    }

    public void setLoggedInUserCanManageStudents(boolean loggedInUserCanManageStudents) {
        this.loggedInUserCanManageStudents = loggedInUserCanManageStudents;
    }

    public boolean isLoggedInUserCanManageTutors() {
        return loggedInUserCanManageTutors;
    }

    public void setLoggedInUserCanManageTutors(boolean loggedInUserCanManageTutors) {
        this.loggedInUserCanManageTutors = loggedInUserCanManageTutors;
    }

    public boolean isPrivilegeGenerateCredits() {
        return privilegeGenerateCredits;
    }

    public void setPrivilegeGenerateCredits(boolean privilegeGenerateCredits) {
        this.privilegeGenerateCredits = privilegeGenerateCredits;
    }
}