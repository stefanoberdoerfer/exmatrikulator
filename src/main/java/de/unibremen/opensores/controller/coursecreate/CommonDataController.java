package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Field;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.service.SemesterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Backing Bean of the Course create page 'General Data'.
 * This controls the first page of the courseCreate-Flow.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class CommonDataController implements Serializable {

    private static final long serialVersionUID = -616531390355922094L;

    private static transient Logger log = LogManager.getLogger(CommonDataController.class);

    private transient List<Field> courseNumbers;

    private transient List<Lecturer> lecturers;

    private long semesterId;
    private transient List<Semester> semesters;

    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    @EJB
    private transient SemesterService semesterService;

    /**
     * Initialisation method to get semesters from semesterService and
     * add the courseNumber input fields to the UI.
     */
    @PostConstruct
    public void init() {
        semesters = semesterService.listSemesters();

        reloadCourseNumbers();

        lecturers = course.getLecturers();
    }

    /**
     * This method hands the given data from the 'General Data' page over to the flowscoped
     * attribute course of the CourseCreateFlowController before the view gets destroyed.
     */
    @PreDestroy
    public void preDestroy() {
        for (Semester s : semesters) {
            if (s.getSemesterId().equals(semesterId)) {
                course.setSemester(s);
                break;
            }
        }

        course.setNumbers(new ArrayList<>());
        for (Field f : courseNumbers) {
            if (!f.getData().isEmpty()) {
                course.getNumbers().add(f.getData());
            }
        }
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Lecturer> getLecturers() {
        return lecturers;
    }

    public void setLecturers(List<Lecturer> lecturers) {
        this.lecturers = lecturers;
    }

    public long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(long semesterId) {
        this.semesterId = semesterId;
    }

    public List<Semester> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<Semester> semesters) {
        this.semesters = semesters;
    }

    /**
     * Reloads the courseNumbers from the to be created course object.
     * This method is needed to update the courseNumbers correctly, if data is
     * updated from the copyCourse-dialog.
     */
    public void reloadCourseNumbers() {
        log.debug("loaded courseNumbers from course object");
        courseNumbers = new ArrayList<>();
        for (String s : course.getNumbers()) {
            courseNumbers.add(new Field(s));
        }
        if (courseNumbers.isEmpty()) {
            courseNumbers.add(new Field(""));
        }
    }

    /**
     * Adds one CourseNumber to the list of courseNumbers.
     */
    public void addCourseNumber() {
        course.setNumbers(new ArrayList<>());
        for (Field f : courseNumbers) {
            if (!f.getData().isEmpty()) {
                course.getNumbers().add(f.getData());
            }
        }
        courseNumbers.add(new Field(""));
    }

    public List<Field> getCourseNumbers() {
        return courseNumbers;
    }

    public void setCourseNumbers(List<Field> courseNumbers) {
        this.courseNumbers = courseNumbers;
    }
}
