package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.ExamEvent;
import de.unibremen.opensores.model.Student;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Service class for database transactions related to Exam Events.
 */
@Stateless
public class ExamEventService extends GenericService<ExamEvent> {

    /**
     * Gets a list of students which have not registered to an exam event of this
     * exam.
     * @param exam The exam which is used to look up the exam events.
     * @return The list of students without
     */
    public List<Student> studentsWithoutExamEvent(Exam exam) {
        List<Student> students = em.createQuery(
                "SELECT s FROM Student s "
                        + "LEFT JOIN s.examEvents AS examEvent "
                        + "WHERE examEvent.exam NOT IN (SELECT e FROM Exam e "
                                         + " WHERE e.examId = :examId)"
                        + "AND s.isDeleted = false "
                        + "AND s.isConfirmed = true", Student.class)
                .setParameter("examId", exam.getExamId())
                .getResultList();
        return students;
    }
}
