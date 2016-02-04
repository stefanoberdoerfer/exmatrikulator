package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Field;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.service.CourseService;
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
 * The Backing Bean of the Course copy dialog.
 * This controls the copy logic of the courseCreate-Flow.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class CourseCopyController implements Serializable {

    private static final long serialVersionUID = -7698178714781340177L;

    private static transient Logger log = LogManager.getLogger(CourseCopyController.class);

    private transient List<Course> copyCourses;

    private long copyCourseId;

    private boolean copyCommonData;
    private boolean copyExams;
    private boolean copyParticipationTypes;
    private boolean copyFormulas;
    private boolean copyTutorials;
    private boolean copyLecturers;
    private boolean copyTutors;

    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    @EJB
    private transient CourseService courseService;

    /**
     * Initialisation method to get all Courses of the logged in lecturer
     * from the courseService.
     */
    @PostConstruct
    public void init() {
        //TODO show only courses of logged in lecturer
        copyCourses = courseService.listCourses();
    }

    /**
     * This method copies the selected attributes of an selected old course
     * to the newly to be created one.
     */
    public void copyAction() {
        Course targetCourse = null;
        for (Course c : copyCourses) {
            if (c.getCourseId().equals(copyCourseId)) {
                targetCourse = c;
                break;
            }
        }

        log.debug("copyAction called");

        if (targetCourse == null) {
            return;
        }

        if (copyCommonData) {
            log.debug("Copy common data from: " + targetCourse.getName());
            course.setName(targetCourse.getName());
            course.setNumbers(targetCourse.getNumbers());
            course.setSws(targetCourse.getSws());
            course.setCreditPoints(targetCourse.getCreditPoints());
            course.setStudentsCanSeeFormula(targetCourse.getStudentsCanSeeFormula());
        }

        if (copyExams) {
            log.debug("Copy exams from: " + targetCourse.getName());
        }

        if (copyParticipationTypes) {
            log.debug("Copy Parttypes from: " + targetCourse.getName());
            for (ParticipationType p : targetCourse.getParticipationTypes()) {
                ParticipationType px = new ParticipationType();
                px.setName(p.getName());
                px.setGroupPerformance(p.getGroupPerformance());
                px.setIsDefaultParttype(p.isDefaultParttype());
                px.setPerformanceArea(p.getPerformanceArea());
                px.setPerformanceContent(p.getPerformanceContent());
                px.setRestricted(p.getRestricted());

                course.getParticipationTypes().add(px);
            }
        }

        if (copyFormulas) {
            log.debug("Copy Formulas from: " + targetCourse.getName());
        }

        if (copyTutorials) {
            log.debug("Copy Tutorials from: " + targetCourse.getName());
        }

        if (copyLecturers) {
            log.debug("Copy Lecturers from: " + targetCourse.getName());
        }

        if (copyTutors) {
            log.debug("Copy Tutors from: " + targetCourse.getName());
        }
    }


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Course> getCopyCourses() {
        return copyCourses;
    }

    public void setCopyCourses(List<Course> copyCourses) {
        this.copyCourses = copyCourses;
    }

    public long getCopyCourseId() {
        return copyCourseId;
    }

    public void setCopyCourseId(long copyCourseId) {
        this.copyCourseId = copyCourseId;
    }

    public boolean isCopyCommonData() {
        return copyCommonData;
    }

    public void setCopyCommonData(boolean copyCommonData) {
        this.copyCommonData = copyCommonData;
    }

    public boolean isCopyExams() {
        return copyExams;
    }

    public void setCopyExams(boolean copyExams) {
        this.copyExams = copyExams;
    }

    public boolean isCopyParticipationTypes() {
        return copyParticipationTypes;
    }

    public void setCopyParticipationTypes(boolean copyParticipationTypes) {
        this.copyParticipationTypes = copyParticipationTypes;
    }

    public boolean isCopyFormulas() {
        return copyFormulas;
    }

    public void setCopyFormulas(boolean copyFormulas) {
        this.copyFormulas = copyFormulas;
    }

    public boolean isCopyTutorials() {
        return copyTutorials;
    }

    public void setCopyTutorials(boolean copyTutorials) {
        this.copyTutorials = copyTutorials;
    }

    public boolean isCopyLecturers() {
        return copyLecturers;
    }

    public void setCopyLecturers(boolean copyLecturers) {
        this.copyLecturers = copyLecturers;
    }

    public boolean isCopyTutors() {
        return copyTutors;
    }

    public void setCopyTutors(boolean copyTutors) {
        this.copyTutors = copyTutors;
    }
}
