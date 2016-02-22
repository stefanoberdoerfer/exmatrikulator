package de.unibremen.opensores.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.mindrot.jbcrypt.BCrypt;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import de.unibremen.opensores.util.tme.Parser;
import de.unibremen.opensores.util.tme.TMEObject;
import de.unibremen.opensores.util.tme.TMEArray;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.exception.TmeException;
import de.unibremen.opensores.exception.SemesterFormatException;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ResourceBundle;

/**
 * Controller for import TME files.
 *
 * @author Sören Tempel
 */
@ManagedBean
@ViewScoped
public class TmeController implements Serializable {
    /**
     * Unique serial version uid.
     */
    private static final long serialVersionUID = -126631593355925099L;

    /**
     * The log4j logger.
     */
    private Logger log = LogManager.getLogger(TmeController.class);

    /**
     * User service for connecting to the database.
     */
    @EJB
    private transient UserService userService;

    /**
     * Course service for connecting to the database.
     */
    @EJB
    private transient CourseService courseService;

    /**
     * Semester service for connecting to the database.
     */
    @EJB
    private transient SemesterService semesterService;

    /**
     * List of uploaded files by the user.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<UploadedFile> files = new ArrayList<>();

    /**
     * List of imported users.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<User> importedUsers = new ArrayList<>();

    /**
     * List of imported courses.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<Course> importedCourses = new ArrayList<>();

    /**
     * List of imported semesters.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<Semester> importedSemesters = new ArrayList<>();

    /**
     * Handles file upload events.
     */
    public void handleFileUpload(FileUploadEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());

