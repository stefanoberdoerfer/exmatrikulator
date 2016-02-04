package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Semester;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Service class for the Semester model class.
 *
 * @author Stefan Oberdörfer
 */
@Stateless
public class SemesterService extends GenericService<Semester> {
    /**
     * Lists all semesters of the SEMESTER table.
     * @return A list of all semesters or an empty list if no semester was found.
     */
    public List<Semester> listSemesters() {
        return em.createQuery(
                "SELECT DISTINCT s "
                        + "FROM Semester s ", Semester.class).getResultList();
    }
}
