package de.unibremen.opensores.controller.tutorial;

import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.service.LogService;
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
import de.unibremen.opensores.service.PrivilegedUserService;

import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.io.IOException;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

/**
 * Controller for managing tutorials.
 *
 * @author Sören Tempel
 */
@ManagedBean
@ViewScoped
public class TutorialController {

    /**
     * The log4j logger.
     */
    private Logger log = LogManager.getLogger(TutorialController.class);

    /**
     * The currently logged in user.
     */
    private User user;

    /**
     * The user service for connection to the database.
     */
    @EJB
    private UserService userService;

    /**
     * The course service for connection to the database.
     */
    @EJB
    private CourseService courseService;

    /**
     * The tutorial service for connection to the database.
     */
    @EJB
    private TutorialService tutorialService;

    /**
     * The student service for connection to the database.
     */
    @EJB
    private StudentService studentService;

    /**
     * The group service for connection to the database.
     */
    @EJB
    private GroupService groupService;

    /**
     * The privileged user service for connection to the database.
     */
    @EJB
    private PrivilegedUserService privilegedUserService;

    /**
     * The logService for Exmatrikulator logs.
     */
    @EJB
    private LogService logService;

    /**
     * Course for this tutorial.
     */
    private Course course;

    /**
     * Current tutorial object.
     */
    private Tutorial tutorial;

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
    private Group group;

    /**
     * Name of the current group.
     */
    private String groupName;

    /**
     * Current student context.
     */
    private Student student;

    /**
     * New group name, used when the group is being edited.
     */
    private String newGroupName = null;

    /**
     * List of members for the current group.
     */
    private DualListModel<Student> groupMembers;

    /**
     * Whether the current user can manage tutorials.
     */
    private boolean manageTutorials;

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

