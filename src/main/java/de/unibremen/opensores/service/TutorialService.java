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
        List<PrivilegedUser> tutors = em.createQuery(
                "SELECT DISTINCT tutor FROM PrivilegedUser tutor "
                        + "JOIN tutor.tutorials AS tutorial "
                        + "WHERE tutorial.tutorialId = :tutId "
                        + "AND tutor.user.userId = :userId "
                        + "AND tutor.isDeleted = false ", PrivilegedUser.class)
                .setParameter("tutId", tutorial.getTutorialId())
                .setParameter("userId", user.getUserId())
                .getResultList();
        return tutors.isEmpty() ? null : tutors.get(0);
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
                        + "WHERE s.user.id = :userId "
                        + "AND s.isDeleted = false "
                        + "AND s.isConfirmed = true",Student.class)
                .setParameter("tutId", tutorial.getTutorialId())
                .setParameter("userId", user.getUserId())
                .getResultList();
        return students.isEmpty() ? null : students.get(0);
    }

}
