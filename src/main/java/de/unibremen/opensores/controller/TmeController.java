package de.unibremen.opensores.controller;

import de.unibremen.opensores.util.DateUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.mindrot.jbcrypt.BCrypt;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.tme.Parser;
import de.unibremen.opensores.util.tme.TMEObject;
import de.unibremen.opensores.util.tme.TMEArray;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.GroupService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.TutorialService;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.exception.TmeException;
import de.unibremen.opensores.exception.SemesterFormatException;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
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
     * Student service for connecting to the database.
     */
    @EJB
    private transient StudentService studentService;

    /**
     * Group service for connecting to the database.
     */
    @EJB
    private transient GroupService groupService;

    /**
     * List of uploaded files by the user.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<UploadedFile> files = new ArrayList<>();

    /**
     * Maps TME ids to JPA entities.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient HashMap<Integer, Object> entityMap = new HashMap<>();

    /**
     * Maps TME ids to TME objects.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient HashMap<Integer, TMEObject> nodeMap = new HashMap<>();

    /**
     * List of imported courses.
     */
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
            justification = "actually findbugs is right this needs to be "
            + "serializable but I am too lazy to fix it")
    private transient List<Course> importedCourses = new ArrayList<>();


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

        List<TMEObject> objs = new ArrayList<>();
        for (File file : uploaded) {
            try {
                String data = FileUtils.readFileToString(file);
                objs.addAll(new Parser(data).getTMEObjects());
            } catch (InterruptedException | IOException e) {
                log.fatal(e);
                return;
            } catch (ParseException e) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_ERROR, bundle.getString("common.error"),
                    String.format("Syntax error '%s' in line %d", e.getMessage(),
                        e.getErrorOffset())));
            }

            if (!file.delete()) {
                log.debug("failed to delte temporary upload file");
            }
        }

        String msg = null;
        try {
            importObjects(objs);
        } catch (TmeException e) {
            msg = e.getMessage();
        } catch (ClassCastException e) {
            msg = bundle.getString("import.unexpectedType");
        } catch (NoSuchElementException e) {
            msg = bundle.getString("import.nonExistingNode");
        }

        if (msg != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
        } else {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("import.success")));
        }
    }

    /**
     * Imports the given list of TMEObjects.
     *
     * @param objs TMEObject to import.
     * @throws TmeException On a failed import.
     */
    private void importObjects(List<TMEObject> objs)
            throws TmeException {
        for (TMEObject obj : objs) {
            int id = obj.getId();
            if (nodeMap.containsKey(id)) {
                throw new TmeException("Duplicated id " + id);
            } else {
                nodeMap.put(id, obj);
            }
        }

        for (TMEObject obj : objs) {
            String[] splited = obj.getName().split("\\.");
            if (splited.length <= 0) {
                throw new TmeException("Invalid node key");
            }

            String key = splited[splited.length - 1];
            switch (key) {
                case "Course":
                    createCourse(obj);
                    break;
                case "Tutorial":
                    createTutorial(obj);
                    break;
                case "Teacher":
                case "StudentData":
                    createUser(obj);
                    break;
                case "Student":
                case "Assignment":
                case "Category":
                case "Exam":
                case "ExamDate":
                case "StudentExam":
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
                    break;
                default:
                    log.debug("Didn't recongize key " + key);
            }
        }
    }

    /**
     * Imports a course and associated TME objects.
     *
     * @param node Course TME object.
     * @return Imported Course.
     * @throws TmeException On a failed import.
     */
    private Course createCourse(TMEObject node) throws TmeException {
        Object obj = entityMap.get(node.getId());
        if (obj != null) {
            return (Course) obj;
        }

        String name = node.getString("name");
        Semester semester = createSemester(node.getString("zeitraum"));

        Course exc = courseService.findCourseByName(name, semester);
        if (exc != null) {
            return exc;
        }

        Course course = new Course();
        course.setName(name);
        course.setDefaultSws(node.getString("wochenstunden"));
        course.setDefaultCreditPoints(node.getInt("cp"));
        course.setRequiresConfirmation(false);
        course.setStudentsCanSeeFormula(true);
        course.setCreated(DateUtil.getDateTime());

        //super safe identifier collisionhandling
        String randomIdentifier;
        do {
            randomIdentifier = RandomStringUtils.randomAlphabetic(4);
        } while (courseService.findCourseByIdentifier(randomIdentifier) != null);
        course.setIdentifier(randomIdentifier);

        ParticipationType type = new ParticipationType();
        type.setName(node.getString("studyArea"));
        type.setGroupPerformance(node.getBoolean("groupPerformance"));
        type.setRestricted(false);
        type.setSws(null);
        type.setCreditPoints(null);
        type.setIsDefaultParttype(true);
        type.setCourse(course);

        User currentUser = (User) FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap()
            .get(Constants.SESSION_MAP_KEY_USER);

        GradeFormula formular = new GradeFormula();
        formular.setFormula(Constants.DEFAULT_GRADE_SCRIPT);
        formular.setEditDescription(Constants.DEFAULT_SCRIPT_EDIT_MESSAGE);
        formular.setEditor(currentUser);
        formular.setSaveDate(DateUtil.getDateTime());
        formular.setValid(false);
        formular.setParticipationType(type);
        type.getGradeFormulas().add(formular);

        course.getParticipationTypes().add(type);
        course.setSemester(semester);

        List<String> vaks = new ArrayList<>();
        vaks.add(node.getString("nummer"));
        course.setNumbers(vaks);

        if (node.getBoolean("finished")) {
            course.setLastFinalization(new Date());
        } else {
            course.setLastFinalization(null);
        }

        course.setMinGroupSize(node.getInt("minimaleGruppenGroesse"));
        course.setMaxGroupSize(node.getInt("maximaleGruppenGroesse"));

        courseService.persist(course);
        createGroups(node.getArray("groups"), course);

        course = courseService.update(course);
        log.debug("Persisted course " + course.getName());

        importedCourses.add(course);
        entityMap.put(node.getId(), course);

        return course;
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
     * Creates all groups from the given TMEArray.
     *
     * @param array TMEArray to create groups from.
     * @param course Course tho groups belong to.
     * @return List of created group entities.
     * @throws TmeException On a failed creation.
     */
    private List<Group> createGroups(TMEArray array, Course course)
            throws TmeException {
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            int id = array.getInt(i);

            TMEObject node = findNode("jgradebook.data.Group", id);
            if (node == null) {
                throw new TmeException("non-existend group " + id);
            }

            Group group = createGroup(node, course);
            groups.add(group);
        }

        return groups;
    }

    /**
     * Creates a group from the given TME object in the given course.
     *
     * @param node TME group object.
     * @param course Course the group belongs to.
     * @return Created group entity.
     * @throws TmeException On a failed creation.
     */
    private Group createGroup(TMEObject node, Course course)
            throws TmeException {
        Object obj = entityMap.get(node.getId());
        if (obj != null) {
            return (Group) obj;
        }

        int tutorialId = node.getInt("tutorial");
        TMEObject tutorialNode = findNode("jgradebook.data.Tutorial", tutorialId);
        if (tutorialNode == null) {
            throw new TmeException("non-existend tutorial " + tutorialId);
        }

        Tutorial tutorial = createTutorial(tutorialNode, course);
        Group group = new Group();
        group.setName(node.getString("name"));
        group.setTutorial(tutorial);
        groupService.persist(group);

        tutorial.getGroups().add(group);
        List<Student> students = createStudents(node.getArray("students"),
                group, course);
        group.setStudents(students);

        course.getStudents().addAll(students);
        courseService.update(course);

        group = groupService.update(group);
        log.debug("Created group " + group.getName());

        entityMap.put(node.getId(), group);
        return group;
    }

    /**
     * Creates all students from the given TMEArray.
     *
     * @param array TMEArray to create students from.
     * @param group Group the students belong to.
     * @param course Course the students belong to.
     * @return List of created student entities.
     * @throws TmeException On a failed creation.
     */
    private List<Student> createStudents(TMEArray array, Group group, Course course)
            throws TmeException {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            int id = array.getInt(i);

            TMEObject node = findNode("jgradebook.data.Student", id);
            if (node == null) {
                throw new TmeException("non-existend student " + id);
            }

            Student student = createStudent(node, group, course);
            students.add(student);
        }

        return students;
    }

    /**
     * Creates a student from the given TME object in the given group.
     *
     * @param node TME student object.
     * @param group Group the student belongs to.
     * @param course Course the student belongs to.
     * @return Created Student entity.
     * @throws TmeException On a failed creation.
     */
    private Student createStudent(TMEObject node, Group group, Course course)
            throws TmeException {
        Object obj = entityMap.get(node.getId());
        if (obj != null) {
            return (Student) obj;
        }

        int id = node.getInt("studentData");
        TMEObject studentData = findNode("jgradebook.data.StudentData", id);
        if (studentData == null) {
            throw new TmeException("non-existend studentData " + id);
        }

        User user = createUser(studentData);
        Student student = new Student();
        student.setGroup(group);
        student.setCourse(course);
        student.setTutorial(group.getTutorial());

        student.setUser(user);
        student.setDeleted(false);
        student.setHidden(false);
        student.setTries(0);
        student.setParticipationType(course.getDefaultParticipationType());

        if (node.getString("status") == "CONFIRMED") {
            student.setAcceptedInvitation(true);
            student.setConfirmed(true);
        } else {
            student.setAcceptedInvitation(false);
            student.setConfirmed(false);
        }

        student.setPaboGrade(null);
        student.setPublicComment(null);

        if (node.has("comment")) {
            student.setPrivateComment(node.getString("comment"));
        } else {
            student.setPrivateComment(null);
        }

        studentService.persist(student);
        log.debug("Persisted student " + student.getUser());

        entityMap.put(node.getId(), student);
        return student;
    }

    /**
     * Creates a tutorial from the given TME object. The associated
     * course is copied from the entity map.
     *
     * @pre Associated course needs to be in the entityMap.
     * @param node TME tutorial object.
     * @return Created tutorial entity.
     * @throws TmeException On a failed creation.
     */
    private Tutorial createTutorial(TMEObject node)
            throws TmeException {
        int courseId = node.getInt("course");
        Object courseObj = entityMap.get(courseId);
        if (courseObj == null) {
            throw new TmeException(String.format("Course node %d needs to "
                        + "be declared before tutorial node %d",
                        courseId, node.getId()));
        }

        return createTutorial(node, (Course) courseObj);
    }


    /**
     * Creates a tutorial from the given TME object in the given course.
     *
     * @param node TME tutorial object.
     * @param course Course the tutorial belongs to.
     * @return Created tutorial entity.
     * @throws TmeException On a failed creation.
     */
    private Tutorial createTutorial(TMEObject node, Course course)
            throws TmeException {
        Object obj = entityMap.get(node.getId());
        if (obj != null) {
            return (Tutorial) obj;
        }

        int tutorId = node.getInt("tutor");
        TMEObject tutorNode = findNode("jgradebook.data.Teacher", tutorId);
        if (tutorNode == null) {
            throw new TmeException("non-existend tutor " + tutorId);
        }

        Tutorial tutorial = new Tutorial();
        tutorial.setCourse(course);
        tutorial.setName("jgradebook Tutorial " + node.getId());

        User user = createUser(tutorNode);
        PrivilegedUser tutor = privilegedUserService.findPrivUserInCourse(
                user, course);

        if (tutor == null) {
            tutor = new PrivilegedUser();
            tutor.setCourse(course);
            tutor.setSecretary(false);
            tutor.setUser(user);
            tutor.setHidden(false);
            tutor.setDeleted(false);

            course.getTutors().add(tutor);
            tutor.getTutorials().add(tutorial);
            privilegedUserService.persist(tutor);
        }

        course.getTutorials().add(tutorial);
        tutorial.getTutors().add(tutor);
        tutorialService.persist(tutorial);

        courseService.update(course);
        log.debug("Persisted tutorial " + tutorial.getName());

        entityMap.put(node.getId(), tutorial);
        return tutorial;
    }

    /**
     * Returns a user entity from the given TME object.
     *
     * @param node StudentData or Teacher TME object.
     * @return User entity.
     */
    private User createUser(TMEObject node) {
        Object obj = entityMap.get(node.getId());
        if (obj != null) {
            return (User) obj;
        }

        String email = node.getString("email");
        User user = userService.findByEmail(email);
        if (user != null) {
            entityMap.put(node.getId(), user);
            return user;
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(node.getString("firstname"));
        newUser.setLastName(node.getString("lastname"));
        newUser.setLastActivity(DateUtil.getDateTime());

        // Teachers don't have a marticulation number
        if (node.has("matriculationNumber")) {
            newUser.setMatriculationNumber(node.getString("matriculationNumber"));
        } else {
            newUser.setMatriculationNumber(null);
        }

        String plainPasswd = node.getString("password");
        newUser.setPassword(BCrypt.hashpw(plainPasswd, BCrypt.gensalt()));

        newUser.addRole(GlobalRole.USER);
        if (node.has("superuser") && node.getBoolean("superuser")) {
            newUser.addRole(GlobalRole.LECTURER);

            Lecturer lecturer = new Lecturer();
            lecturer.setHidden(false);
            lecturer.setDeleted(false);
            lecturer.setIsCourseCreator(false);
            lecturer.setUser(newUser);

            PrivilegedUser privUser = new PrivilegedUser();
            privUser.setSecretary(false);
            privUser.setUser(newUser);
            privUser.setHidden(false);

            for (Course course : importedCourses) {
                lecturer.setCourse(course);
                privUser.setCourse(course);

                course.getLecturers().add(lecturer);
                course.getTutors().add(privUser);

                if (course.getLecturers().size() >= 1) {
                    course.getLecturers().get(0).setIsCourseCreator(true);
                }
            }
        }

        userService.persist(newUser);
        log.debug(String.format("Persisted new user '%s' (%s)",
                    newUser.toString(), newUser.getEmail()));

        entityMap.put(node.getId(), newUser);
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
        TMEObject node = nodeMap.get(id);
        if (node == null) {
            return null;
        }

        return (node.getName().equals(name)) ? node : null;
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
     *
     * @param file File which should be removed.
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
}
