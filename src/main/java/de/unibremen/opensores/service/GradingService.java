
package de.unibremen.opensores.service;

import de.unibremen.opensores.exception.AlreadyGradedException;
import de.unibremen.opensores.exception.InvalidGradeException;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;

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
 * @author Lorenz Huether
 * @author Kevin Scheck
 * @author Matthias Reichmann
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
     * UserService for database transactions related to users.
     */
    @EJB
    private UserService userService;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    @EJB
    private LogService logService;

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
     * @param corrector Corrector who wants to see the students
     * @return List of Students or null
     */
    public List<Student> getStudents(Course course, User corrector) {
        List<Student> s;

        if (userService.hasCourseRole(corrector, "LECTURER", course)) {
            s = em.createQuery("SELECT DISTINCT s "
                            + "FROM Student s "
                            + "WHERE s.course.courseId = :courseId "
                            + "AND s.isConfirmed = true",
                        Student.class)
                    .setParameter("courseId", course.getCourseId())
                    .getResultList();
        } else {
            s = em.createQuery("SELECT s "
                            + "FROM Student s "
                            + "JOIN s.tutorial.tutors AS t "
                            + "WHERE s.course.courseId = :courseId "
                            + "AND s.isConfirmed = true "
                            + "AND t.user.userId = :correctorId",
                        Student.class)
                    .setParameter("courseId", course.getCourseId())
                    .setParameter("correctorId", corrector.getUserId())
                    .getResultList();
        }

        return (s.isEmpty() ? null : s);
    }

    /**
     * Searches the students for the given data. Searches the e-mail, name
     * and matriculation number.
     * @param course Course to load the students
     * @param corrector Corrector who wants to see the students
     * @param search String to search for
     * @return List of Students or null
     */
    public List<Student> getStudents(Course course, User corrector, String search) {
        List<Student> ls;

        if (userService.hasCourseRole(corrector, "LECTURER", course)) {
            ls = em.createQuery("SELECT DISTINCT s "
                            + "FROM Student s "
                            + "JOIN s.user AS u "
                            + "JOIN s.course AS c WITH c.courseId = :cid "
                            + "WHERE (lower(u.email) LIKE :search "
                            + "OR lower(concat(u.firstName, ' ', u.lastName)) "
                            + "LIKE :search "
                            + "OR u.matriculationNumber LIKE :search) "
                            + "AND s.isConfirmed = TRUE",
                        Student.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("search", "%" + search.toLowerCase() + "%")
                    .getResultList();
        } else {
            ls = em.createQuery("SELECT s "
                            + "FROM Student s "
                            + "JOIN s.tutorial.tutors AS t "
                            + "JOIN s.user AS u "
                            + "JOIN s.course AS c WITH c.courseId = :cid "
                            + "WHERE (lower(u.email) LIKE :search "
                            + "OR lower(concat(u.firstName, ' ', u.lastName)) "
                            + "LIKE :search "
                            + "OR u.matriculationNumber LIKE :search) "
                            + "AND s.isConfirmed = TRUE "
                            + "AND t.user.userId = :correctorId",
                        Student.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("search", "%" + search.toLowerCase() + "%")
                    .setParameter("correctorId", corrector.getUserId())
                    .getResultList();
        }

        return (ls.isEmpty() ? null : ls);
    }

    /**
     * Returns all groups of a course.
     * @param course Course to load the students
     * @param corrector Corrector who wants to see the students
     * @return List of Students or null
     */
    public List<Group> getGroups(Course course, User corrector) {
        List<Group> s;

        if (userService.hasCourseRole(corrector, "LECTURER", course)) {
            s = em.createQuery("SELECT DISTINCT g "
                            + "FROM Group g "
                            + "WHERE g.tutorial.course.courseId = :courseId "
                            + "ORDER BY g.name ASC",
                        Group.class)
                    .setParameter("courseId", course.getCourseId())
                    .getResultList();
        } else {
            s = em.createQuery("SELECT g "
                            + "FROM Group g "
                            + "JOIN g.tutorial.tutors AS t "
                            + "WHERE g.tutorial.course.courseId = :courseId "
                            + "AND t.user.userId = :correctorId "
                            + "ORDER BY g.name ASC",
                        Group.class)
                    .setParameter("courseId", course.getCourseId())
                    .setParameter("correctorId", corrector.getUserId())
                    .getResultList();
        }

        return (s.isEmpty() ? null : s);
    }

    /**
     * Returns all gradings of a student.
     * @param student Student to load the gradings
     * @return List of Gradings or null
     */
    public Map<Long, Grading> getStudentGradings(Student student) {
        Map<Long, Grading> m = new HashMap<>();

        List<Grading> gradings = em.createQuery("SELECT DISTINCT g "
                        + "FROM Grading g "
                        + "WHERE g.student.studentId = :studentId",
                    Grading.class)
                .setParameter("studentId", student.getStudentId())
                .getResultList();

        for (Grading g : gradings) {
            m.put(g.getExam().getExamId(), g);
        }

        return m;
    }

    /**
     * Searches for a student via the name, email or matriculation number.
     * @param course Course to search in
     * @param search Value to search for
     * @return Student
     */
    public Student findStudent(Course course, String search) {
        try {
            return em.createQuery("SELECT DISTINCT s "
                            + "FROM Student s "
                            + "JOIN s.user AS u "
                            + "JOIN s.course AS c WITH c.courseId = :cid "
                            + "WHERE lower(u.email) = :search "
                            + "OR lower(concat(u.firstName, ' ', u.lastName)) "
                            + "= :search "
                            + "OR u.matriculationNumber = :search",
                        Student.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("search", search.toLowerCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Searches for a group via the id.
     * @param course Course to search in
     * @param groupId Id of the group
     * @return Group
     */
    public Group getGroup(Course course, Long groupId) {
        try {
            return em.createQuery("SELECT DISTINCT g "
                            + "FROM Group g "
                            + "JOIN g.tutorial AS t "
                            + "JOIN t.course AS c WITH c.courseId = :cid "
                            + "WHERE g.groupId = :gid",
                        Group.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("gid", groupId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Updates the pabo grade of the given student.
     * @param corrector Lecturer who changed the grade
     * @param student Student that should be updated
     * @param paboGrade Pabo grade for this student
     * @param privateComment Private comment of the corrector
     * @param publicComment Public comment of the corrector
     */
    private void persistGrade(User corrector, Student student, PaboGrade paboGrade,
                           String privateComment, String publicComment) {
        final String oldGrade = student.getPaboGrade();
        student.setPaboGrade(paboGrade.name());
        student.setPublicComment(publicComment);
        student.setPrivateComment(privateComment);

        studentService.update(student);
        /*
        Log the change
         */
        String description;

        if (oldGrade == null) {
            description = "added final grade of "
                    + student.getUser().getFirstName() + " "
                    + student.getUser().getLastName() + " ("
                    + student.getUser().getMatriculationNumber() + "): "
                    + paboGrade.name();
        } else {
            description = "changed final grade of "
                    + student.getUser().getFirstName() + " "
                    + student.getUser().getLastName() + " ("
                    + student.getUser().getMatriculationNumber() + ") from "
                    + oldGrade + " to "
                    + paboGrade.name();
        }
        /*
        Change the formula so the student knows it wasn't used for him/her.
         */
        student.setPaboGradeFormula("Manually set by "
                + corrector.getFirstName() + " "
                + corrector.getLastName());

        logService.persist(Log.from(corrector,
                student.getCourse().getCourseId(), description));
    }

    /**
     * Creates a new grading for a student and exam.
     * @param corrector Corrector who entered the grade
     * @param student Student who is graded
     * @param exam Exam which is graded
     * @param value Grading value
     * @param privateComment Private comment of the corrector
     * @param publicComment Public comment of the corrector
     */
    private void persistGrade(User corrector, Student student, Exam exam,
                              BigDecimal value, String privateComment,
                              String publicComment) {
        Grade grade = new Grade();
        grade.setGradeType(exam.getGradeType());
        grade.setValue(value);
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
        /*
        Log the change
         */
        String description = "added grading of "
                + student.getUser().getFirstName() + " "
                + student.getUser().getLastName() + " ("
                + student.getUser().getMatriculationNumber() + ") for "
                + exam.getName() + ": " + value;

        logService.persist(Log.from(corrector,
                student.getCourse().getCourseId(), description));
    }

    /**
     * Updates an existing grading.
     * @param corrector Corrector who entered the grade
     * @param grading Grading that is updated
     * @param value New grade value
     * @param privateComment Private comment of the corrector
     * @param publicComment Public comment of the corrector
     */
    private void persistGrade(User corrector, Grading grading, BigDecimal value,
                              String privateComment, String publicComment) {
        final BigDecimal oldGrade = grading.getGrade().getValue();
        Grade grade = grading.getGrade();
        grade.setGradeType(grading.getExam().getGradeType());
        grade.setValue(value);
        grade.setMaxPoints(grading.getExam().getMaxPoints());
        gradeService.update(grade);

        grading.setCorrector(corrector);
        grading.setPublicComment(publicComment);
        grading.setPrivateComment(privateComment);
        this.update(grading);
        /*
        Log the change
         */
        Student student = grading.getStudent();
        Exam exam = grading.getExam();

        String description = "changed grading of "
                + student.getUser().getFirstName() + " "
                + student.getUser().getLastName() + " ("
                + student.getUser().getMatriculationNumber() + ") for "
                + exam.getName() + " from " + oldGrade
                + " to " + value;

        logService.persist(Log.from(corrector,
                student.getCourse().getCourseId(), description));
    }

    /**
     * Stores the pabo grade for a group, i.e. performs the persistGrade method
     * on each student of the group.
     * @param course Course whose group shall be updated
     * @param corrector User who entered the grade
     * @param group Group to update
     * @param paboGrade New pabo grade value
     * @param privateComment Private comment
     * @param publicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws IllegalAccessException Thrown if user may not grade the group
     */
    public void storePaboGrade(final Course course, final User corrector,
                               final Group group, final PaboGrade paboGrade,
                               final String privateComment,
                               final String publicComment,
                               final boolean overwrite)
            throws IllegalAccessException, AlreadyGradedException {
        /*
        Check if the user is a lecturer. Only lecturers may change final
        grades.
         */
        if (!userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new IllegalAccessException("NO_ACCESS");
        }
        /*
        Check if there is already a final grade for one student. Throwing
        an exception so the ajax error function gets called.
         */
        List<Student> students = group.getStudents();

        for (Student s : students) {
            if (s.getPaboGrade() != null && !overwrite) {
                throw new AlreadyGradedException();
            }
        }
        /*
        Store the final grades
         */
        for (Student s : students) {
            this.persistGrade(corrector, s, paboGrade, privateComment,
                    publicComment);
        }
    }

    /**
     * Updates the pabo grade for a single student. If there is already a grade
     * it will be overwritten if the flag is set.
     * @param course Course that contains the student
     * @param corrector User who entered the grade
     * @param student Student to update
     * @param paboGrade New pabo grade value
     * @param privateComment Private comment
     * @param publicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws IllegalAccessException Thrown if user may not grade this student
     */
    public void storePaboGrade(final Course course, final User corrector,
                               final Student student, final PaboGrade paboGrade,
                               final String privateComment,
                               final String publicComment,
                               final boolean overwrite)
            throws IllegalAccessException, AlreadyGradedException {
        /*
        Check if the user is a lecturer. Only lecturers may change final
        grades.
         */
        if (!userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new IllegalAccessException("NO_ACCESS");
        }
        /*
        Check if there is already a final grade for this student. Throwing
        an exception so the ajax error function gets called.
         */
        if (student.getPaboGrade() != null && !overwrite) {
            throw new AlreadyGradedException();
        }
        /*
        Store the final grade
         */
        this.persistGrade(corrector, student, paboGrade, privateComment,
                publicComment);
    }

    /**
     * Stores the grade for a student for a single exam.
     * @param course Course that contains the exam and student
     * @param corrector User who entered the grade
     * @param exam Exam to upgrade
     * @param student Student to upgrade
     * @param value New grading value
     * @param privateComment Private comment
     * @param publicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws IllegalAccessException Thrown if user may not grade this student
     * @throws InvalidGradeException Thrown if the grading is invalid
     */
    public void storeGrade(final Course course, final User corrector,
                           final Exam exam, final Student student,
                           final BigDecimal value, final String privateComment,
                           final String publicComment, final boolean overwrite)
            throws IllegalAccessException, InvalidGradeException,
            AlreadyGradedException {
        /*
        Check if the user is a lecturer or tutors
         */
        if (!userService.hasCourseRole(corrector, "PRIVILEGED_USER", course)
                && !userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new IllegalAccessException("NO_ACCESS");
        }
        /*
        If the user is a tutor, check if he may grade this student
         */
        if (!userService.hasCourseRole(corrector, "LECTURER", course)
                && (!exam.isGradableByTutors()
                || !this.mayGrade(corrector, student))) {
            throw new IllegalAccessException("NOT_GRADABLE");
        }
        /*
        Check if there is already a grading for this student
         */
        Grading grading = this.getGrading(student, exam);

        if (grading != null && !overwrite) {
            throw new AlreadyGradedException();
        }
        /*
        Check if the grading is valid
         */
        if (!exam.isValidGrading(value)) {
            throw new InvalidGradeException("INVALID_GRADE");
        }
        /*
        Store the grading
         */
        if (grading == null) {
            this.persistGrade(corrector, student, exam, value,
                    privateComment, publicComment);
        } else {
            this.persistGrade(corrector, grading, value,
                    privateComment, publicComment);
        }
    }

    /**
     * Stores the grade for a group for a single exam.
     * @param course Course that contains the exam and student
     * @param corrector User who entered the grade
     * @param exam Exam to upgrade
     * @param group Group to upgrade
     * @param value New grading value
     * @param privateComment Private comment
     * @param publicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws IllegalAccessException Thrown if user may not grade
     * @throws InvalidGradeException Thrown if value is invalid
     */
    public void storeGrade(final Course course, final User corrector,
                           final Exam exam, final Group group,
                           final BigDecimal value, final String privateComment,
                           final String publicComment, final boolean overwrite)
            throws IllegalAccessException, InvalidGradeException, AlreadyGradedException {
        /*
        Check if the user is a lecturer or tutors
         */
        if (!userService.hasCourseRole(corrector, "PRIVILEGED_USER", course)
                && !userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new IllegalAccessException("NO_ACCESS");
        }
        /*
        If the user is a tutor, check if he may grade this group
         */
        if (!userService.hasCourseRole(corrector, "LECTURER", course)
                && (!exam.isGradableByTutors()
                || !this.mayGrade(corrector, group))) {
            throw new IllegalAccessException("NOT_GRADABLE");
        }
        /*
        Check if there is already a grading for any of the students
         */
        List<Student> students = group.getStudents();
        Map<Student, Grading> gradings = new HashMap<>();

        for (Student s : students) {
            Grading grading = this.getGrading(s, exam);

            if (grading != null && !overwrite) {
                throw new AlreadyGradedException();
            } else {
                gradings.put(s, grading);
            }
        }
        /*
        Check if the grading is valid
         */
        if (!exam.isValidGrading(value)) {
            throw new InvalidGradeException("INVALID_GRADE");
        }
        /*
        Store the grading
         */
        for (Student s : students) {
            Grading grading = gradings.get(s);

            if (grading == null) {
                this.persistGrade(corrector, s, exam, value,
                        privateComment, publicComment);
            } else {
                this.persistGrade(corrector, grading, value,
                        privateComment, publicComment);
            }
        }
    }

    /**
     * Returns the exam with the given id for the given course.
     * @param course Course to search in
     * @param exam Id of the exam
     * @return Exam
     */
    public Exam getExam(Course course, Long exam) {
        try {
            return em.createQuery("SELECT DISTINCT e "
                            + "FROM Exam e "
                            + "JOIN e.course AS c WITH c.courseId = :cid "
                            + "WHERE e.examId = :exam",
                        Exam.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("exam", exam)
                    .getSingleResult();
        } catch (NoResultException e) {
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
            return em.createQuery("SELECT DISTINCT g "
                            + "FROM Grading g "
                            + "JOIN g.exam AS e WITH e.examId = :eid "
                            + "JOIN g.student AS s WITH s.studentId = :sid",
                        Grading.class)
                    .setParameter("eid", exam.getExamId())
                    .setParameter("sid", student.getStudentId())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns a List of Gradings where the given user is corrector of.
     * @param corrector User whose created gradings should be queried
     * @return List of gradings with the given user as creator or empty list
     */
    public List<Grading> getGradingsByCorrector(User corrector) {
        if (corrector == null || corrector.getUserId() == null) {
            throw new IllegalArgumentException(
                    "The corrector can't be null and must have a valid user id");
        }
        return em.createQuery("SELECT DISTINCT g "
                        + "FROM Grading g "
                        + "WHERE g.corrector.id = :uid",
                Grading.class)
                .setParameter("uid", corrector.getUserId())
                .getResultList();
    }

    /**
     * Returns if a tutor may grade a student.
     * @param tutor Tutor who grades
     * @param student Student who should be graded
     * @return true if he may grade the student
     */
    public boolean mayGrade(User tutor, Student student) {
        List<PrivilegedUser> privileged = student.getTutorial().getTutors();

        for (PrivilegedUser p : privileged) {
            if (p.getUser().getUserId().equals(tutor.getUserId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns if a tutor may grade a group.
     * @param tutor Tutor who grades
     * @param group Group that should be graded
     * @return true if he may grade the student
     */
    public boolean mayGrade(User tutor, Group group) {
        if (group.getTutorial() == null) {
            return false;
        }

        List<PrivilegedUser> privileged = group.getTutorial().getTutors();

        for (PrivilegedUser p : privileged) {
            if (p.getUser().getUserId().equals(tutor.getUserId())) {
                return true;
            }
        }

        return false;
    }
}
