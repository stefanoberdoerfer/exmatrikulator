package de.unibremen.opensores.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DualListModel;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.service.GroupService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.TutorialService;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.ValidationException;

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
     * The currently logged in user.
     */
    private transient User user;

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
     * The student service for connection to the database.
     */
    @EJB
    private transient StudentService studentService;

    /**
     * The group service for connection to the database.
     */
    @EJB
    private transient GroupService groupService;

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
     * Current student context.
     */
    private transient Student student;

    /**
     * New group name, used when the group is being edited.
     */
    private String newGroupName = null;

    /**
     * List of members for the current group.
     */
    private DualListModel<Student> groupMembers;

    /**
     * List of tutorials for the current course.
     */
    private List<Tutorial> tutorials;

    /**
     * Method called on bean initialization.
     */
    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext exContext = facesContext.getExternalContext();

        HttpServletRequest req = (HttpServletRequest) exContext.getRequest();
        HttpServletResponse res = (HttpServletResponse) exContext.getResponse();

        user = (User) exContext.getSessionMap().get("user");
        course = courseService.findCourseById(req.getParameter("course-id"));
        if (course == null || user == null) {
            try {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e) {
                log.fatal(e);
            }
            return;
        }

        tutorialTutors = new DualListModel<>(course.getTutors(),
            new ArrayList<>());
        tutorialStudents = new DualListModel<>(course.getStudents(),
            new ArrayList<>());
        groupMembers = new DualListModel<>(course.getStudents(),
            new ArrayList<>());

        tutorials = userTutorials();
    }

    /**
     * Returns a list of tutorials the current user can access.
     *
     * @return List of tutorials or null.
     */
    private List<Tutorial> userTutorials() {
        if (userService.hasCourseRole(user, Role.LECTURER, course)) {
            return course.getTutorials();
        }

        List<Tutorial> list = new ArrayList<>();
        if (userService.hasCourseRole(user, Role.PRIVILEGED_USER, course)) {
            PrivilegedUser pu = courseService.findPrivileged(course, user.getEmail());
            if (pu == null) {
                return null; // Should never be the case
            }

            if (pu.hasPrivilege(Privilege.ManageTutorials)) {
                return course.getTutorials();
            } else {
                list.addAll(pu.getTutorials());
            }
        }

        if (userService.hasCourseRole(user, Role.STUDENT, course)) {
            Student st = courseService.findStudent(course, user.getEmail());
            if (st == null) {
                return null; // Should never be the case
            }

            list.add(st.getTutorial());
        }

        return list;
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

        this.tutorialName = null;
        tutorialTutors = new DualListModel<>(course.getTutors(),
            new ArrayList<>());

        tutorials = userTutorials();
        log.debug("Created new tutorial " + tutorial.getName());
    }

    /**
     * Changes the current tutorial context.
     *
     * @param tut Tutorial to switch to.
     */
    public void changeCurrentTutorial(@NotNull Tutorial tut) {
        log.debug("Switch to tutorial " + tut.getName());

        this.newTutorialName = null;
        this.removalConformation = null;

        this.tutorial = tutorialService.find(Tutorial.class, tut.getTutorialId());
        this.tutorialName = tutorial.getName();

        this.group = null;
        this.groupName = null;
        this.groupMembers.setSource(studentsWithoutGroup(tutorial));
        this.groupMembers.setTarget(new ArrayList<>());

        this.tutorialTutors.setSource(course.getTutors());
        this.tutorialTutors.setTarget(new ArrayList<>());

        this.tutorialStudents.setSource(courseService
            .studentsWithoutTutorial(course));
        this.tutorialStudents.setTarget(new ArrayList<>());
    }

    /**
     * Changes the current group context.
     *
     * @param grp Group to switch to.
     */
    public void changeCurrentGroup(@NotNull Group grp) {
        log.debug("Switch to group " + grp.getName());

        grp = groupService.find(Group.class, grp.getGroupId());
        changeCurrentTutorial(grp.getTutorial());

        this.group = grp;
        this.groupName = grp.getName();

        this.groupMembers.setTarget(grp.getStudents());
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
        for (Student s : tutorial.getStudents()) {
            s.setTutorial(null);
            s.setGroup(null);
        }

        tutorialService.remove(tutorial);
        tutorials = userTutorials();

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
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        group = new Group();
        group.setName(groupName);
        group.setTutorial(tutorial);

        try {
            group = updateMembers(group);
        } catch (ValidationException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), e.getMessage()));
            return;
        }

        tutorial.getGroups().add(group);
        tutorial = tutorialService.update(tutorial);

        course = tutorial.getCourse();
        tutorials = userTutorials();

        log.debug(String.format("Created new group %s in tutorial %s",
            group.getName(), tutorial.getName()));
    }

    /**
     * Edits an existing group in the current tutorial.
     */
    public void editGroup() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        try {
            group = updateMembers(group);
        } catch (ValidationException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), e.getMessage()));
            return;
        }

        group.setName(newGroupName);
        this.newGroupName = null;

        tutorial = tutorialService.update(tutorial);
    }

    /**
     * Remove the current group from the current tutorial.
     */
    public void removeGroup() {
        for (Student student : group.getStudents()) {
            student.setGroup(null);
            studentService.update(student);
        }

        tutorial.getGroups().remove(group);
        tutorial = tutorialService.update(tutorial);

        course = tutorial.getCourse();
        tutorials = userTutorials();

        log.debug(String.format("Removed group %s from tutorial %s",
            group.getName(), tutorial.getName()));

        group = null;
        groupName = null;
    }

    /*
     * Updates group members for the given group in the current tutorial.
     *
     * @param group Group to update members for.
     * @return Updated group.
     * @throws ValidationException If group size invariant is violated.
     */
    private Group updateMembers(Group group) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        Integer max = course.getMaxGroupSize();
        Integer min = course.getMinGroupSize();

        List<Student> newMembers = groupMembers.getTarget();
        int members = newMembers.size();

        if (min != null && members < min) {
            throw new ValidationException(
                    bundle.getString("courses.groupDeleteMinSize"));
        } else if (max != null && members > max) {
            throw new ValidationException(
                    bundle.getString("courses.groupCreateMaxSize"));
        } else if (members <= 0) {
            throw new ValidationException(
                    bundle.getString("courses.groupWouldBeEmpty"));
        }

        List<Student> students = new ArrayList<>();
        for (Student student : newMembers) {
            student.setGroup(group);
            log.debug(String.format("Added student with email %s to group %s",
                student.getUser().getEmail(), group.getName()));
            students.add(student);
        }

        group.setStudents(students);
        return group;
    }

    /**
     * Changes the current student context.
     *
     * @param std Student to switch to.
     */
    public void changeCurrentStudent(@NotNull Student std) {
        log.debug("Switch to student " + std.getUser());

        this.student = studentService.find(Student.class, std.getStudentId());
        changeCurrentTutorial(student.getTutorial());
    }

    /**
     * Updates the students for the current tutorial.
     */
    public void updateStudents() {
        for (Student s : tutorialStudents.getTarget()) {
            s.setTutorial(tutorial);
        }

        tutorial.setStudents(tutorialStudents.getTarget());
        tutorial = tutorialService.update(tutorial);
    }

    /**
     * Removes the current student from the current tutorial.
     */
    public void removeStudent() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        String msg = null;
        Group group = student.getGroup();
        if (group != null) {
            Integer min = course.getMinGroupSize();
            int members = group.getStudents().size();
            if (min != null && members <= min) {
                msg = bundle.getString("courses.groupDeleteMinSize");
            } else if (members <= 1) {
                msg = bundle.getString("courses.groupWouldBeEmpty");
            }
        }

        if (msg != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        tutorial.getStudents().remove(student);
        tutorial = tutorialService.update(tutorial);

        student.setGroup(null);
        student.setTutorial(null);
        student = studentService.update(student);
    }

    /**
     * Returns a list of tutorials for the current user.
     */
    public List<Tutorial> getTutorials() {
        return tutorials;
    }

    /**
     * Sets the list of tutorials for the current user.
     *
     * @param tutorials Tutorials for the current user.
     */
    public void setTutorials(List<Tutorial> tutorials) {
        this.tutorials = tutorials;
    }

    /**
     * Returns a list of all students without out a group.
     *
     * @param tutorial Tutorial to apply this function to.
     * @return List of students who are not a part of a group.
     */
    public List<Student> studentsWithoutGroup(@NotNull Tutorial tutorial) {
        return tutorialService.studentsWithoutGroup(tutorial);
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
