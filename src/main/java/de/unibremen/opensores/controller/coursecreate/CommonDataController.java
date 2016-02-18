package de.unibremen.opensores.controller.coursecreate;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Field;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.SemesterService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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

    /**
     * List of courseNumber strings each wrapped in a Field-object.
     * Wrapper Class needed for dynamic input-field generation.
     */
    private transient List<Field> courseNumbers;

    /**
     * LecturerSearch resultlist.
     */
    private transient List<User> searchResultList;

    /**
     * Id of chosen semester.
     */
    private long semesterId;

    /**
     * List of available semesters in Exmatrikulator.
     */
    private transient List<Semester> semesters;

    /**
     * FlowScoped course to be created.
     */
    @ManagedProperty("#{courseCreateFlowController.course}")
    private transient Course course;

    @EJB
    private transient SemesterService semesterService;

    @EJB
    private transient UserService userService;

    /**
     * Initialisation method to get semesters from semesterService and
     * add the courseNumber input fields to the UI.
     */
    @PostConstruct
    public void init() {
        semesters = semesterService.listSemesters();
        reloadCourseNumbers();
    }

    /**
     * This method hands the given data from the 'General Data' page over to the flowscoped
     * course of the CourseCreateFlowController before this view gets destroyed.
     */
    @PreDestroy
    public void preDestroy() {
        for (Semester s : semesters) {
            if (s.getSemesterId().equals(semesterId)) {
                course.setSemester(s);
                break;
            }
        }
        updateCourseNumbers();
    }

    /**
     * Reloads the courseNumbers from the to be created course object.
     * This method is needed to update the courseNumbers correctly, if data is
     * updated from the copyCourse-dialog.
     */
    public void reloadCourseNumbers() {
        log.debug("loaded courseNumbers from course object");

        courseNumbers = course.getNumbers().stream()
                .map((Field::new))
                .collect(Collectors.toList());

        if (courseNumbers.isEmpty()) {
            courseNumbers.add(new Field(""));
        }
    }

    /**
     * Updates the courseNumbers attribute of the to be created Course and
     * adds one empty CourseNumber-Field to the UI.
     */
    public void addCourseNumber() {
        updateCourseNumbers();

        if (!courseNumbers.get(courseNumbers.size() - 1).getData().isEmpty()) {
            courseNumbers.add(new Field(""));
        }
    }

    /**
     * Updates Numbers attribute of the course to be created.
     */
    private void updateCourseNumbers() {
        ArrayList<String> list = new ArrayList<>();
        courseNumbers.stream()
                .filter(f -> !f.getData().isEmpty())
                .forEach(f -> list.add(f.getData()));
        course.setNumbers(list);
    }

    /**
     * Searches for users in the whole system who are lecturers with the UserInput query string
     * given via ValueChangeEvent.
     */
    public void searchForLecturers(ValueChangeEvent vce) {
        String query = (String) vce.getNewValue();
        log.debug("searchForLecturers called with input: " + query);
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        searchResultList = userService.searchForLecturers(query);
    }

    /**
     * Creates a new Lecturer object with the chosen User and adds this Lecturer to the
     * course if he isn't in there already. Also adds him as a PrivilegedUser to this course.
     * After successful adding, this method removes given user from the searchResultList.
     */
    public void addAsLecturer(User user) {
        if (user == null) {
            return;
        }

        log.debug("addAsLecturer called with user: " + user);

        //check if not already a lecturer
        for (Lecturer l : course.getLecturers()) {
            if (l.getUser().getUserId().equals(user.getUserId())) {
                return;
            }
        }

        Lecturer lec = new Lecturer();
        lec.setUser(user);
        lec.setHidden(false);
        lec.setCourse(course);
        lec.setIsCourseCreator(false);
        course.getLecturers().add(lec);

        PrivilegedUser privUser = new PrivilegedUser();
        privUser.setUser(user);
        privUser.setHidden(false);
        privUser.setSecretary(false);
        privUser.setCourse(course);
        course.getTutors().add(privUser);

        searchResultList.remove(user);
    }

    /**
     * Removes one Lecturer from the courses list of lecturers and also removes him as
     * a Privileged User.
     */
    public void removeLecturer(Lecturer lecturer) {
        course.getLecturers().remove(lecturer);
        course.getTutors().stream()
                .filter(u1 -> u1.getUser().getUserId().equals(lecturer.getUser().getUserId()))
                .forEach(u2 -> course.getTutors().remove(u2));
    }

    public List<Field> getCourseNumbers() {
        return courseNumbers;
    }

    public void setCourseNumbers(List<Field> courseNumbers) {
        this.courseNumbers = courseNumbers;
    }

    public List<User> getSearchResultList() {
        return searchResultList;
    }

    public void setSearchResultList(List<User> searchResultList) {
        this.searchResultList = searchResultList;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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
}
