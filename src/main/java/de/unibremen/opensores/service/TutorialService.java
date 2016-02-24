package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Tutorial;

import java.util.List;
import javax.ejb.Stateless;

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
}
