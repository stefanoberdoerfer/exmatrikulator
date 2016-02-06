package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Tutorial;
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
     * Find tutor of this course using a user object.
     *
     * @param course Course to look at.
     * @param user User associated with this tutor.
     * @return Tutor with this email or null.
     */
    public PrivilegedUser findTutor(Course course, User user) {
        List<PrivilegedUser> privUsers = em.createQuery(
                "SELECT DISTINCT u FROM PrivilegedUser u"
                + " JOIN u.course AS c"
                + " WITH c.courseId = :courseId"
                + " WHERE u.isSecretary = false"
                + " AND u.user.userId = :userId", PrivilegedUser.class)
            .setParameter("courseId", course.getCourseId())
            .setParameter("userId", user.getUserId())
            .getResultList();

        return (privUsers.isEmpty()) ? null : privUsers.get(0);
    }

    /**
     * Find tutorial by name.
     *
     * @param course Course tutorial belongs to.
     * @param name Name of the tutorial.
     * @return Tutorial with the given name or null.
     */
    public Tutorial findTutorial(Course course, String name) {
        List<Tutorial> tutorials = em.createQuery(
                "SELECT DISTINCT t FROM Tutorial t"
                + " JOIN t.course AS c"
                + " WITH c.courseId = :courseId"
                + " WHERE t.name = :tutorial", Tutorial.class)
            .setParameter("courseId", course.getCourseId())
            .setParameter("tutorial", name)
            .getResultList();

        return (tutorials.isEmpty()) ? null : tutorials.get(0);
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

    /**
     * Finds the course by its unique id.
     * @param id The unique id of the course.
     * @return The course object with the id or null if the course is not found.
     */
    public Course findById(long id) {
        List<Course> courseList = em.createQuery(
                "SELECT DISTINCT c "
                        + "FROM Course c "
                        + "WHERE c.courseId = :id", Course.class)
                .setParameter("id",id)
                .getResultList();
        return courseList.isEmpty() ? null : courseList.get(0);
    }
}
