package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Semester;

import java.util.Calendar;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.Timer;

/**
 * Service class for the Semester model class.
 *
 * @author Stefan Oberdörfer, Lorenz Hüther
 */
@Stateless
public class SemesterService extends GenericService<Semester> {
    /**
     * Finds a semester.
     *
     * @param year Semester year.
     * @param winter True if the semester should be a winter semester,
     *   false otherwise.
     * @return Semester with given parameters or false.
     */
    public Semester findSemester(int year, boolean winter) {
        List<Semester> semesters = em.createQuery(
                "SELECT DISTINCT s FROM Semester s "
                + "WHERE s.semesterYear = :year "
                + "AND s.isWinter = :winter", Semester.class)
            .setParameter("year", year)
            .setParameter("winter", winter)
            .getResultList();

        return (semesters.isEmpty()) ? null : semesters.get(0);
    }

    /**
     * Lists all semesters of the SEMESTER table.
     * @return A list of all semesters or an empty list if no semester was found.
     */
    public List<Semester> listSemesters() {
        return em.createQuery(
                "SELECT DISTINCT s "
                        + "FROM Semester s ", Semester.class).getResultList();
    }

    /**
     * Generates a new Semester for every SoSe / WiSe ten years ahead.
     */
    public void scheduledSemesterCreator(Timer timer) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);

        Semester soSe = new Semester();
        Semester wiSe = new Semester();

        soSe.setSemesterYear(year + 10);
        soSe.setIsWinter(false);
        wiSe.setSemesterYear(year + 10);
        wiSe.setIsWinter(true);

        em.persist(soSe);
        em.persist(wiSe);
    }

    /**
     * Creates the next n Semesters.
     *
     * @param nextSemesters the next n semester to create.
     */
    public void createNextSemesters(int nextSemesters) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);

        for (int n = 0; n <= nextSemesters; n++) {
            Semester soSe = new Semester();
            Semester wiSe = new Semester();

            soSe.setIsWinter(false);
            soSe.setSemesterYear(year + n);
            wiSe.setIsWinter(true);
            wiSe.setSemesterYear(year + n);

            em.persist(soSe);
            em.persist(wiSe);
        }
    }
}
