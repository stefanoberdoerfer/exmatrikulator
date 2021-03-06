package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to display the data tables for students and groups.
 * @author Matthias Reichmann
 */
@ManagedBean
@ViewScoped
public class GradingController {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(GradingController.class);

    /**
     * The course which exams get edited.
     */
    private Course course;

    /**
     * Currently logged in user.
     */
    private User user;

    /**
     * Student gradings. Stored here so it won't be loaded multiple times.
     */
    private Map<Long, Map<Long, Grading>> studentGradings = new HashMap<>();

    /**
     * String to search for in the students overview.
     */
    private String searchValue;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * GradingService for database transactions related to gradings.
     */
    @EJB
    private GradingService gradingService;

    /**
     * GradeService for database transactions related to grades.
     */
    @EJB
    private GradeService gradeService;

    /**
     * Method called after initialisation.
     * Gets the corresponding course from the http param.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        HttpServletRequest httpReq
                = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();

        log.debug("Request URI: " + httpReq.getRequestURI());
        final String courseIdString = httpReq.getParameter(Constants.HTTP_PARAM_COURSE_ID);

        log.debug("course-id: " + courseIdString);
        long courseId = -1;
        if (courseIdString != null) {
            try {
                courseId = Long.parseLong(courseIdString.trim());
            } catch (NumberFormatException e) {
                log.debug("NumberFormatException while parsing courseId");
            }
        }

        if (courseId != -1) {
            course = courseService.find(Course.class, courseId);
        }

        Object userObj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("user");

        log.debug("Loaded course object: " + course);

        if (course == null || !(userObj instanceof User)) {
            log.debug("trying to redirect to /course/overview");
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
        /*
        Load the user
         */
        user = (User)FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("user");
    }

    /**
     * Returns the course.
     *
     * @return Course
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Returns a list of all students of the opened course.
     *
     * @return List of students or null
     */
    public List<Student> getStudents() {
        if (searchValue != null && searchValue.trim().length() > 0) {
            log.debug("Search for " + searchValue);
            return gradingService.getStudents(course, user, searchValue.trim());
        } else {
            return gradingService.getStudents(course, user);
        }
    }

    /**
     * Returns the grading of a single exam and student. Uses map because
     * gradings are only stored for graded exams. But we want all exams to
     * show up.
     * @param student Student whose gradings shall be loaded
     * @param exam Exam to check
     * @return Map of exams with gradings
     */
    public Grading getStudentGrading(final Student student,
                                                final Exam exam) {
        log.debug("load student gradings for student "
                + student.getUser().getFirstName());

        Map<Long, Grading> gradings = studentGradings.get(student.getStudentId());

        log.debug("Gradings is "
                + (gradings == null ? "not loaded" : "already loaded"));

        if (gradings == null) {
            gradings = gradingService.getStudentGradings(student);
            log.debug("Loaded gradings is null");
            studentGradings.put(student.getStudentId(), gradings);
        }

        return gradings.get(exam.getExamId());
    }

    public void setSearchValue(String search) {
        this.searchValue = search;
    }

    public String getSearchValue() {
        return this.searchValue;
    }

    public List<Group> getGroups() {
        return gradingService.getGroups(course, user);
    }

    public String getPaboGradeName(final String name) {
        return gradeService.paboGradeDisplayName(name);
    }

    public boolean hasBooleanGradeType(final Exam exam) {
        return (exam != null && exam.hasGradeType(GradeType.Boolean));
    }

    /**
     * Returns if the given user passed the given boolean exam.
     * @param student Student to check
     * @param exam Exam to check
     * @return true if the student passed
     */
    public boolean hasPassed(final Student student, final Exam exam) {
        final Grading grading = getStudentGrading(student, exam);

        return grading != null && GradeType.Boolean.hasPassed(grading
                .getGrade().getValue());
    }

    /**
     * Called to reset the exam gradings cache for a specific student.
     * @param student Student to remove from the cache.
     */
    public void resetExamGradings(Student student) {
        if (student == null) {
            return;
        }

        log.debug("Remove student gradings for " + student.getStudentId());
        studentGradings.remove(student.getStudentId());
    }

    /**
     * Called to reset the exam gradings cache for a group.
     * @param group Group to remove from the cache.
     */
    public void resetExamGradings(Group group) {
        for (Student s : group.getStudents()) {
            resetExamGradings(s);
        }
    }

    public boolean isPointExam(Exam exam) {
        return exam.getGradeType().equals(GradeType.Point.getId());
    }
}