        manageTutorials = isAllowedToManageTutorials();
        tutorialTutors = new DualListModel<>(course.getTutors(),
            new ArrayList<>());
        tutorialStudents = new DualListModel<>(course.getStudents(),
            new ArrayList<>());
        groupMembers = new DualListModel<>(course.getStudents(),
            new ArrayList<>());
    }

    /**
     * Whether or not the current user is allowed to mange tutorials.
     *
     * @return True if he is, false otherwise.
     */
    private boolean isAllowedToManageTutorials() {
        if (userService.hasCourseRole(user, Role.LECTURER, course)) {
            return true;
        }

        if (userService.hasCourseRole(user, Role.PRIVILEGED_USER, course)) {
            PrivilegedUser pu = courseService.findPrivileged(course, user.getEmail());
            if (pu == null) {
                return false;
            }

            return pu.hasPrivilege(Privilege.ManageTutorials);
        }

        return false;
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

        log.debug("Created new tutorial " + tutorial.getName());
    }

    /**
     * Changes the current tutorial context.
     *
     * @param id ID of the tutorial to swtich to.
     */
    public void changeCurrentTutorial(long id) {
        this.tutorial = tutorialService.find(Tutorial.class, id);
        this.tutorialName = tutorial.getName();

        this.newTutorialName = null;
        this.removalConformation = null;

        this.group = null;
        this.groupName = null;
        this.groupMembers.setSource(studentsWithoutGroup(tutorial));
        this.groupMembers.setTarget(new ArrayList<>());

        List<PrivilegedUser> tutors = tutorial.getTutors();
        this.tutorialTutors.setTarget(tutors);
        this.tutorialTutors.setSource(course.getTutors().stream()
                .filter(t -> !tutors.contains(t))
                .collect(Collectors.toList()));

        this.tutorialStudents.setSource(courseService
            .studentsWithoutTutorial(course));
        this.tutorialStudents.setTarget(new ArrayList<>());

        log.debug("Switched to tutorial " + tutorial.getName());
    }

    /**
     * Changes the current group context.
     *
     * @param id ID of the group to switch to.
     */
    public void changeCurrentGroup(long id) {
        Group grp = groupService.find(Group.class, id);
        changeCurrentTutorial(grp.getTutorial().getTutorialId());

        this.group = grp;
        this.groupName = group.getName();
        this.groupMembers.setTarget(group.getStudents());

        log.debug("Switched to group " + group.getName());
    }

    /**
     * Edits an existing tutorial.
     */
    public void editTutorial() {
        final String oldName = tutorial.getName();

        tutorial.setName(newTutorialName);
        tutorial = tutorialService.update(tutorial);

        tutorial = updateTutors(tutorial);
        tutorial = tutorialService.update(tutorial);

        course = tutorial.getCourse();
        log.debug(String.format("Changed tutorial name from %s to %s",
            oldName, newTutorialName));
        this.newTutorialName = null;
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

        logService.persist(Log.from(user, course.getCourseId(),
                String.format("Removed tutorial %s.", tutorial.getName())));
        tutorialService.remove(tutorial);
        course = tutorial.getCourse();

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
            tutor.getTutorials().add(tutorial);
            privilegedUserService.update(tutor);

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
        groupService.persist(group);
        logService.persist(Log.from(user, course.getCourseId(),
                String.format("Created group %s in tutorial %s.",
                        group.getName(), tutorial.getName())));
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
        log.debug(String.format("Created new group %s in tutorial %s",
            group.getName(), tutorial.getName()));
    }

    /**
     * Edits an existing group in the current tutorial.
     */
    public void editGroup() {
        final String oldName = group.getName();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        group.setName(newGroupName);
        group = groupService.update(group);
        logService.persist(Log.from(user, course.getCourseId(),
                String.format("Updated group %s in tutorial %s.",
                        group.getName(), tutorial.getName())));

        try {
            group = updateMembers(group);
        } catch (ValidationException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), e.getMessage()));
            return;
        }

        tutorial = tutorialService.update(tutorial);
        course = tutorial.getCourse();

        log.debug(String.format("Change group name from %s to %s",
            oldName, newGroupName));
        this.newGroupName = null;
    }

    /**
     * Remove the current group from the current tutorial.
     */
    public void removeGroup() {
        if (group != null && group.getStudents() != null) {
            for (Student student : group.getStudents()) {
                student.setGroup(null);
                studentService.update(student);
            }

            logService.persist(Log.from(user, course.getCourseId(),
                    String.format("Removed group %s in tutorial %s.",
                            group.getName(), tutorial.getName())));
        }

        tutorial.getGroups().remove(group);
        tutorial = tutorialService.update(tutorial);

        course = tutorial.getCourse();
        log.debug(String.format("Removed group %s from tutorial %s",
            groupName, tutorial.getName()));

        group = null;
        groupName = null;
    }

    /**
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
        for (Student student : tutorial.getStudents()) {
            if (newMembers.contains(student)) {
                if (student.getGroup() != null) {
                    continue;
                }

                student.setGroup(group);
                studentService.update(student);

                log.debug(String.format("Added student with email %s to group %s",
                    student.getUser().getEmail(), group.getName()));
                students.add(student);
            } else {
                student.setGroup(null);
                studentService.update(student);

                log.debug(String.format("Removed student with email %s from group %s",
                    student.getUser().getEmail(), group.getName()));
                students.remove(student);
            }
        }

        group.setStudents(students);
        logService.persist(Log.from(user, course.getCourseId(),
                String.format("Edited the group members of group %s in tutorial %s.",
                        group.getName(), tutorial.getName())));
        return group;
    }

    /**
     * Changes the current student context.
     *
     * @param id ID of the student to switchc to.
     */
    public void changeCurrentStudent(long id) {
        this.student = studentService.find(Student.class, id);
        changeCurrentTutorial(student.getTutorial().getTutorialId());

        log.debug("Switched to student " + student.getUser());
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
        logService.persist(Log.from(user, course.getCourseId(),
                String.format("Updated the students of tutorial %s.",
                        tutorial.getName())));
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
        logService.persist(Log.from(user, course.getCourseId(),
                String.format("Removed student %s from from tutorial %s.",
                        student.getUser(), tutorial.getName())));
    }

    /**
     * Returns a list of tutorials the current user can access.
     *
     * @return List of tutorials or null.
     */
    public List<Tutorial> getTutorials() {
        if (manageTutorials) {
            return course.getTutorials();
        }

        List<Tutorial> list = new ArrayList<>();
        if (userService.hasCourseRole(user, Role.PRIVILEGED_USER, course)) {
            PrivilegedUser pu = courseService.findPrivileged(course, user.getEmail());
            if (pu == null) {
                return null; // Should never be the case
            }

            list.addAll(pu.getTutorials());
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
     * Whether the current user is a tutorial manager.
     *
     * @return True if he is, false otherwise.
     */
    public boolean isTutorialManager() {
        return manageTutorials;
    }

    /**
     * Returns a list of all students without out a group.
     *
     * @param tutorial Tutorial to apply this function to.
     * @return List of students who are not a part of a group.
     */
    public List<Student> studentsWithoutGroup(Tutorial tutorial) {
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
