package de.unibremen.opensores.controller;

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
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.Upload;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UploadService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Startup controller, creates dummy data.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class ApplicationController {

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

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(ApplicationController.class);

    @PostConstruct
    public void init() {
        initDummyData();
    }

    /**
     * Inserts dummy users in the database at startup.
     * @TODO Delete before deadline :^)
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
        newUser.addRole(GlobalRole.USER);

        final User newLecturer = new User();
        newLecturer.setEmail("lecturer@uni-bremen.de");
        newLecturer.setPassword(BCrypt.hashpw("lecturer",BCrypt.gensalt()));
        newLecturer.setFirstName("Leo");
        newLecturer.setLastName("Lektor");
        newLecturer.addRole(GlobalRole.LECTURER);
        newLecturer.addRole(GlobalRole.USER);

        final User newAdmin = new User();
        newAdmin.setEmail("admin@uni-bremen.de");
        newAdmin.setPassword(BCrypt.hashpw("admin",BCrypt.gensalt()));
        newAdmin.setFirstName("Adolf");
        newAdmin.setLastName("Admin");
        newAdmin.addRole(GlobalRole.ADMIN);
        newAdmin.addRole(GlobalRole.USER);

        // User who isnt in the default course
        final User notInCourse = new User();
        notInCourse.setEmail("notincourse@uni-bremen.de");
        notInCourse.setPassword(BCrypt.hashpw("exmatrikuliert", BCrypt.gensalt()));
        notInCourse.setFirstName("Nick");
        notInCourse.setLastName("Imcuas");
        notInCourse.setMatriculationNumber("133742");
        notInCourse.addRole(GlobalRole.USER);


        final User unconfirmedUser = new User();
        unconfirmedUser.setEmail("unconfirmed@uni-bremen.de");
        unconfirmedUser.setPassword(BCrypt.hashpw("exmatrikuliert", BCrypt.gensalt()));
        unconfirmedUser.setFirstName("Nod");
        unconfirmedUser.setLastName("Komfirmed");
        unconfirmedUser.setMatriculationNumber("113742");
        unconfirmedUser.addRole(GlobalRole.USER);


        userService.persist(notInCourse);
        log.debug("Inserted User with id: " + notInCourse.getUserId());
        userService.persist(newUser);
        log.debug("Inserted User with id: " + newUser.getUserId());
        userService.persist(unconfirmedUser);
        log.debug("Inserted User with id: " + unconfirmedUser.getUserId());
        userService.persist(newLecturer);
        log.debug("Inserted Lecturer with id: " + newLecturer.getUserId());
        userService.persist(newAdmin);
        log.debug("Inserted Admin with id: " + newAdmin.getUserId());

        //Current semester
        Semester semester = new Semester();
        semester.setIsWinter(true);
        semester.setName("15/16");
        semesterService.persist(semester);

        //next semester
        Semester semester2 = new Semester();
        semester2.setIsWinter(false);
        semester2.setName("16");
        semesterService.persist(semester2);

        //Course with all relations filled
        Course course = new Course();
        course.setSws(42);
        course.setCreditPoints(1337);
        course.setName("TestVeranstaltung");
        course.getNumbers().add("VAK-Nummer123");
        course.setRequiresConformation(false);
        course.setStudentsCanSeeFormula(true);
        course.setSemester(semester);

        course = courseService.persist(course);
        log.debug("Inserted Course with id: " + course.getCourseId());

        logService.persist(Log.from(newLecturer,course.getCourseId(),
                "Course has been created."));

        // Mail template for course
        MailTemplate mail = new MailTemplate();
        mail.setSubject("Durchgefallen");
        mail.setText("ihr seid durchgefallen");
        mail.setLocale("de");
        mail.setCourse(course);
        course.setEmailTemplate(mail);

        //Student for course
        Student student = new Student();
        student.setAcceptedInvitation(true);
        student.setConfirmed(true);
        student.setTries(0);
        student.setDeleted(false);
        student.setUser(newUser);
        student.setHidden(false);
        student.setPaboGrade("1.3");

        course.getStudents().add(student);
        student.setCourse(course);

        //Lecturer for course
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(newLecturer);
        lecturer.setHidden(false);

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
        group.setCourse(course);
        group.setTutorial(tutorial);
        course.getGroups().add(group);
        tutorial.getGroups().add(group);
        group.setTutorial(tutorial);
        group.getStudents().add(student);
        student.setGroup(group);

        //ParticipationType
        ParticipationType partType = new ParticipationType();
        partType.setName("Informatik");
        partType.setGroupPerformance(false);
        partType.setRestricted(false);
        partType.setCourse(course);
        partType.setIsDefaultParttype(true);
        course.getParticipationTypes().add(partType);
        student.setParticipationType(partType);
        //ParticipationType winf = new ParticipationType();
        //winf.setName("Wirtschaftsinformatik");
        //winf.setGroupPerformance(false);
        //winf.setRestricted(false);
        //winf.setCourse(course);
        //winf.setIsDefaultParttype(false);
        //course.getParticipationTypes().add(winf);

        //GradeFormula
        GradeFormula formula = new GradeFormula();
        formula.setTime(new Date(100L));
        formula.setEditor(newLecturer);
        formula.setFormula("(1336 + 1)*27");
        partType.setGradeFormula(formula);
        //winf.setGradeFormula(formula);

        //Exam
        Exam exam = new Exam();
        exam.setName("TestPrüfung");
        exam.setShortcut("TP1");
        exam.setCourse(course);
        exam.setGradeType(GradeType.Point.getId());
        course.getExams().add(exam);

        //persist everything
        course = courseService.update(course);

        //Testlogs
        if (course.getStudents().size() > 0) {
            student = course.getStudents().get(0);
            log.debug("Studentlist of course is not empty");
        }

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
        grade.setValue(new BigDecimal("1.0"));

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

        //Testlogs

        if (course.getLecturers().size() > 0) {
            lecturer = course.getLecturers().get(0);
            log.debug("Lecturelist of course is not empty");
        }

        if (course.getTutorials().size() > 0) {
            tutorial = course.getTutorials().get(0);
            log.debug("Tutoriallist of course is not empty");
        }

        if (course.getGroups().size() > 0) {
            group = course.getGroups().get(0);
            log.debug("Grouplist of course has " + course.getGroups().size() + " groups");
            log.debug("First group has " + group.getStudents().size() + " students");
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
                + "; Semester: " + course.getSemester().getName() + " with id: "
                + course.getSemester().getSemesterId() + " and GradeFormula: "
                + course.getParticipationTypes().get(0).getGradeFormula().getFormula());
        log.debug("Exam: " + course.getExams().get(0).getName());
        log.debug("Student: " + student.getUser().getFirstName() + " has GradingValue: "
                + student.getGradings().get(0).getGrade().getValue()
                + " from " + student.getGradings().get(0).getCorrector().getFirstName());
        log.debug("Upload with id " + upload.getUploadId() + " uploaded by "
                + upload.getUploaders().get(0).getUser().getFirstName());
    }

}
