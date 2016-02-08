
package de.unibremen.opensores.service;

import de.unibremen.opensores.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for the Grading model class.
 *
 * @author Lorenz Huether, Kevin Scheck, Matthias Reichmann
 */
@Stateless
public class GradingService extends GenericService<Grading> {

    /**
     * StudentService for database transactions related to students.
     */
    @EJB
    private StudentService studentService;

    /**
     * GradeService for database transactions related to grades.
     */
    @EJB
    private GradeService gradeService;

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

    /**
     * Searches for a student via the name, email or student id (TODO!)
     * @param course Course to search in
     * @param search Value to search for
     * @return Student or null
     */
    public Student findStudent(Course course, String search) {
        try {
            return em.createQuery("SELECT DISTINCT s " +
                        "FROM Student s " +
                        "JOIN s.user AS u " +
                        "JOIN s.course AS c WITH c.courseId = :cid " +
                        "WHERE u.email = :search " +
                        "OR concat(u.firstName, ' ', u.lastName) = :search",
                        Student.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("search", search)
                    .getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }

    /**
     * Updates the pabo grade of the given student.
     * @param student Student that should be updated
     * @param paboGrade Pabo grade for this student
     */
    public void storeGrade(Student student, PaboGrade paboGrade) {
        student.setPaboGrade(paboGrade.getGradeName());

        studentService.update(student);
    }

    /**
     * Creates a new grading for a student and exam.
     * @param corrector Corrector who entered the grade
     * @param student Student who is graded
     * @param exam Exam which is graded
     * @param value Grading value
     * @param publicComment Public comment of the corrector
     * @param privateComment Private comment of the corrector
     */
    public void storeGrade(User corrector, Student student, Exam exam, String value,
                           String publicComment, String privateComment) {
        BigDecimal decimal = new BigDecimal(value.replace(',', '.'));

        Grade grade = new Grade();
        grade.setGradeType(exam.getGradeType());
        grade.setValue(decimal);
        grade.setMaxPoints(exam.getMaxPoints());
        gradeService.persist(grade);

        Grading grading = new Grading();
        grading.setCorrector(corrector);
        grading.setExam(exam);
        grading.setStudent(student);
        grading.setPublicComment(publicComment);
        grading.setPrivateComment(privateComment);
        grading.setGrade(grade);
        this.persist(grading);
    }

    /**
     * Updates an existing grading.
     * @param corrector Corrector who entered the grade
     * @param grading Grading that is updated
     * @param value New grade value
     * @param publicComment Public comment of the corrector
     * @param privateComment Private comment of the corrector
     */
    public void storeGrade(User corrector, Grading grading, String value,
                           String publicComment, String privateComment) {
        BigDecimal decimal = new BigDecimal(value.replace(',', '.'));

        Grade grade = grading.getGrade();
        grade.setGradeType(grading.getExam().getGradeType());
        grade.setValue(decimal);
        grade.setMaxPoints(grading.getExam().getMaxPoints());
        gradeService.update(grade);

        grading.setCorrector(corrector);
        grading.setPublicComment(publicComment);
        grading.setPrivateComment(privateComment);
        this.update(grading);
    }

    /**
     * Returns the exam with the given id for the given course.
     * @param course Course to search in
     * @param exam Id of the exam
     * @return Exam or null
     */
    public Exam getExam(Course course, Long exam) {
        try {
            return em.createQuery("SELECT DISTINCT e " +
                        "FROM Exam e " +
                        "JOIN e.course AS c WITH c.courseId = :cid " +
                        "WHERE e.examId = :exam",
                        Exam.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("exam", exam)
                    .getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }

    /**
     * Returns the grading of a student for an exam.
     * @param student Student whose grading is searched
     * @param exam Exam which shall be graded
     * @return Grading or null
     */
    public Grading getGrading(Student student, Exam exam) {
        try {
            return em.createQuery("SELECT DISTINCT g " +
                        "FROM Grading g " +
                        "JOIN g.exam AS e WITH e.examId = :eid " +
                        "JOIN g.student AS s WITH s.studentId = :sid",
                    Grading.class)
                    .setParameter("eid", exam.getExamId())
                    .setParameter("sid", student.getStudentId())
                    .getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }
}
