package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
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

        Course course = new Course();
        course.setName("TestVeranstaltung");
        course.setNumber("VAK-Nummer123");
        course.setRequiresConformation(false);

        course = courseService.persist(course);
        log.debug("Inserted Course with id: " + course.getCourseId());

        Student student = new Student();
        student.setAcceptedInvitation(false);
        student.setConfirmed(false);
        student.setTries(0);
        student.setAttending(true);
        student.setUser(newUser);

        course.getStudents().add(student);
        student.setCourse(course);

        Lecturer lecturer = new Lecturer();
        lecturer.setCourse(course);
        lecturer.setUser(newLecturer);

        course = courseService.update(course);

        if (course.getStudents().size() > 0) {
            student = course.getStudents().get(0);
            log.debug("Studentlist of course is not empty");
        }

        if (course.getLecturers().size() > 0) {
            lecturer = course.getLecturers().get(0);
            log.debug("Lecturelist of course is not empty");
        }

        log.debug("Got Student out of Course with id: "
                + student.getStudentId() + " and username " + student.getUser().getFirstName());
        log.debug("Got Lecturer out of Course with id: "
                + lecturer.getLecturerId() + " and username " + lecturer.getUser().getFirstName());
    }

}
