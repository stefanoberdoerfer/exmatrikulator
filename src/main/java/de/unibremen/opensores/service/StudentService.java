package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;

import javax.ejb.Stateless;
import java.util.List;

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
}
