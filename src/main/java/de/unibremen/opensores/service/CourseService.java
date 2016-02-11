package de.unibremen.opensores.service;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(CourseService.class);

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
     * Finds the course by its uniqueid with all participants of
     * the course loaded to the course object.
     * @param id The unique id of the course.
     * @return The course object with the id and all its particpants
     *          or null if the course is not found.
     */
    public Course findByIdWithParticipants(long id) {
        Course course = em.find(Course.class, id);

        if (course == null) {
            return null;
        }
        // Triggering lazy fetching
        // Must log it out because of findbugs
        log.debug("Student size: " + course.getStudents().size());
        log.debug("Lecturer size: " + course.getLecturers().size());
        log.debug("Tutor size: " + course.getTutors().size());

        return course;
    }

}
