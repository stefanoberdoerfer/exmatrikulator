package de.unibremen.opensores.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DualListModel;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.TutorialService;

import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.io.Serializable;
import java.text.MessageFormat;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * Controller for managing tutorials.
 *
 * @author Sören Tempel
 */
@ManagedBean
@ViewScoped
public class TutorialController implements Serializable {
    /**
     * Unique serial version uid.
     */
    private static final long serialVersionUID = -626531390355925094L;

    /**
     * The log4j logger.
     */
    private Logger log = LogManager.getLogger(TutorialController.class);

    /**
     * The user service for connection to the database.
     */
    @EJB
    private transient UserService userService;

    /**
     * The course service for connection to the database.
     */
    @EJB
    private transient CourseService courseService;

    /**
     * The tutorial service for connection to the database.
     */
    @EJB
    private transient TutorialService tutorialService;

    /**
     * Course for this tutorial.
     */
    private transient Course course;

    /**
     * Current tutorial object.
     */
    private transient Tutorial tutorial;

    /**
     * Name of the current tutorial.
     */
    private String tutorialName;

    /**
     * New tutorial name, used when the tutorial is being edited.
     */
    private String newTutorialName = null;

    /**
     * Removal conformation required to remove a tutorial.
     */
    private String removalConformation;

    /**
     * List of tutors for this tutorial.
     */
    private DualListModel<PrivilegedUser> tutorialTutors;

    /**
     * List of students for this tutorial.
     */
    private DualListModel<Student> tutorialStudents;

    /**
     * Current group context.
     */
    private transient Group group;

    /**
     * Name of the current group.
     */
    private String groupName;

    /**
     * New group name, used when the group is being edited.
     */
    private String newGroupName = null;

    /**
     * List of members for the current group.
     */
    private DualListModel<Student> groupMembers;

    /**
     * Method called on bean initialization.
     */
    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        HttpServletRequest req = (HttpServletRequest)
            facesContext.getExternalContext().getRequest();
        String idStr = req.getParameter("course-id");

        try {
            course = (idStr == null || idStr.trim().isEmpty()) ? null : courseService
                .find(Course.class, Integer.valueOf(idStr).longValue());
        } catch (NumberFormatException e) {
            course = null;
        }

        /*
         * TODO verify that the user is allowed to access this course
         */

