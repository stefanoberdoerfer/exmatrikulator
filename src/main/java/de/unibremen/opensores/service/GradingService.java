
package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Grading;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Service class for the Grading model class.
 *
 * @author Lorenz Huether
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
}
