package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;

import javax.ejb.Stateless;
import javax.mail.Part;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for the Student model class.
 *
 * @author Stefan Oberdörfer
 * @author Kevin Scheck
 */
@Stateless
public class StudentService extends GenericService<Student> {


    /**
     * Finds a student by its associated user and its course.
     * @Return The student class if it is found, null if there is no student
     *          for this user and course.
     */
    public Student findStudentInCourse(User user, Course course) {
        List<Student> students = em.createQuery(
                "SELECT DISTINCT s FROM Student s"
                + " WHERE s.user.userId = :userId"
                + " AND s.course.courseId = :courseId", Student.class)
                .setParameter("userId", user.getUserId())
                .setParameter("courseId", course.getCourseId())
                .getResultList();
        return students.isEmpty() ? null : students.get(0);
    }

    /**
     * Gets a map of participation types types off all undeleted and confirmed students.
     * @param participationTypes The list of participation types from which the
     *                           undeleted and confirmed students should be got.
     *
     * @return A map from the participation types to a list of the students.
     */
    public Map<ParticipationType, List<Student>> getUndeletedAndConfirmedStudentsOf(
            List<ParticipationType> participationTypes) {
        if (participationTypes == null) {
            throw new IllegalArgumentException("The List of ParticipatioNTypes cant be null");
        }
        Map<ParticipationType, List<Student>> partTypeStudents = new HashMap<>();
        for (ParticipationType type: participationTypes) {
            partTypeStudents.put(type, em.createQuery(
                    "SELECT DISTINCT s FROM Student s"
                    + " WHERE s.participationType.partTypeId = :typeId"
                    + " AND s.isConfirmed = true"
                    + " AND s.isDeleted = false", Student.class)
                    .setParameter("typeId", type.getPartTypeId())
                    .getResultList());
        }
        return partTypeStudents;
    }
}
