
package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Student;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for the Grading model class.
 *
 * @author Lorenz Huether, Kevin Scheck
 */
@Stateless
public class GradingService extends GenericService<Grading> {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(GradingService.class);

    /**
     * Lists all gradings of the GRADINGS table.
     * @return List of all gradings or an empty list if no gradings were found.
     */
    public List<Grading> listGradings() {
        List<Grading> gradingList = em.createQuery(
                "SELECT DISTINCT g "
                        + "FROM Grading g ", Grading.class).getResultList();
        return gradingList;
    }

    /**
     * Deletes all gradings from a given exam.
     * @param exam The exam from whith all gradings should be deleted from.
     * @return The number of deleted entries.
     */
    public int deleteAllGradingsFromExam(@NotNull Exam exam) {
        if (exam.getExamId() == null) {
            return 0;
        }

        return em.createQuery("DELETE FROM Grading g "
                   + " WHERE g.exam.examId = :examId")
                .setParameter("examId", exam.getExamId())
                .executeUpdate();
    }

    /**
     * Returns all students of a course.
     * @param course Course to load the students
     * @return List of Students or null
     */
    public List<Student> getStudents(Course course) {
        if (course.getCourseId() == null) {
            return null;
        }

        List<Student> s = em.createQuery("SELECT DISTINCT s " +
                    "FROM Student s " +
                    "WHERE s.course.courseId = :courseId " +
                    "AND s.isConfirmed = true " +
                    "AND s.acceptedInvitation = true")
                .setParameter("courseId", course.getCourseId())
                .getResultList();

        return (s.isEmpty() ? null : s);
    }

    /**
     * Returns all gradings of a student, i.e. also the exams without gradings.
     * @param course Course to load the exams
     * @param student Student to load the gradings
     * @return List of Gradings or null
     */
    public Map<Exam, Grading> getStudentGradings(Course course, Student student) {
        if (course.getCourseId() == null) {
            return null;
        }

        Map<Exam, Grading> m = new HashMap<>();
        /*
        Load exams manually and don't use relation in grading because there
        may be no grading for every exam.
         */
        List<Exam> exams = em.createQuery("SELECT DISTINCT e " +
                    "FROM Exam e " +
                    "WHERE e.course.courseId = :courseId ")
                .setParameter("courseId", course.getCourseId())
                .getResultList();

        List<Grading> gradings = em.createQuery("SELECT DISTINCT g " +
                    "FROM Grading g " +
                    "WHERE g.exam.course.courseId = :courseId " +
                    "AND g.student.studentId = :studentId")
                .setParameter("courseId", course.getCourseId())
                .setParameter("studentId", student.getStudentId())
                .getResultList();

        for (Exam e : exams) {
            m.put(e, null);
        }

        for (Grading g : gradings) {
            m.put(g.getExam(), g);
        }

        return m;
    }
}
