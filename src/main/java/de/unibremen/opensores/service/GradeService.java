package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Student;

import javax.ejb.Stateless;

/**
 * Service class for the Grade model class.
 *
 * @author Matthias Reichmann
 */
@Stateless
public class GradeService extends GenericService<Grade> {
    /**
     * Returns the grade name of the PaboGrade enum instance identified by the
     * given name. Returns a question mark if unknown.
     * @param name Name of the instance
     * @return Grade name or Question mark
     */
    public String paboGradeDisplayName(final String name) {
        try {
            PaboGrade paboGrade = PaboGrade.valueOf(name);

            return paboGrade.getGradeName();
        } catch (IllegalArgumentException | NullPointerException e) {

            return "?";
        }
    }
}
