
package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Grading;

import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service class for the Grading model class.
 *
 * @author Lorenz Huether, Kevin Scheck
 */
@Stateless
public class GradingService extends GenericService<Grading> {

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
}
