package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;

import javax.ejb.Stateless;

/**
 * Service class for database transactions related to Exams.
 */
@Stateless
public class ExamService extends GenericService<Exam> {

    /**
     * Find exam using a string id.
     *
     * @param idStr Exam id to use for lookup.
     * @return Associated exam if found or null if the id is not associated with
     *         a course or is not a valid long id.
     */
    public Exam findExamById(String idStr) {
        Exam exam;
        try {
            exam = (idStr == null || idStr.trim().isEmpty()) ? null :
                    find(Exam.class, Integer.valueOf(idStr).longValue());
        } catch (NumberFormatException e) {
            exam = null;
        }

        return exam;
    }

}
