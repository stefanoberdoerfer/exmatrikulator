package de.unibremen.opensores.service;

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
