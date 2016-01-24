package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

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
    private UserService userService;

    @EJB
    private CourseService courseService;

    @EJB
    private StudentService studentService;

    @EJB
    private SemesterService semesterService;

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
        newUser.setLanguage("de");
        newUser.getRoles().add(Role.USER.getId());

        final User newLecturer = new User();
        newLecturer.setEmail("lecturer@uni-bremen.de");
        newLecturer.setPassword(BCrypt.hashpw("lecturer",BCrypt.gensalt()));
        newLecturer.setFirstName("Leo");
        newLecturer.setLastName("Lektor");
        newLecturer.getRoles().add(Role.LECTURER.getId());
        newLecturer.getRoles().add(Role.USER.getId());

        final User newAdmin = new User();
        newAdmin.setEmail("admin@uni-bremen.de");
        newAdmin.setPassword(BCrypt.hashpw("admin",BCrypt.gensalt()));
        newAdmin.setFirstName("Adolf");
        newAdmin.setLastName("Admin");
        newAdmin.getRoles().add(Role.ADMIN.getId());
        newAdmin.getRoles().add(Role.USER.getId());

        userService.persist(newUser);
        log.debug("Inserted User with id: " + newUser.getUserId());
        userService.persist(newLecturer);
        log.debug("Inserted Lecturer with id: " + newLecturer.getUserId());
        userService.persist(newAdmin);
        log.debug("Inserted Admin with id: " + newAdmin.getUserId());

        //Current semester
        Semester semester = new Semester();
        semester.setIsWinter(true);
        semester.setName("Wintersemester 15/16");
        semesterService.persist(semester);

        //Course with all relations filled
        Course course = new Course();
        course.setName("TestVeranstaltung");
        course.setNumber("VAK-Nummer123");
        course.setRequiresConformation(false);
        course.setSemester(semester);

        course = courseService.persist(course);
        log.debug("Inserted Course with id: " + course.getCourseId());

        //Student for course
        Student student = new Student();
        student.setAcceptedInvitation(false);
        student.setConfirmed(false);
        student.setTries(0);
        student.setAttending(true);
        student.setUser(newUser);

        course.getStudents().add(student);
        student.setCourse(course);

        //Lecturer for course
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(newLecturer);

        lecturer.setCourse(course);
        course.getLecturers().add(lecturer);

        //Tutorial for course
        Tutorial tutorial = new Tutorial();
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
        course.getTutors().add(privUser);
        privUser.getTutorials().add(tutorial);
        tutorial.getTutors().add(privUser);

        //Group for course and tutorial
        Group group = new Group();
        group.setCourse(course);
        group.setTutorial(tutorial);
        course.getGroups().add(group);
        tutorial.getGroups().add(group);
        group.getStudents().add(student);

        //ParticipationType
        ParticipationType partType = new ParticipationType();
        partType.setName("Informatik");
        partType.setGroupPerformance(false);
        partType.setRestricted(false);
        partType.setCourse(course);
        course.getParticipationTypes().add(partType);

        //TODO: Exams dont work properly
        Exam exam = new Exam();
        exam.setName("TestPrüfung");
        //exam.setCourse(course);
        //course.getExams().add(exam);

        //persist everything
        course = courseService.update(course);

        //Testlogs
        if (course.getStudents().size() > 0) {
            student = course.getStudents().get(0);
            log.debug("Studentlist of course is not empty");
        }

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
            log.debug("Grouplist of course is not empty");
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
                + course.getSemester().getSemesterId());
    }

}
