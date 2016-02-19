package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.User;

import java.util.List;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for database transactions related to Tutorials.
 */
@Stateless
public class TutorialService extends GenericService<Tutorial> {
    /**
     * Returns all students without a group in this tutorial.
     *
     * @param tutorial Tutorial students belong to.
     * @return List of students without a group.
     */
    public List<Student> studentsWithoutGroup(Tutorial tutorial) {
        List<Student> students = em.createQuery(
                "SELECT s FROM Student s "
                + "JOIN s.tutorial AS t WITH t.tutorialId = :id "
                + "WHERE s.group IS NULL", Student.class)
            .setParameter("id", tutorial.getTutorialId())
            .getResultList();

        return students;
    }

    /**
<<<<<<< 4203122d4ef5a6e83e7d1769413f7b319b39aba6
     * Returns a list that are not in the given tutorial.
     *
     * @param tutorial Tutorial to look in.
     * @return List of Tutors that are not part of this tutorial.
     */
    public List<PrivilegedUser> tutorsNotInTutorial(Tutorial tutorial) {
        List<PrivilegedUser> tutors = em.createQuery(
            "SELECT DISTINCT p From PrivilegedUser p "
            + "LEFT JOIN p.course AS c "
            + "WITH c.courseId = :cid "
            + "LEFT JOIN p.tutorials AS t "
            + "WITH t.tutorialId != :tid "
            + "WHERE t not member of p.tutorials", PrivilegedUser.class)
            .setParameter("cid", tutorial.getCourse().getCourseId())
            .setParameter("tid", tutorial.getTutorialId())
            .getResultList();

        return tutors;
    }

    /**
     * Find tutorial using a string id.
     *
     * @param idStr Tutorial id to use for lookup.
     * @return Associated course or null.
     */
    public Tutorial findTutorialById(String idStr) {
        Tutorial tutorial;
        try {
            tutorial = (idStr == null || idStr.trim().isEmpty()) ? null :
                    find(Tutorial.class, Integer.valueOf(idStr).longValue());
        } catch (NumberFormatException e) {
            tutorial = null;
        }

        return tutorial;
    }

    /**
     * Gets the PrivilegedUser object representing the tutor of a tutorial
     * given an user.
     * @param user The user which privileged User object representing the tutor
     *             should be got from the tutorial.
     * @param tutorial The tutorial from which the tutor should be got
     * @return The PrivilegedUser object or null if the user is
     *         not in the tutorial as tutor.
     */
    public PrivilegedUser getTutorOf(Tutorial tutorial, User user) {
        if (tutorial == null || user == null) {
            throw new IllegalArgumentException("The tutorial and user cant be null");
        }
        //TODO
        return null;
    }

    /**
     * Gets the Student object representing the student participating in a tutorial.
     * @param user The user which student object should be got.
     * @param tutorial The tutorial from which the student should be got
     * @return The Student object or null if the user is not in the tutorial as student.
     */
    public Student getStudentOf(Tutorial tutorial, User user) {
        if (tutorial == null || user == null) {
            throw new IllegalArgumentException("The tutorial and user cant be null");
        }
        List<Student> students = em.createQuery(
                "SELECT s FROM Student s "
                        + "JOIN s.tutorial AS t WITH t.tutorialId = :tutId "
                        + "WHERE s.user.id = :userId",Student.class)
                .setParameter("tutId", tutorial.getTutorialId())
                .setParameter("userId", user.getUserId())
                .getResultList();
        return students.isEmpty() ? null : students.get(0);
    }

}
