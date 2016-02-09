package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Log;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Stateless service for database transactions related to logs in the exmatrikulator.
 * @author Kevin Scheck
 */
@Stateless
public class LogService extends GenericService<Log> {

    /**
     * Gets all the logs related to a course.
     * @param course The course from which all logs should be queried.
     * @return A list of all logs of the course, sorted by newest date.
     *         The list is empty if the course has no logs.
     * @throws IllegalArgumentException If the course is null or has no course id.
     *
     */
    public List<Log> getLogFromCourse(Course course) {
        if (course == null || course.getCourseId() == null) {
            throw new IllegalArgumentException(
                    "The course can't be null and must have a valid course id");
        }
        return em.createQuery(
                "SELECT l "
               + "FROM Log l WHERE "
               + "l.courseId = :id "
               + "ORDER BY l.date DESC", Log.class)
                .setParameter("id", course.getCourseId())
                .getResultList();
    }
}
