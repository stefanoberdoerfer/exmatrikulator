package de.unibremen.opensores.service;

import com.sun.tools.javac.jvm.Gen;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.User;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Service class for database transactions related to grade Formulas.
 */
@Stateless
public class GradeFormulaService extends GenericService<GradeFormula> {
    /**
     * Returns a list of GradeFormulas where the given user is editor of.
     * @param editor User who saved the formula and whose formulas should be queried
     * @return List of GradeFormulas with the given user as editor or empty list
     */
    public List<GradeFormula> getFormulasByEditor(User editor) {
        if (editor == null || editor.getUserId() == null) {
            throw new IllegalArgumentException(
                    "The corrector can't be null and must have a valid user id");
        }
        return em.createQuery("SELECT DISTINCT g "
                        + "FROM GradeFormula g "
                        + "WHERE g.editor.id = :uid",
                GradeFormula.class)
                .setParameter("uid", editor.getUserId())
                .getResultList();
    }
}
