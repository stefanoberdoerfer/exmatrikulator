package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.ServerProperties;
import de.unibremen.opensores.util.csv.StudIpParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * The Backing Bean of the Course create page 'Participants'.
 * This controls the fifth and last page of the courseCreate-Flow.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class StudentImportController {

    private static transient Logger log = LogManager.getLogger(StudentImportController.class);

    /**
     * The expiration duration of a reset token of the newly created user.
     */
    private static final int RESET_TOKEN_EXPIRATION = 14 * 24;

    /**
     * List of uploaded files by the user.
     */
    private List<UploadedFile> files = new ArrayList<>();

    /**
     * Folder Path to temporary file location specified in server properties.
     */
    private Path tempFileFolder;

    /**
     * The logged in user.
     */
    private User loggedInUser;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The UserService for searching in all lecturers.
     */
    private UserService userService;

    /**
     * Messages ResourceBundle for localised text.
     */
    private transient ResourceBundle bundle;

    /**
     * Boolean if sending Mails produces errors only once.
     */
    private boolean mailError;

    /**
     * Flag to indicate that this controller was initiated from the courseCreateWizard.
     * No db transactions have to be made if this attribute is 'true'.
     */
    private boolean calledFromWizard;

    /**
     * The FacesContext of the bean, provides ResourceBundles, HTTPRequests etc.
     */
    private FacesContext context;

    private Course course;

    private List<User> usersToBeCreated;

    private List<Student> studentsToBeAdded;

    /**
     * Initialises the StudentImportController Bean.
     * Gets the fileupload path from config.properties and tries to create the
     * specified directory and any necessary but nonexistent parent directories.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)
                context.getExternalContext().getRequest();
        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        course = courseService.findCourseById(req.getParameter("course-id"));
        log.debug("Loaded course object: " + course);

        loggedInUser = (User) context.getExternalContext()
                .getSessionMap().get(Constants.SESSION_MAP_KEY_USER);

        usersToBeCreated = new ArrayList<>();
        studentsToBeAdded = new ArrayList<>();

        //Properties
        Properties properties = null;

        try {
            properties = ServerProperties.getProperties();
        } catch (IOException ex) {
            log.error("config.properties could not be loaded");
            log.error(ex);
        }

        if (properties != null) {
            final String strRootPath = properties.getProperty("fileupload.root","");
            final String strTempPath = properties.getProperty("fileupload.tempfolder","");

            //check properties
            if (strRootPath.trim().isEmpty() || strTempPath.trim().isEmpty()) {
                log.info("One or more fileupload paths in config.properties"
                       + "are empty or not declared");
            }

            //build path
            try {
                tempFileFolder = Paths.get(strRootPath,strTempPath);
            } catch (InvalidPathException ex) {
                log.error(ex);
            }

            //mkdir
            if (!Files.exists(tempFileFolder)) {
                try {
                    Files.createDirectories(tempFileFolder);
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        }
    }

    /**
     * Method used to handle an uploaded file.
     * Given file will be added to the list of uploaded files.
     */
    public void handleFileUpload(FileUploadEvent event) {
        log.debug("Upload action called");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        if (event.getFile() != null && tempFileFolder != null) {
            log.debug("Upload success");
            files.add(event.getFile());
        } else {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("courses.create.uploadFail")));
        }
    }

    /**
     * Public method invoked from the UI to start importing Users from the uploaded .csv files.
     * Adds Users who don't already have an account to a special list in the FlowController and
     * adds all Users as Students to the to be created Course.
     */
    public void executeImport() {
        log.debug("executeImport called");
        List<File> storedFiles = storeFilesOnServer();
        List<User> importedUsers = new ArrayList<>();
        for (File f : storedFiles) {
            try {
                importedUsers.addAll(StudIpParser.parseCSV(f));
            } catch (IOException | IllegalArgumentException ex) {
                log.error(ex);
            }
        }

        log.debug("Total Number of newly to be created Users: " + usersToBeCreated.size());

        addUsersToCourse(importedUsers);
    }

    private List<File> storeFilesOnServer() {
        log.debug("storeFilesOnServer called");
        List<File> storedFiles = new ArrayList<>();
        for (UploadedFile f : files) {
            String filename = FilenameUtils.getBaseName(f.getFileName());
            String extension = FilenameUtils.getExtension(f.getFileName());
            try {
                Path fileP = Files.createTempFile(tempFileFolder, filename + "-", "." + extension);
                Files.copy(f.getInputstream(), fileP, StandardCopyOption.REPLACE_EXISTING);
                log.debug("Successfully saved File: " + filename + "." + extension);
                storedFiles.add(fileP.toFile());
            } catch (IOException ex) {
                log.error(ex);
                log.debug("Failed to save File: " + filename + "." + extension);
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ResourceBundle bundle = ResourceBundle.getBundle("messages",
                        facesContext.getViewRoot().getLocale());
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_ERROR, bundle.getString("common.error"),
                        bundle.getString("courses.create.storeError")));
            }
        }
        return storedFiles;
    }

    /**
     * Adds given Users to the to be created course. Users without existing
     * account will be added to the 'usersToBeCreated' list of the FlowController.
     *
     * @param users list of users which should be added as participants
     *              to the course
     */
    private void addUsersToCourse(List<User> users) {
        checkCourseIsNull();
        for (User u : users) {
            User dbUser = userService.findByEmail(u.getEmail());

            //Existing user check via email
            if (dbUser == null) {
                usersToBeCreated.add(u);
            } else {
                u = dbUser;
            }

            //Creating new Student and add him to the course
            Student student = new Student();
            student.setUser(u);
            student.setCourse(course);
            student.setHidden(false);
            student.setAcceptedInvitation(false);
            student.setConfirmed(true);
            student.setDeleted(false);
            student.setTries(0);

            if (calledFromWizard) {
                course.getStudents().add(student);
            } else {
                studentsToBeAdded.add(student);
            }
        }
    }

    /**
     * Method to redirect if needed Course-object is null. Check can't be placed in
     * the initialisation method because in die CourseCreateWizard the Course-object will
     * get set from a wrapping '-StepController'.
     */
    private void checkCourseIsNull() {
        if (course == null) {
            try {
                ((HttpServletResponse) context.getExternalContext().getResponse())
                        .sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e) {
                log.fatal(e);
            }
        }
    }

    /**
     * Method to save all newly created Students to the course.
     * Can only be called from the Settings page (not from Wizard)
     */
    public void saveStudents() {
        if (calledFromWizard) {
            return;
        }

        //persist new users
        usersToBeCreated.stream().forEach(this::createNewUser);

        //set default ParticipationType and add all students
        //which are not already in the course
        ParticipationType defaultParttype = course.getDefaultParticipationType();
        for (Student s : studentsToBeAdded) {
            if (!course.containsUser(s.getUser())) {
                s.setParticipationType(defaultParttype);
                defaultParttype.getStudents().add(s);
                course.getStudents().add(s);
            }
        }

        courseService.update(course);
        studentsToBeAdded.clear();
        usersToBeCreated.clear();
    }

    /**
     * Created a new User in the system and tries to send an password reset
     * mail to him/her.
     * @param newUser User object to be persisted
     */
    private void createNewUser(User newUser) {
        PasswordReset passwordReset = userService
                .initPasswordReset(newUser, RESET_TOKEN_EXPIRATION);
        newUser.setToken(passwordReset);
        userService.persist(newUser);
        try {
            sendRegistrationMail(newUser);
        } catch (IOException | MessagingException ex) {
            if (!mailError) {
                log.error(ex);
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                bundle.getString("common.error"),
                                bundle.getString("passwordReset.mailFail")));
                mailError = true;
            }
        }
    }

    /**
     * Sends a newly registered user an email that they are registered now.
     */
    private void sendRegistrationMail(User newUser) throws MessagingException, IOException {
        HttpServletRequest request = (HttpServletRequest)
                FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = userService.getPasswordResetURL(request, newUser,
                newUser.getToken());

        String registratorName = loggedInUser.toString();
        String fmt = bundle.getString("registration.mailByLecturer");
        String text = new MessageFormat(fmt).format(new Object[]{
                newUser.getFirstName(), registratorName,
                course.getName(), url});
        String subject = bundle.getString("passwordReset.mailSubject");
        newUser.sendEmail(subject, text);
    }

    /**
     * Checks if a given student is in the list of users to be created.
     * Is used by the JSF UI to display a beautiful datatable.
     *
     * @param student student to get checked
     * @return '✘' if student is in the usersToBeCreated List; '✔' otherwise
     */
    public String isStudentWithExistingAccount(Student student) {
        return usersToBeCreated.contains(student.getUser()) ? "✘" : "✔";
    }

    /**
     * Converts a filesize from simple long to human readable display-value.
     *
     * @param fsize long value representing a filesize in bytes
     * @return Human readable string representation
     */
    public String getHumanReadableFileSize(long fsize) {
        return FileUtils.byteCountToDisplaySize(fsize);
    }

    /**
     * Removes a file from the list of currently uploaded files.
     */
    public void removeFile(UploadedFile file) {
        files.remove(file);
    }

    /**
     * Injects the logService into CommonDataController.
     * @param logService The logService to be injected.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Injects the user service to the CommonDataController.
     * @param userService The user service to be injected to the bean.
     */
    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Injects the course service to the ExamController.
     * @param courseService The course service to be injected to the bean.
     */
    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public List<UploadedFile> getFiles() {
        return files;
    }

    public void setFiles(List<UploadedFile> files) {
        this.files = files;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<User> getUsersToBeCreated() {
        return usersToBeCreated;
    }

    public void setUsersToBeCreated(List<User> usersToBeCreated) {
        this.usersToBeCreated = usersToBeCreated;
    }

    public boolean isCalledFromWizard() {
        return calledFromWizard;
    }

    public void setCalledFromWizard(boolean calledFromWizard) {
        this.calledFromWizard = calledFromWizard;
    }

    public List<Student> getStudentsToBeAdded() {
        return studentsToBeAdded;
    }

    public void setStudentsToBeAdded(List<Student> studentsToBeAdded) {
        this.studentsToBeAdded = studentsToBeAdded;
    }
}
