package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;
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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
public class StudentImportController implements Serializable {

    private static transient Logger log = LogManager.getLogger(StudentImportController.class);

    private static final long serialVersionUID = -4126282231974680267L;

    /**
     * List of uploaded files by the user.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "transient or not - both generates errors. Needs to be transient.")
    private transient List<UploadedFile> files = new ArrayList<>();

    /**
     * Folder Path to temporary file location specified in server properties.
     */
    private transient Path tempFileFolder;

    @EJB
    private transient UserService userService;

    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    @ManagedProperty("#{courseCreateFlowController.usersToBeCreated}")
    private transient List<User> usersToBeCreated;

    /**
     * Initialises the StudentImportController Bean.
     * Gets the fileupload path from config.properties and tries to create the
     * specified directory and any necessary but nonexistent parent directories.
     */
    @PostConstruct
    public void init() {
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
        for (User u : users) {
            User dbUser = userService.findByEmail(u.getEmail());

            //Existing user check via email
            if (dbUser == null) {
                usersToBeCreated.add(u);
            } else {
                u = dbUser;
            }

            //Creating new Student and add him to the couse
            Student student = new Student();
            student.setUser(u);
            student.setCourse(course);
            student.setHidden(false);
            student.setAcceptedInvitation(false);
            student.setConfirmed(true);
            student.setDeleted(false);
            student.setTries(0);

            course.getStudents().add(student);
        }
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
}
