package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * List of courses with the possibility of getting copied.
     */
    private transient List<Course> copyCourses;

    /**
     * Id of chosen course to be copied.
     */
    private long copyCourseId;

    /**
     * Boolean flags representing the different copy options.
     */
    private boolean copyCommonData;
    private boolean copyExams;
    private boolean copyParticipationTypes;
    private boolean copyTutorials;
    private boolean copyLecturers;
    private boolean copyTutors;

    /**
     * FlowScoped course to be created.
     */
    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    /**
     * Currently logged in User.
     */
    @ManagedProperty("#{userController.user}")
    private transient User user;

    @EJB
    private transient UserService userService;

    /**
     * Initialisation method to get all Courses of the logged in lecturer
     * from the userService.
     */
    @PostConstruct
    public void init() {
        copyCourses = userService.getLecturerCourses(user);
    }

    /**
     * This method copies the selected attributes of an selected other course
     * to the newly to be created one.
     */
    public void copyAction() {
        log.debug("copyAction called");

        Course courseToCopy = null;
        for (Course c : copyCourses) {
            if (c.getCourseId().equals(copyCourseId)) {
                courseToCopy = c;
                break;
            }
        }
        if (courseToCopy == null) {
            return;
        }

        //Don't change order of copy actions
        if (copyCommonData) {
            copyCommonDataFrom(courseToCopy);
        }

        if (copyExams) {
            copyExamsFrom(courseToCopy);
        }

        if (copyParticipationTypes) {
            copyParticipationTypesFrom(courseToCopy);
        }

        if (copyTutors) {
            copyTutorsFrom(courseToCopy);
        }

        if (copyTutorials) {
            copyTutorialsFrom(courseToCopy);
        }

        if (copyLecturers) {
            copyLecturersFrom(courseToCopy);
        }
    }

    /**
     * Copies all lecturers from given course. Currently logged in
     * user will always be marked as the creator of the new course.
     * If option 'copyTutors' is not chosen, then all copied lecturers
     * will also be added as PrivilegedUsers to the new course.
     * This happens because every lecturer automatically has to be a
     * PrivilegedUser.
     *
     * @param courseToCopy another course of which lecturers will be copied from
     */
    private void copyLecturersFrom(Course courseToCopy) {
        log.debug("Copy Lecturers from: " + courseToCopy.getName());
        List<Lecturer> targetLecturers = new ArrayList<>();

        //Current user must be in the list of lecturers of courseToCopy.
        //Add all lecturers to the new course and make current user to creator.
        List<User> copyLecturers = courseToCopy.getLecturers().stream()
                .map(Lecturer::getUser)
                .collect(Collectors.toList());
        for (User u : copyLecturers) {
            log.debug("Add Lecturer: " + u);
            Lecturer lnew = new Lecturer();
            lnew.setHidden(false);
            lnew.setCourse(course);
            lnew.setUser(u);
            lnew.setIsCourseCreator(u.getUserId().equals(user.getUserId()));
            targetLecturers.add(lnew);

            if (!copyTutors) {
                PrivilegedUser privUser = new PrivilegedUser();
                privUser.setUser(u);
                privUser.setHidden(false);
                privUser.setSecretary(false);
                privUser.setCourse(course);
                course.getTutors().add(privUser);
            }
        }
        course.setLecturers(targetLecturers);
    }

    /**
     * Copies all Tutorials from the given course to the new one.
     *
     * @param courseToCopy another course of which Tutorials will be copied from
     */
    private void copyTutorialsFrom(Course courseToCopy) {
        log.debug("Copy Tutorials from: " + courseToCopy.getName());

        List<Tutorial> targetList = course.getTutorials();
        targetList.clear();

        for (Tutorial tCopy : courseToCopy.getTutorials()) {
            Tutorial tut = new Tutorial();
            tut.setName(tCopy.getName());
            tut.setCourse(course);
            targetList.add(tut);
        }
    }

    /**
     * Copies all PrivilegedUsers from the given course to the new one.
     * If option 'copyTutorials' is chosen the newly created PrivilegedUsers
     * will also have the right relation to the copied Tutorials.
     *
     * @param courseToCopy another course of which PrivilegedUsers will be copied from
     */
    private void copyTutorsFrom(Course courseToCopy) {
        log.debug("Copy Tutors from: " + courseToCopy.getName());

        List<PrivilegedUser> targetList = course.getTutors();
        targetList.clear();

        for (PrivilegedUser pCopy : courseToCopy.getTutors()) {
            PrivilegedUser pUser = new PrivilegedUser();
            pUser.setUser(pCopy.getUser());
            pUser.setSecretary(pCopy.isSecretary());
            pUser.setPrivileges(pCopy.getPrivileges());
            pUser.setHidden(false);

            if (copyTutorials) {
                List<Tutorial> tutorialList = new ArrayList<>();
                for (Tutorial otherTutorial : pCopy.getTutorials()) {
                    for (Tutorial thisTut : course.getTutorials()) {
                        if (otherTutorial.getName().equals(thisTut.getName())) {
                            tutorialList.add(thisTut);
                        }
                    }
                }
                pUser.setTutorials(tutorialList);
            }

            targetList.add(pUser);
        }
    }

    /**
     * Copies all ParticipationTypes from the given course to the new one.
     * If option 'copyExams' is chosen the newly created ParticipationTypes
     * will also have the right relation to the copied Exams.
     *
     * @param courseToCopy another course of which ParticipationTypes will be copied from
     */
    private void copyParticipationTypesFrom(Course courseToCopy) {
        log.debug("Copy Parttypes from: " + courseToCopy.getName());
        course.setParticipationTypes(new ArrayList<>());
        for (ParticipationType p : courseToCopy.getParticipationTypes()) {
            ParticipationType px = new ParticipationType();
            px.setName(p.getName());
            px.setGroupPerformance(p.getGroupPerformance());
            px.setIsDefaultParttype(p.isDefaultParttype());
            px.setPerformanceArea(p.getPerformanceArea());
            px.setPerformanceContent(p.getPerformanceContent());
            px.setRestricted(p.getRestricted());
            px.setGradeFormula(p.getGradeFormula());

            if (copyExams) {
                List<Exam> examList = new ArrayList<>();
                for (Exam otherExam : p.getExams()) {
                    for (Exam thisExam : course.getExams()) {
                        if (otherExam.getName().equals(thisExam.getName())) {
                            examList.add(thisExam);
                        }
                    }
                }
                px.setExams(examList);
            }

            course.getParticipationTypes().add(px);
        }
    }

    /**
     * Copies all Exams from the given course to the new one.
     *
     * @param courseToCopy another course of which Exams will be copied from
     */
    private void copyExamsFrom(Course courseToCopy) {
        log.debug("Copy exams from: " + courseToCopy.getName());
        List<Exam> targetList = course.getExams();
        targetList.clear();
        for (Exam e : courseToCopy.getExams()) {
            Exam ex = new Exam();
            ex.setName(e.getName());
            ex.setGradeType(e.getGradeType());
            ex.setGradableByTutors(e.isGradableByTutors());
            ex.setUploadAssignment(e.isUploadAssignment());
            ex.setAllowedFileEndings(e.getAllowedFileEndings());
            ex.setDeadline(e.getDeadline());
            ex.setMaxFileSizeMB(e.getMaxFileSizeMB());
            ex.setMaxPoints(e.getMaxPoints());
            ex.setShortcut(e.getShortcut());
            ex.setCourse(course);
            targetList.add(ex);
        }
    }

    /**
     * Copies Name, Numbers, SWS, CP, flags and Groupsizes from the given course to the new one.
     *
     * @param courseToCopy another course of which the common course data
     *                     will be copied from
     */
    private void copyCommonDataFrom(Course courseToCopy) {
        log.debug("Copy common data from: " + courseToCopy.getName());
        course.setName(courseToCopy.getName());
        course.setNumbers(courseToCopy.getNumbers());
        course.setSws(courseToCopy.getSws());
        course.setCreditPoints(courseToCopy.getCreditPoints());
        course.setStudentsCanSeeFormula(courseToCopy.getStudentsCanSeeFormula());
        course.setRequiresConfirmation(courseToCopy.getRequiresConfirmation());
        course.setMinGroupSize(courseToCopy.getMinGroupSize());
        course.setMaxGroupSize(courseToCopy.getMaxGroupSize());
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
