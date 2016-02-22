package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Course;


import javax.ejb.Stateless;
import java.util.List;

/**
 * Service class for the Course model class.
 * @todo JPA Course search
 *
 * @author Stefan Oberdörfer
 */
@Stateless
public class CourseService extends GenericService<Course> {
    /**
     * Find course using the course name.
     *
     * @param name Name of the course.
     * @return Associated course or null.
     */
    public Course findCourseByName(String name) {
        List<Course> courses = em.createQuery(
                "SELECT DISTINCT c "
                + "FROM Course c "
                + "WHERE c.name = :name", Course.class)
            .setParameter("name", name)
            .getResultList();

        return (courses.isEmpty()) ? null : courses.get(0);
    }

    /**
     * Find course using a string id.
     *
     * @param idStr Course id to use for lookup.
     * @return Associated course or null.
     */
    public Course findCourseById(String idStr) {
        Course course;
        try {
            course = (idStr == null || idStr.trim().isEmpty()) ? null :
                find(Course.class, Integer.valueOf(idStr).longValue());
        } catch (NumberFormatException e) {
            course = null;
        }

        return course;
    }

    /**
     * Find student of this course using an email.
     *
     * @param course Course to look at.
     * @param email Email associated with this student.
     * @return Student with this email or null.
     */
    public Student findStudent(Course course, String email) {
        List<Student> students = em.createQuery(
                "SELECT DISTINCT s FROM Student s"
                + " JOIN s.course AS c"
                + " WITH c.courseId = :courseId"
                + " WHERE s.user.email = :email", Student.class)
            .setParameter("courseId", course.getCourseId())
            .setParameter("email", email)
            .getResultList();

        return (students.isEmpty()) ? null : students.get(0);
    }

    /**
     * Find privileged user for this course using an email.
     *
     * @param course Course to look at.
     * @param email Email associated with this privileged user.
     * @return Privileged user with this email.
     */
    public PrivilegedUser findPrivileged(Course course, String email) {
        List<PrivilegedUser> privUsers = em.createQuery(
                "SELECT DISTINCT u FROM PrivilegedUser u"
                + " JOIN u.course AS c"
                + " WITH c.courseId = :courseId"
                + " AND u.user.email = :email", PrivilegedUser.class)
            .setParameter("courseId", course.getCourseId())
            .setParameter("email", email)
            .getResultList();

        return (privUsers.isEmpty()) ? null : privUsers.get(0);
    }

    /**
     * Find tutor of this course using an email.
     *
     * @param course Course to look at.
     * @param email Email associated with this tutor.
     * @return Tutor with this email or null.
     */
    public PrivilegedUser findTutor(Course course, String email) {
        List<PrivilegedUser> privUsers = em.createQuery(
                "SELECT DISTINCT u FROM PrivilegedUser u"
                + " JOIN u.course AS c"
                + " WITH c.courseId = :courseId"
                + " WHERE u.isSecretary = false"
                + " AND u.user.email = :email", PrivilegedUser.class)
            .setParameter("courseId", course.getCourseId())
            .setParameter("email", email)
            .getResultList();

        return (privUsers.isEmpty()) ? null : privUsers.get(0);
    }

    /**
     * Returns list of students without a tutorial.
     *
     * @param course Course to look at.
     * @return List of students without a tutorial.
     */
    public List<Student> studentsWithoutTutorial(Course course) {
        List<Student> students = em.createQuery(
                "SELECT s FROM Student s "
                + "JOIN s.course AS c WITH c.courseId = :id "
                + "WHERE s.tutorial IS NULL", Student.class)
            .setParameter("id", course.getCourseId())
            .getResultList();

        return students;
    }

    /**
     * Lists all courses of the COURSES table.
     * @return A list of all courses or an empty list if no courses were found.
     */
    public List<Course> listCourses() {
        List<Course> courseList = em.createQuery(
                "SELECT DISTINCT c "
                        + "FROM Course c ", Course.class).getResultList();
        return courseList;
    }

}
