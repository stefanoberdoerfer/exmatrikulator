package de.unibremen.opensores.controller.common;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.MailTemplate;
import de.unibremen.opensores.model.PaboData;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.RecordBookEntry;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.Upload;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.BackupService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.ParticipationTypeService;
import de.unibremen.opensores.service.RecordBookService;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UploadService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.DateUtil;
import de.unibremen.opensores.util.ServerProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 * Startup controller, creates dummy data.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class ApplicationController {

    @EJB
    private BackupService backup;

    @EJB
    private GradingService gradingService;

    @EJB
    private UserService userService;

    @EJB
    private CourseService courseService;

    @EJB
    private StudentService studentService;

    @EJB
    private SemesterService semesterService;

    @EJB
    private UploadService uploadService;

    @EJB
    private LogService logService;

    @EJB
    private ParticipationTypeService participationTypeService;

    @EJB
    private RecordBookService recordBookService;

    /**
     * Map of courseIds to currently logged in users
     * with rights to edit this course (Lecturers and PrivilegedUsers).
     */
    private ConcurrentHashMap<Long,List<User>> sessionRegister;

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(ApplicationController.class);

    /**
     * Initialisation method of whole the Exmatrikulator Application.
     * Gets called on application start.
     * Intialises the datastructures needed to show the status of currently
     * logged in users with writing rights per course.
     */
    @PostConstruct
    public void init() {
        //initialize map used to determine the current login status of users
        //with writing rights in all the different courses of the Exmatrikulator system
        sessionRegister = new ConcurrentHashMap<>();
        for (Course c : courseService.listCourses()) {
            sessionRegister.put(c.getCourseId(),Collections.synchronizedList(new ArrayList<>()));
        }

        initSemesters();
        initAdmin();
    }

    /**
     * Creates the first admin user, needs to be set in config.properties.
     */
    public void initAdmin() {
        String mail = null;
        String pass = null;
        String frst = null;
        String last = null;

        try {
            Properties props = ServerProperties.getProperties();
            mail = props.getProperty("install.admin.mail");
            pass = props.getProperty("install.admin.pass");
            frst = props.getProperty("install.admin.frst");
            last = props.getProperty("install.admin.last");
        } catch (final IOException e) {
            log.error("Could not create default admin " + e.toString());
        }

        if (mail == null) {
            log.error("Could not determine default admin mail");
            return;
        }

        if (!userService.isEmailRegistered(mail)) {
            if (pass == null) {
                log.error("Could not determine default admin pass.");
                return;
            }
            final User newAdmin = new User();
            newAdmin.setEmail(mail);
            newAdmin.setFirstName(frst);
            newAdmin.setLastName(last);
            newAdmin.setPassword(BCrypt.hashpw(pass, BCrypt.gensalt()));
            newAdmin.addRole(GlobalRole.ADMIN);
            newAdmin.addRole(GlobalRole.USER);
            newAdmin.setLastActivity(DateUtil.getDateTime());
            userService.persist(newAdmin);
            log.debug("creating new admin, with user " + mail + " and pass " + pass);
        } else {
            log.debug("Admin already existing, skipping creation of default admin.");
        }
    }

    /**
     * Method gets called for every newly started session in the Exmatrikulator system.
     * The sessionRegister of courses and their corresponding users with edit-rights gets updated
     * with the new users LECTURER or PRIVILEGED_USER course->role relations.
     * @param user Newly logged in user object
     * @param courseRoleMap map of the users courses to his corresponding role
     */
    public void registerSession(User user, Map<Course, Role> courseRoleMap) {
        log.debug("Registering session of: " + user);
        for (Map.Entry<Course,Role> entry : courseRoleMap.entrySet()) {
            Role role = entry.getValue();
            if (role == Role.PRIVILEGED_USER || role == Role.LECTURER) {
                Course c = entry.getKey();
                log.debug("Adding " + user + " to sessionMap entry for " + c.getName());
                if (sessionRegister.get(c.getCourseId()) == null) {
                    sessionRegister.putIfAbsent(c.getCourseId(),
                            Collections.synchronizedList(new ArrayList<>()));
                }
                sessionRegister.get(c.getCourseId()).add(user);
            }
        }
    }

    /**
     * Method gets called when the user session expired or was invalidated (logout).
     * Removes all occurrences of the user in the sessionRegister to clear his logged-in
     * status completely.
     * @param user User object with invalid session after this hook
     * @param courseRoleMap map of the users courses to his corresponding role
     */
    public void unregisterSession(User user, Map<Course, Role> courseRoleMap) {
        log.debug("Unregistering session of: " + user);
        for (Map.Entry<Course,Role> entry : courseRoleMap.entrySet()) {
            Role role = entry.getValue();
            if (role == Role.PRIVILEGED_USER || role == Role.LECTURER) {
                Course c = entry.getKey();
                if (sessionRegister.get(c.getCourseId()) != null) {
                    log.debug("Removing " + user + " from sessionMap entry for " + c.getName());
                    sessionRegister.get(c.getCourseId()).removeIf(u -> u.equals(user));
                }
            }
        }
    }

    /**
     * Returns the list of Users with editor privileges in the course specified with the
     * courseId.
     * @param courseId Id to specified the course which editors should be returned
     * @return List of editors for the specified course. Empty list if there aren't any.
     */
    public List<User> getEditorSessions(Long courseId) {
        List<User> editors = sessionRegister.get(courseId);
        return editors != null ? editors : new ArrayList<>();
    }

    /**
     * Initilializes the next 10 semesters in advance.
     */
    public void initSemesters() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);

        if (semesterService.findSemester(year, false) == null) {
            Semester soSe = new Semester();
            soSe.setIsWinter(false);
            soSe.setSemesterYear(year);
            semesterService.persist(soSe);
            log.debug("Created " + soSe.toString());
        }

        if (semesterService.findSemester((year - 1), true) == null) {
            Semester wiSe = new Semester();
            wiSe.setIsWinter(true);
            wiSe.setSemesterYear(year - 1);
            semesterService.persist(wiSe);
            log.debug("Created " + wiSe.toString());
        }

        List<Semester> semesters = semesterService.listSemesters();
        int toCreate = 0;

        if (semesters.isEmpty()) {
            toCreate = 10;
        } else {
            int highest = highestYear(semesters);
            toCreate = 10 - (highest - year);
        }

        if (toCreate > 0) {
            semesterService.createNextSemesters(toCreate);
            log.debug("Created " + toCreate + " semesters.");
        } else {
            log.debug("Nothing to create.");
        }
    }

    /**
     * Gets the highest year of all Semesters.
     *
     * @param semesters list of semesters.
     */
    private int highestYear(List<Semester> semesters) {
        int highest = 0;

        for (Semester s : semesters) {
            int year = s.getSemesterYear();
            if (year > highest) {
                highest = year;
            }
        }

        return highest;
    }


    /**
     * Inserts dummy users in the database at startup.
      @TODO Delete before deadline :^)
     */
    private void initDummyData() {
        //Users
        final User newUser = new User();
        newUser.setEmail("user@uni-bremen.de");
        newUser.setPassword(BCrypt.hashpw("user",BCrypt.gensalt()));
        newUser.setFirstName("Ute");
        newUser.setLastName("User");
        newUser.setMatriculationNumber("123456");
        newUser.setLanguage("de");
        newUser.setSalutation("Frau Prof. Dr.");
        newUser.addRole(GlobalRole.USER);
        newUser.setLastActivity(DateUtil.getDateTime());


        final User secondUser = new User();
        secondUser.setEmail("user2@uni-bremen.de");
        secondUser.setPassword(BCrypt.hashpw("user2",BCrypt.gensalt()));
        secondUser.setFirstName("Chuck");
        secondUser.setLastName("Norris");
        secondUser.setMatriculationNumber("hat keine");
        secondUser.setLanguage("de");
        secondUser.addRole(GlobalRole.USER);
        secondUser.setLastActivity(DateUtil.getDateTime());


        final User newLecturer = new User();
        newLecturer.setEmail("lecturer@uni-bremen.de");
        newLecturer.setPassword(BCrypt.hashpw("lecturer",BCrypt.gensalt()));
        newLecturer.setFirstName("Leo");
        newLecturer.setLastName("Lektor");
        newLecturer.addRole(GlobalRole.LECTURER);
        newLecturer.addRole(GlobalRole.USER);
        newLecturer.setLastActivity(DateUtil.getDateTime());


        final User newLecturer2 = new User();
        newLecturer2.setEmail("courseholder@uni-bremen.de");
        newLecturer2.setPassword(BCrypt.hashpw("courseholder",BCrypt.gensalt()));
        newLecturer2.setFirstName("Cord");
        newLecturer2.setLastName("Courseholder");
        newLecturer2.addRole(GlobalRole.LECTURER);
        newLecturer2.addRole(GlobalRole.USER);
        newLecturer2.setLastActivity(DateUtil.getDateTime());

        final User newAdmin = new User();
        newAdmin.setEmail("admin@uni-bremen.de");
        newAdmin.setPassword(BCrypt.hashpw("admin",BCrypt.gensalt()));
        newAdmin.setFirstName("Adolf");
        newAdmin.setLastName("Admin");
        newAdmin.addRole(GlobalRole.ADMIN);
        newAdmin.addRole(GlobalRole.USER);
        newAdmin.setLastActivity(DateUtil.getDateTime());


        // User who isnt in the default course
        final User notInCourse = new User();
        notInCourse.setEmail("notincourse@uni-bremen.de");
        notInCourse.setPassword(BCrypt.hashpw("exmatrikuliert", BCrypt.gensalt()));
        notInCourse.setFirstName("Nick");
        notInCourse.setLastName("Imcuas");
        notInCourse.setMatriculationNumber("133742");
        notInCourse.addRole(GlobalRole.USER);
        notInCourse.setLastActivity(DateUtil.getDateTime());


        final User unconfirmedUser = new User();
        unconfirmedUser.setEmail("unconfirmed@uni-bremen.de");
        unconfirmedUser.setPassword(BCrypt.hashpw("exmatrikuliert", BCrypt.gensalt()));
        unconfirmedUser.setFirstName("Nod");
        unconfirmedUser.setLastName("Komfirmed");
        unconfirmedUser.setMatriculationNumber("113742");
        unconfirmedUser.addRole(GlobalRole.USER);
        unconfirmedUser.setLastActivity(DateUtil.getDateTime());

        userService.persist(notInCourse);
        log.debug("Inserted User with id: " + notInCourse.getUserId());
        userService.persist(newUser);
        log.debug("Inserted User with id: " + newUser.getUserId());
        userService.persist(secondUser);
        log.debug("Inserted User with id: " + secondUser.getUserId());
        userService.persist(unconfirmedUser);
        log.debug("Inserted User with id: " + unconfirmedUser.getUserId());
        userService.persist(newLecturer);
        log.debug("Inserted Lecturer with id: " + newLecturer.getUserId());
        userService.persist(newLecturer2);
        log.debug("Inserted another Lecturer with id: " + newLecturer2.getUserId());
        userService.persist(newAdmin);
        log.debug("Inserted Admin with id: " + newAdmin.getUserId());



        //Course with all relations filled
        Course course = new Course();
        course.setCreated(DateUtil.getDateTime());
        course.setDefaultSws("2+2");
        course.setDefaultCreditPoints(1337);
        course.setName("TestVeranstaltung");
        course.setIdentifier("TV2016");
        course.getNumbers().add("VAK-Nummer123");
        course.setRequiresConfirmation(false);
        course.setStudentsCanSeeFormula(true);
        course.setSemester(semesterService.findSemester(2015, true));




        course = courseService.persist(course);
        log.debug("Inserted Course with id: " + course.getCourseId());

        logService.persist(Log.from(newLecturer,course.getCourseId(),
                "Course has been created."));

        // Mail template for course
        MailTemplate mail = new MailTemplate();
        mail.setName("Test Template");
        mail.setSubject("Durchgefallen");
        mail.setText("Test : {{salutation}} {{firstName}} {{lastName}}"
                + " {{paboGrade}} {{semester}} {{courseName}}"
                + " {{#grades}} {{.}} {{/grades}}");
        mail.setLocale("de");
        mail.setCourse(course);
        mail.setIsDefault(true);
        List<MailTemplate> templates = course.getEmailTemplates();
        templates.add(mail);
        course.setEmailTemplates(templates);

        //Students for course
        Student student = new Student();
        student.setAcceptedInvitation(true);
        student.setConfirmed(true);
        student.setTries(0);
        student.setDeleted(false);
        student.setUser(newUser);
        student.setHidden(false);
        student.setPaboGrade("GRADE_4_0");
        student.setPaboData(new PaboData());

        course.getStudents().add(student);
        student.setCourse(course);

        Student student2 = new Student();
        student2.setAcceptedInvitation(true);
        student2.setConfirmed(true);
        student2.setTries(0);
        student2.setDeleted(false);
        student2.setUser(secondUser);
        student2.setHidden(false);
        student2.setPaboGrade("GRADE_1_0");

        course.getStudents().add(student2);
        student2.setCourse(course);

        //Lecturer for course
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(newLecturer);
        lecturer.setHidden(false);
        lecturer.setIsCourseCreator(true);
        lecturer.setCourse(course);
        course.getLecturers().add(lecturer);

        //Tutorial for course
        Tutorial tutorial = new Tutorial();
        tutorial.setName("Schmick Tutorial");
        tutorial.setCourse(course);
        course.getTutorials().add(tutorial);
        tutorial.getStudents().add(student);
        student.setTutorial(tutorial);

        //PrivilegedUser
        PrivilegedUser privUser = new PrivilegedUser();
        privUser.setSecretary(false);
        privUser.getPrivileges().add(Privilege.ExportData.getId());
        privUser.setUser(newLecturer);
        privUser.setCourse(course);
        privUser.setHidden(false);
        course.getTutors().add(privUser);
        privUser.getTutorials().add(tutorial);
        tutorial.getTutors().add(privUser);

        //Group for course and tutorial
        Group group = new Group();
        group.setName("420 Blaze it");
        group.setTutorial(tutorial);
        tutorial.getGroups().add(group);
        group.setTutorial(tutorial);
        group.getStudents().add(student);
        student.setGroup(group);

        //ParticipationType
        ParticipationType partType = new ParticipationType();
        partType.setName("Informatik");
        partType.setGroupPerformance(false);
        partType.setCourse(course);
        partType.setIsDefaultParttype(true);
        partType = participationTypeService.persist(partType);

        course.getParticipationTypes().add(partType);
        student.setParticipationType(partType);
        student2.setParticipationType(partType);

        ParticipationType winf = new ParticipationType();
        winf.setName("Wirtschaftsinformatik");
        winf.setGroupPerformance(false);
        winf.setCourse(course);
        winf.setIsDefaultParttype(false);
        winf = participationTypeService.persist(winf);

        //GradeFormula
        GradeFormula formula = new GradeFormula();
        formula.setSaveDate(new Date(100L));
        formula.setEditor(newLecturer);
        formula.setFormula("def set_final_grade(grades, student_info, other_course_grades):\n"
                + "    return PaboGrade.GRADE_5_0");
        formula.setEditDescription("Initialized GradeFormula");
        partType.addNewFormula(formula);

        GradeFormula winfFormula = new GradeFormula();
        winfFormula.setSaveDate(new Date(100L));
        winfFormula.setEditor(newLecturer);
        winfFormula.setFormula("def set_final_grade(grades, student_info,other_course_grades):\n"
                + "    return PaboGrade.GRADE_1_0");
        winfFormula.setEditDescription("Initialized WINF GradeFormula");
        course.getParticipationTypes().add(winf);
        winf.addNewFormula(winfFormula);


        //ParticipationType 2
        ParticipationType partType2 = new ParticipationType();
        partType2.setName("Systems Engineering");
        partType2.setGroupPerformance(false);
        partType2.setCourse(course);
        partType2.setIsDefaultParttype(false);
        course.getParticipationTypes().add(partType2);

        //GradeFormula
        GradeFormula formula2 = new GradeFormula();
        formula2.setSaveDate(new Date(100L));
        formula2.setEditor(newLecturer);
        formula2.setFormula("(1336 + 1)*27");
        formula2.setEditDescription("Sys 1337 m8");
        partType2.addNewFormula(formula2);

        //Exam
        Exam exam = new Exam();
        exam.setName("Testprüfung");
        exam.setShortcut("TP1");
        exam.setCourse(course);
        exam.setMaxPoints(new BigDecimal(20));
        exam.setGradeType(GradeType.Point.getId());
        course.getExams().add(exam);

        Exam exam2 = new Exam();
        exam2.setName("Mündliche Prüfung");
        exam2.setShortcut("MP");
        exam2.setCourse(course);
        exam2.setGradeType(GradeType.Boolean.getId());
        course.getExams().add(exam2);

        Exam exam3 = new Exam();
        exam3.setName("Klausur");
        exam3.setShortcut("KL");
        exam3.setCourse(course);
        exam3.setGradeType(GradeType.Numeric.getId());
        course.getExams().add(exam3);

        Exam exam4 = new Exam();
        exam4.setName("Übungsblatt 1");
        exam4.setShortcut("UEB1");
        exam4.setCourse(course);
        exam4.setMaxPoints(new BigDecimal("15.0"));
        exam4.setGradeType(GradeType.Point.getId());
        course.getExams().add(exam4);

        //persist everything
        course = courseService.update(course);

        student = course.getStudents().get(0);
        student2 = course.getStudents().get(1);
        exam = course.getExams().get(0);

        //Testlogs

        //Grading
        Grading grading = new Grading();
        grading.setCorrector(newLecturer);
        grading.setStudent(student);
        student.getGradings().add(grading);
        grading.setExam(course.getExams().get(0));

        //Grade
        Grade grade = new Grade();
        grade.setGradeType(exam.getGradeType());
        if (exam.getGradeType().equals(GradeType.Point.getId())) {
            grade.setMaxPoints(exam.getMaxPoints());
        }
        grade.setValue(new BigDecimal("0.8"));

        grading.setGrade(grade);
        gradingService.persist(grading);

        Upload upload = new Upload();
        upload.setFileSize(100L);
        upload.setPath("/blah/blah/upload.zip");
        upload.setTime(new Date(123456L));
        upload.setComment("lol");
        upload.getUploaders().add(student);
        student.getUploads().add(upload);

        uploadService.persist(upload);


        // Recordbook
        RecordBookEntry rbe = RecordBookEntry.from(student2,course,exam,
                new Date(),60,"Alles komplett gelöst");
        recordBookService.persist(rbe);

        RecordBookEntry entry = new RecordBookEntry();
        entry.setStudent(student);
        entry.setComment("Aufgabe 1");
        entry.setCourse(course);
        entry.setDate(new Date());
        entry.setDuration(5);
        entry.setExam(exam);

        recordBookService.persist(entry);


        //Testlogs

        if (course.getLecturers().size() > 0) {
            lecturer = course.getLecturers().get(0);
            log.debug("Lecturelist of course is not empty");
        }

        if (course.getTutorials().size() > 0) {
            tutorial = course.getTutorials().get(0);
            log.debug("Tutoriallist of course is not empty");
        }

        log.debug("Got Student out of Course with id: "
                + student.getStudentId() + " and username " + student.getUser().getFirstName());
        log.debug("Got Lecturer out of Course with id: "
                + lecturer.getLecturerId() + " and username " + lecturer.getUser().getFirstName());
        log.debug("Got Tutorial out of Course with id: "
                + tutorial.getTutorialId() + " and Tutor "
                + tutorial.getTutors().get(0).getUser().getFirstName());
        log.debug("Got Group out of Course with id: " + group.getGroupId()
                + " and groupmember: " + group.getStudents().get(0).getUser().getFirstName());
        log.debug("ParticipationType: " + course.getParticipationTypes().get(0).getName()
                + "; Semester: " + course.getSemester().toString() + " with id: "
                + course.getSemester().getSemesterId() + " and GradeFormula: "
                + course.getParticipationTypes().get(0).getLatestFormula().getFormula());
        log.debug("Exam: " + course.getExams().get(0).getName());
        log.debug("Student: " + student.getUser().getFirstName() + " has GradingValue: "
                + student.getGradings().get(0).getGrade().getValue()
                + " from " + student.getGradings().get(0).getCorrector().getFirstName());
        log.debug("Upload with id " + upload.getUploadId() + " uploaded by "
                + upload.getUploaders().get(0).getUser().getFirstName());
        if (rbe != null) {
            log.debug("RecordBookEntry with id " + rbe.getEntryId() + " persisted. Summary: "
                    + rbe.getDuration() + " Minutes for exam " + rbe.getExam().getName()
                    + " of Course " + rbe.getCourse().getName() + ". Comment: " + rbe.getComment());
        }
    }
}
