package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;

import javax.ejb.Stateless;
import java.util.List;

/**
 * The service class for database transactions for privileged users.
 *
 * @autor Kevin Scheck
 */
@Stateless
public class PrivilegedUserService extends GenericService<PrivilegedUser> {
    /**
     * Finds a student by its associated user and its course.
     * @Return The student class if it is found, null if there is no student
     *          for this user and course.
     */
    public PrivilegedUser findPrivUserInCourse(User user, Course course) {
        List<PrivilegedUser> privUsers = em.createQuery(
                "SELECT DISTINCT p FROM PrivilegedUser p"
                        + " WHERE p.user.userId = :userId"
                        + " AND p.course.courseId = :courseId", PrivilegedUser.class)
                .setParameter("userId", user.getUserId())
                .setParameter("courseId", course.getCourseId())
                .getResultList();
        return privUsers.isEmpty() ? null : privUsers.get(0);
    }

}