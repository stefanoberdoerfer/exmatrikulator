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
     * Find tutor of this course using an email from the user object.
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