        if (course == null) {
            String msg = bundle.getString("courses.fail");
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));

            /*
             * TODO decide where the user should be redirected to.
             */

            return;
        }

        // TODO we might want to set this in the filter.
        facesContext.getExternalContext().getSessionMap()
            .put("course", course);

        tutorialTutors = new DualListModel<>(course.getTutors(),
            new ArrayList<>());
        tutorialStudents = new DualListModel<>(course.getStudents(),
            new ArrayList<>());
        groupMembers = new DualListModel<>(course.getStudents(),
            new ArrayList<>());
    }

    /**
     * Creates a new tutorial.
     */
    public void createTutorial() {
        tutorial = new Tutorial();
        tutorial.setCourse(course);
        tutorial.setName(tutorialName);

        tutorial = updateTutors(tutorial);
        course.getTutorials().add(tutorial);
        course = courseService.update(course);

        log.debug("Created new tutorial " + tutorial.getName());
    }

    /**
     * Changes the current tutorial context.
     *
     * @param tutorial Tutorial to switch to.
     */
    public void changeCurrentTutorial(@NotNull Tutorial tutorial) {
        this.tutorial = tutorial;
        this.tutorialName = tutorial.getName();

        this.group = null;
        this.groupName = null;
        this.groupMembers.setSource(tutorial.getStudents());
        this.groupMembers.setTarget(new ArrayList<>());

        List<PrivilegedUser> tutors = tutorial.getTutors();
        this.tutorialTutors.setTarget(tutors);
        this.tutorialTutors.setSource(tutors.stream()
                .filter(t -> !tutorialTutors.getTarget().contains(t))
                .collect(Collectors.toList()));

        List<Student> students = tutorial.getStudents();
        this.tutorialStudents.setSource(students);
        this.tutorialStudents.setSource(students.stream()
                .filter(s -> !tutorialStudents.getTarget().contains(s))
                .collect(Collectors.toList()));
    }

    /**
     * Changes the current group context.
     *
     * @param group Group to switch to.
     */
    public void changeCurrentGroup(@NotNull Group group) {
        this.group = group;
        this.groupName = group.getName();

        List<Student> students = group.getStudents();
        this.groupMembers.setTarget(students);
        this.groupMembers.setSource(students.stream()
                .filter(s -> !groupMembers.getTarget().contains(s))
                .collect(Collectors.toList()));

        this.tutorial = group.getTutorial();
        this.tutorialName = tutorial.getName();
    }

    /**
     * Edits an existing tutorial.
     */
    public void editTutorial() {
        tutorial.setName(newTutorialName);
        this.newTutorialName = null;

        tutorial = updateTutors(tutorial);
        course = courseService.update(course);
    }

    /**
     * Removes the current tutorial.
     */
    public void removeTutorial() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        String name = tutorial.getName();
        if (!removalConformation.equals(name)) {
            String msg = bundle.getString("courses.tutorialDeleteConfirm");
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        course.getTutorials().remove(tutorial);
        tutorialService.remove(tutorial);
        log.debug(String.format("Removed tutorial %s from course %s",
                    name, course.getName()));

        tutorial = null;
        tutorialName = null;
        removalConformation = null;
    }

    /**
     * Updates tutors for the given tutorial in this course.
     *
     * @param tutorial Tutorial to update tutors for.
     * @return Updated Tutorial.
     */
    private Tutorial updateTutors(Tutorial tutorial) {
        List<PrivilegedUser> tutors = new ArrayList<>();
        for (PrivilegedUser tutor : tutorialTutors.getTarget()) {
            if (!course.getTutors().contains(tutor)) {
                course.getTutors().add(tutor);
            }

            tutor.getTutorials().add(tutorial);
            log.debug(String.format("Made user %s a tutor in course %s",
                tutor.getUser().getEmail(), course.getName()));

            tutors.add(tutor);
        }

        tutorial.setTutors(tutors);
        return tutorial;
    }

    /**
     * Creates a new group in the current tutorial.
     */
    public void createGroup() {
        group = new Group();
        group.setName(groupName);
        group.setCourse(course);
        group.setTutorial(tutorial);

        group = updateMembers(group);
        course.getGroups().add(group);
        tutorial.getGroups().add(group);

        tutorial = tutorialService.update(tutorial);
        log.debug(String.format("Created new group %s in tutorial %s",
            group.getName(), tutorial.getName()));
    }

    /**
     * Edits an existing group in the current tutorial.
     */
    public void editGroup() {
        group.setName(newGroupName);
        this.newGroupName = null;

        group = updateMembers(group);
        tutorial = tutorialService.update(tutorial);
    }

    /**
     * Remove the current group from the current tutorial.
     */
    public void removeGroup() {
        course.getGroups().remove(group);
        tutorial.getGroups().remove(group);
        tutorial = tutorialService.update(tutorial);

        log.debug(String.format("Removed group %s from tutorial %s",
            group.getName(), tutorial.getName()));

        group = null;
        groupName = null;
    }

    /**
     * Updates group members for the given group in the current tutorial.
     *
     * @param group Group to update members for.
     * @return Updated group.
     */
    private Group updateMembers(Group group) {
        List<Student> students = new ArrayList<>();
        for (Student student : groupMembers.getTarget()) {
            students.add(student);
            log.debug(String.format("Added student with email %s to group %s",
                student.getUser().getEmail(), group.getName()));
        }

        group.setStudents(students);
        return group;
    }

    /**
     * Updates the students for the current tutorial.
     */
    public void updateStudent() {
        tutorial.setStudents(tutorialStudents.getTarget());
        tutorial = tutorialService.update(tutorial);
    }

    /**
     * Returns a list of all tutorials for this course.
     *
     * @return List of tutorials or null if non exist.
     */
    public List<Tutorial> getTutorials() {
        return (course == null) ? null : course.getTutorials();
    }

    /**
     * Sets the course for this tutorial.
     *
     * @param course Course to use.
     */
    public void setCourse(Course course) {
        this.course = course;
    }

    /**
     * Returns the course for this tutorial.
     *
     * @return Course for this tutorial.
     */
    public Course getCourse() {
        return this.course;
    }

    /**
     * Sets the group name for the current group.
     *
     * @param groupName Group name.
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Returns the group name for the current group.
     *
     * @return Group name.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the name for the current tutorial.
     *
     * @param tutorialName Tutorial name.
     */
    public void setTutorialName(String tutorialName) {
        this.tutorialName = tutorialName;
    }

    /**
     * Returns the name of the current tutorial.
     *
     * @return Name of the current tutorial.
     */
    public String getTutorialName() {
        return tutorialName;
    }

    /**
     * Sets the new name for the current tutorial.
     *
     * @param newTutorialName New Tutorial name.
     */
    public void setNewTutorialName(String newTutorialName) {
        this.newTutorialName = newTutorialName;
    }

    /**
     * Returns the new name for the current tutorial.
     *
     * @return New or current tutorial name.
     */
    public String getNewTutorialName() {
        return (newTutorialName == null) ? tutorialName : newTutorialName;
    }

    /**
     * Sets the new name for the current group.
     *
     * @param newGroupName New group name.
     */
    public void setNewGroupName(String newGroupName) {
        this.newGroupName = newGroupName;
    }

    /**
     * Returns the new name for the current group.
     *
     * @param New or current group name.
     */
    public String getNewGroupName() {
        return (newGroupName == null) ? groupName : newGroupName;
    }

    /**
     * Sets the group members for the current group.
     *
     * @param groupMembers List of group members.
     */
    public void setGroupMembers(DualListModel<Student> groupMembers) {
        this.groupMembers = groupMembers;
    }

    /**
     * Returns the group members for the current course.
     *
     * @return List of group members.
     */
    public DualListModel<Student> getGroupMembers() {
        return groupMembers;
    }

    /**
     * Sets the tutors for the current tutorial.
     *
     * @param tutorialTutors List of tutorial tutors.
     */
    public void setTutorialTutors(DualListModel<PrivilegedUser> tutorialTutors) {
        this.tutorialTutors = tutorialTutors;
    }

    /**
     * Returns the tutors for the current tutorial.
     *
     * @return List of tutorial tutors.
     */
    public DualListModel<PrivilegedUser> getTutorialTutors() {
        return tutorialTutors;
    }

    /**
     * Sets the students for the current tutorial.
     *
     * @param tutorialStudents List of students.
     */
    public void setTutorialStudents(DualListModel<Student> tutorialStudents) {
        this.tutorialStudents = tutorialStudents;
    }

    /**
     * Returns the students for the current tutorial.
     *
     * @return List of students.
     */
    public DualListModel<Student> getTutorialStudents() {
        return tutorialStudents;
    }

    /**
     * Sets the removal conformation for the current tutorial.
     *
     * @param removalConformation Removal conformation.
     */
    public void setRemovalConformation(String removalConformation) {
        this.removalConformation = removalConformation;
    }

    /**
     * Returns the removal conformation for the current tutorial.
     *
     * @return Removal conformation for the current tutorial.
     */
    public String getRemovalConformation() {
        return removalConformation;
    }
}
