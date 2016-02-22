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
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LecturerService;
import de.unibremen.opensores.service.TutorialService;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.service.PrivilegedUserService;
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
     * Tutorial service for connecting to the database.
     */
    @EJB
    private transient TutorialService tutorialService;

    /**
     * PrivilegedUser service for connecting to the database.
     */
    @EJB
    private transient PrivilegedUserService privilegedUserService;

    /**
     * Lecturer service for connecting to the database.
     */
    @EJB
    private transient LecturerService lecturerService;

    /**
     * List of uploaded files by the user.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<UploadedFile> files = new ArrayList<>();

    /**
     * List of parsed TME objects.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<TMEObject> parsedObjs = new ArrayList<>();

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
                /* We only import courses directly everything else is imported
                 * indirectly using the relations from the course node. */

                if (obj.getName().equals("jgradebook.data.Course")) {
                    importCourse(obj);
                }
            } catch (TmeException e) {
                log.debug(e);
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_ERROR, bundle.getString("common.error"),
                    e.getMessage()));
                continue;
            }
        }

        facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_INFO, bundle.getString("common.success"),
                    bundle.getString("import.success")));
    }

    /**
     * Imports a course and associated TME objects.
     *
     * @param obj Course TME object.
     * @throws TmeException On a failed import.
     */
    private void importCourse(TMEObject obj) throws TmeException {
        String name = obj.getString("name");
        if (courseService.findCourseByName(name) != null) {
            return;
        }

        Course course = new Course();
        course.setName(name);
        course.setDefaultSws(obj.getString("wochenstunden"));
        course.setDefaultCreditPoints(obj.getInt("cp"));
        course.setRequiresConfirmation(false);
        course.setStudentsCanSeeFormula(true);

        Semester semester = createSemester(obj.getString("zeitraum"));
        course.setSemester(semester);

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

        courseService.persist(course);
        List<Tutorial> tutorials = createTutorials(
                obj.getArray("tutorials"), course);
        course.setTutorials(tutorials);

        course = courseService.update(course);
        log.debug("Persisted course " + course.getName());
    }

    /**
     * Returns a semester object form the given string.
     *
     * @param str String to use to create semester object.
     * @return Semester object.
     * @throws TmeException On invalid string format.
     */
    private Semester createSemester(String str) throws TmeException {
        Semester semester = null;
        try {
            semester = Semester.valueOf(str);
        } catch (SemesterFormatException e) {
            throw new TmeException("Invalid semester string format");
        }

        Semester sem = semesterService.findSemester(
                semester.getSemesterYear(), semester.isWinter());
        if (sem != null) {
            return sem;
        }

        semesterService.persist(semester);
        log.debug("Persist semester " + semester.toString());

        return semester;
    }

    /**
     * Creates all tutorials from the given TMEArray.
     *
     * @param array TMEArray to create tutorials from.
     * @param course Course the tutorials belong to.
     * @return List of created tutorials entities.
     * @throws TmeException On a failed creation.
     */
    private List<Tutorial> createTutorials(TMEArray array, Course course)
            throws TmeException {
        List<Tutorial> tutorials = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            int id = array.getInt(i);

            TMEObject node = findNode("jgradebook.data.Tutorial", id);
            if (node == null) {
                throw new TmeException("non-existend tutorial " + id);
            }

            Tutorial tutorial = createTutorial(node, course);
            tutorials.add(tutorial);
        }

        return tutorials;
    }

    /**
     * Creates a tutorial with the given id in the TME objects array.
     *
     * @param node TME tutorial object.
     * @param course Course the tutorial belongs to.
     * @return Created tutorial entity.
     * @throws TmeException On a failed creation.
     */
    private Tutorial createTutorial(TMEObject node, Course course)
            throws TmeException {
        int tutorId = node.getInt("tutor");
        TMEObject tutorNode = findNode("jgradebook.data.Teacher", tutorId);
        if (tutorNode == null) {
            throw new TmeException("non-existend tutor " + tutorId);
        }

        Tutorial tutorial = new Tutorial();
        tutorial.setCourse(course);
        tutorial.setName("jgradebook Tutorial " + node.getId());

        User user = createUser(tutorNode);
        if (tutorNode.getBoolean("superuser")) {
            Lecturer lecturer = new Lecturer();
            lecturer.setCourse(course);
            lecturer.setHidden(false);
            lecturer.setDeleted(false);
            lecturer.setIsCourseCreator(true);
            lecturer.setUser(user);

            lecturerService.persist(lecturer);
            course.getLecturers().add(lecturer);
        } else {
            PrivilegedUser tutor = new PrivilegedUser();
            tutor.setCourse(course);
            tutor.setSecretary(false);
            tutor.setUser(user);
            tutor.setHidden(false);
            tutor.setDeleted(false);
            tutor.getTutorials().add(tutorial);

            privilegedUserService.persist(tutor);
            tutorial.getTutors().add(tutor);
        }

        tutorialService.persist(tutorial);
        log.debug("Persisted tutorial " + tutorial.getName());

        return tutorial;
    }

    /**
     * Returns a user entity from the given TME object.
     *
     * @param obj StudentData TME object.
     * @return User entity.
     */
    private User createUser(TMEObject obj) {
        String email = obj.getString("email");
        User user = userService.findByEmail(email);
        if (user != null) {
            return user;
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(obj.getString("firstname"));
        newUser.setLastName(obj.getString("lastname"));

        // Teachers don't have a marticulation number
        if (obj.has("matriculationNumber")) {
            newUser.setMatriculationNumber(obj.getString("matriculationNumber"));
        } else {
            newUser.setMatriculationNumber(null);
        }

        String plainPasswd = obj.getString("password");
        newUser.setPassword(BCrypt.hashpw(plainPasswd, BCrypt.gensalt()));

        userService.persist(newUser);
        log.debug(String.format("Persisted new user '%s' (%s)",
                    newUser.toString(), newUser.getEmail()));

        return newUser;
    }

    /**
     * Finds the node with the given name and id in the TME objects list.
     *
     * @param name Node name.
     * @param id Node id.
     * @return Associated node or null.
     */
    private TMEObject findNode(String name, int id) {
        for (TMEObject obj : parsedObjs) {
            if (!obj.getName().equals(name)) {
                continue;
            } else if (obj.getId() == id) {
                return obj;
            }
        }

        return null;
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

    public List<UploadedFile> getFiles() {
        return files;
    }

    public void setFiles(List<UploadedFile> files) {
        this.files = files;
    }
}