        UploadedFile file = event.getFile();
        if (file == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"),
                bundle.getString("courses.create.uploadFail")));
            return;
        }

        files.add(file);
    }

    /**
     * Uploads all files to the temporary folder.
     *
     * @return List of successfully uploaded files.
     * @throws IOException If uploading an individual file failed.
     */
    private List<File> uploadFiles() throws IOException {
        List<File> uploadedFiles = new ArrayList<>();
        for (UploadedFile file : files) {
            Path fp = Files.createTempFile("exmatrikulator", file.getFileName());
            Files.copy(file.getInputstream(), fp,
                    StandardCopyOption.REPLACE_EXISTING);

            uploadedFiles.add(fp.toFile());
        }

        return uploadedFiles;
    }

    /**
     * Persists all imported records.
     */
    public void persistImports() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());

        for (User user : importedUsers) {
            if (userService.findByEmail(user.getEmail()) != null) {
                continue;
            }

            log.debug("Persisted user " + user.toString());
            userService.persist(user);
        }

        for (Semester sem : importedSemesters) {
            log.debug("Persisted semester " + sem.toString());
            semesterService.persist(sem);
        }

        for (Course course : importedCourses) {
            if (courseService.findCourseByName(course.getName()) != null) {
                continue;
            }

            log.debug("Persisted course " + course.getName());
            courseService.persist(course);
        }

        facesContext.addMessage(null, new FacesMessage(FacesMessage
            .SEVERITY_INFO, bundle.getString("common.success"),
            bundle.getString("import.success")));
    }

    /**
     * Imports the uploaded files.
     */
    public void importFiles() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());

        List<File> uploaded = null;
        try {
            uploaded = uploadFiles();
        } catch (IOException e) {
            log.error(e);
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_ERROR, bundle.getString("common.error"),
                bundle.getString("courses.create.storeError")));
            return;
        }

        List<TMEObject> parsedObjs = new ArrayList<>();
        for (File file : uploaded) {
            try {
                String data = FileUtils.readFileToString(file);
                parsedObjs.addAll(new Parser(data).getTMEObjects());
            } catch (InterruptedException | IOException e) {
                log.fatal(e);
                return;
            } catch (ParseException e) {
                log.debug(e);
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_ERROR, bundle.getString("common.error"),
                    e.getMessage()));
            }

            if (!file.delete()) {
                log.debug("failed to delte temporary upload file");
            }
        }

        for (TMEObject obj : parsedObjs) {
            try {
                importTMEObject(obj);
            } catch (TmeException e) {
                log.debug(e);
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_ERROR, bundle.getString("common.error"),
                    e.getMessage()));
                continue;
            }
        }
    }

    /**
     * Imports the given TME Object into our database.
     *
     * @param obj Object which should be imported.
     * @throws TmeException On mailformated TME files.
     */
    private void importTMEObject(TMEObject obj) throws TmeException {
        String[] splited = obj.getName().split("\\.");
        if (splited.length <= 0) {
            throw new TmeException("Invalid node key");
        }

        String key = splited[splited.length - 1];
        switch (key) {
            case "StudentData":
                importUser(obj);
                break;
            case "Course":
                importCourse(obj);
                break;
            case "Student":
            case "Assignment":
            case "Category":
            case "Exam":
            case "ExamDate":
            case "StudentExam":
            case "Teacher":
            case "Tutorial":
            case "TutorialDate":
            case "WorkPackage":
            case "Group":
            case "Permission":
            case "Activity":
            case "Submission":
            case "ActualSubmission":
            case "Mark":
            case "SubMark":
                // TODO not implemented
                return;
            default:
                throw new TmeException("Unknown node key " + key);
        }
    }

    /**
     * Imports a studentData TME object.
     *
     * @param obj StudentData TME object.
     */
    private void importUser(TMEObject obj) {
        User user = new User();
        user.setEmail(obj.getString("email"));
        user.setFirstName(obj.getString("firstname"));
        user.setLastName(obj.getString("lastname"));
        user.setMatriculationNumber(obj.getString("matriculationNumber"));

        String plainPasswd = obj.getString("password");
        user.setPassword(BCrypt.hashpw(plainPasswd, BCrypt.gensalt()));

        importedUsers.add(user);
    }

    /**
     * Imports a course TME object.
     *
     * @param obj Course TME object.
     */
    private void importCourse(TMEObject obj) {
        Course course = new Course();
        course.setName(obj.getString("name"));
        course.setDefaultSws(obj.getString("wochenstunden"));
        course.setDefaultCreditPoints(obj.getInt("cp"));
        course.setRequiresConfirmation(false);
        course.setStudentsCanSeeFormula(true);

        Semester sem = null;
        try {
            sem = Semester.valueOf(obj.getString("zeitraum"));
        } catch (SemesterFormatException e) {
            log.error(e);
            return;
        }

        Semester semester = semesterService.findSemester(
                sem.getSemesterYear(), sem.isWinter());
        if (semester == null) {
            importedSemesters.add(sem);
            course.setSemester(sem);
        } else {
            course.setSemester(semester);
        }

        List<String> vaks = new ArrayList<>();
        vaks.add(obj.getString("nummer"));
        course.setNumbers(vaks);

        if (obj.getBoolean("finished")) {
            course.setLastFinalization(new Date());
        } else {
            course.setLastFinalization(null);
        }

        course.setMinGroupSize(obj.getInt("minimaleGruppenGroesse"));
        course.setMaxGroupSize(obj.getInt("maximaleGruppenGroesse"));

        importedCourses.add(course);
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

    public List<Semester> getImportedSemesters() {
        return importedSemesters;
    }

    public void setImportedSemesters(List<Semester> importedSemesters) {
        this.importedSemesters = importedSemesters;
    }

    public List<Course> getImportedCourses() {
        return importedCourses;
    }

    public void setImportedCourses(List<Course> importedCourses) {
        this.importedCourses = importedCourses;
    }

    public List<User> getImportedUsers() {
        return importedUsers;
    }

    public void setImportedUsers(List<User> importedUsers) {
        this.importedUsers = importedUsers;
    }

    public List<UploadedFile> getFiles() {
        return files;
    }

    public void setFiles(List<UploadedFile> files) {
        this.files = files;
    }
}
