
package de.unibremen.opensores.service;

import de.unibremen.opensores.exception.*;
import de.unibremen.opensores.model.*;
import de.unibremen.opensores.model.Group;

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
     * UserService for database transactions related to users.
     */
    @EJB
    private UserService userService;

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
     * Returns all groups of a course.
     * @param course Course to load the students
     * @return List of Students or null
     */
    public List<Group> getGroups(Course course) {
        List<Group> s = em.createQuery("SELECT DISTINCT g " +
                "FROM Group g " +
                "WHERE g.course.courseId = :courseId " +
                "ORDER BY g.name ASC")
                .setParameter("courseId", course.getCourseId())
                .getResultList();

        return (s.isEmpty() ? null : s);
    }

    /**
     * Searches the students for the given data. Searches the e-mail, name
     * and student id (TODO!)
     * @param course Course to load the students
     * @param search String to search for
     * @return List of Students or null
     */
    public List<Student> getStudents(Course course, String search) {
        List<Student> ls = em.createQuery("SELECT DISTINCT s " +
                    "FROM Student s " +
                    "JOIN s.user AS u " +
                    "JOIN s.course AS c WITH c.courseId = :cid " +
                    "WHERE lower(u.email) LIKE :search " +
                    "OR lower(concat(u.firstName, ' ', u.lastName)) LIKE :search",
                    Student.class)
                .setParameter("cid", course.getCourseId())
                .setParameter("search", "%" + search.toLowerCase() + "%")
                .getResultList();

        return (ls.isEmpty() ? null : ls);
    }

    /**
     * Returns all gradings of a student.
     * @param student Student to load the gradings
     * @return List of Gradings or null
     */
    public Map<Long, Grading> getStudentGradings(Student student) {
        Map<Long, Grading> m = new HashMap<>();

        List<Grading> gradings = em.createQuery("SELECT DISTINCT g " +
                "FROM Grading g " +
                "WHERE g.student.studentId = :studentId")
                .setParameter("studentId", student.getStudentId())
                .getResultList();

        for (Grading g : gradings) {
            m.put(g.getExam().getExamId(), g);
        }

        return m;
    }

    /**
     * Searches for a student via the name, email or student id (TODO!)
     * @param course Course to search in
     * @param search Value to search for
     * @return Student
     * @throws StudentNotFoundException
     */
    public Student findStudent(Course course, String search)
            throws StudentNotFoundException {
        try {
            return em.createQuery("SELECT DISTINCT s " +
                        "FROM Student s " +
                        "JOIN s.user AS u " +
                        "JOIN s.course AS c WITH c.courseId = :cid " +
                        "WHERE lower(u.email) = :search " +
                        "OR lower(concat(u.firstName, ' ', u.lastName)) = :search",
                        Student.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("search", search.toLowerCase())
                    .getSingleResult();
        } catch(NoResultException e) {
            throw new StudentNotFoundException();
        }
    }

    /**
     * Searches for a group via the id
     * @param course Course to search in
     * @param groupId Id of the group
     * @return Group
     * @throws GroupNotFoundException
     */
    public Group getGroup(Course course, Long groupId)
            throws GroupNotFoundException {
        try {
            return em.createQuery("SELECT DISTINCT g " +
                        "FROM Group g " +
                        "JOIN g.course AS c WITH c.courseId = :cid " +
                        "WHERE g.groupId = :gid",
                        Group.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("gid", groupId)
                    .getSingleResult();
        } catch(NoResultException e) {
            throw new GroupNotFoundException();
        }
    }

    /**
     * Updates the pabo grade of the given student.
     * @param student Student that should be updated
     * @param paboGrade Pabo grade for this student
     * @param publicComment Public comment of the corrector
     * @param privateComment Private comment of the corrector
     */
    private void persistGrade(Student student, PaboGrade paboGrade,
                           String publicComment, String privateComment) {
        student.setPaboGrade(paboGrade.getGradeName());
        student.setPublicComment(publicComment);
        student.setPrivateComment(privateComment);

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
    private void persistGrade(User corrector, Student student, Exam exam, String value,
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
    private void persistGrade(User corrector, Grading grading, String value,
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
     * Stores the pabo grade for a group, i.e. performs the persistGrade method
     * on each student of the group.
     * @param course Course whose group shall be updated
     * @param corrector User who entered the grade
     * @param group Group to update
     * @param paboGrade New pabo grade value
     * @param pPrivateComment Private comment
     * @param pPublicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws NoAccessException
     * @throws GroupNotFoundException
     * @throws OverwritingGradeException
     */
    public void storePaboGrade(final Course course, final User corrector,
                               final Group group, final PaboGrade paboGrade,
                               final String pPrivateComment,
                               final String pPublicComment,
                               final boolean overwrite)
            throws NoAccessException, GroupNotFoundException, OverwritingGradeException {
        /*
        Check if the user is a lecturer. Only lecturers may change final
        grades.
         */
        if (!userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new NoAccessException();
        }
        /*
        Check if the group exists and if he/she is part of this course.
         */
        if (group == null
                || group.getCourse().getCourseId() != course.getCourseId()) {
            throw new GroupNotFoundException();
        }
        /*
        Check if there is already a final grade for one student. Throwing
        an exception so the ajax error function gets called.
         */
        List<Student> students = group.getStudents();

        for (Student s : students) {
            if (s.getPaboGrade() != null && !overwrite) {
                throw new OverwritingGradeException();
            }
        }
        /*
        Store the final grades
         */
        for (Student s : students) {
            this.persistGrade(s, paboGrade, pPublicComment, pPrivateComment);
        }
    }

    /**
     * Updates the pabo grade for a single student. If there is already a grade
     * it will be overwritten if the flag is set.
     * @param course Course that contains the student
     * @param corrector User who entered the grade
     * @param student Student to update
     * @param paboGrade New pabo grade value
     * @param pPrivateComment Private comment
     * @param pPublicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws NoAccessException
     * @throws StudentNotFoundException
     * @throws OverwritingGradeException
     */
    public void storePaboGrade(final Course course, final User corrector,
                               final Student student, final PaboGrade paboGrade,
                               final String pPrivateComment,
                               final String pPublicComment,
                               final boolean overwrite)
            throws NoAccessException, StudentNotFoundException, OverwritingGradeException {
        /*
        Check if the user is a lecturer. Only lecturers may change final
        grades.
         */
        if (!userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new NoAccessException();
        }
        /*
        Check if the student exists and if he/she is part of this course.
         */
        if (student == null
                || student.getCourse().getCourseId() != course.getCourseId()) {
            throw new StudentNotFoundException();
        }
        /*
        Check if there is already a final grade for this student. Throwing
        an exception so the ajax error function gets called.
         */
        if (student.getPaboGrade() != null && !overwrite) {
            throw new OverwritingGradeException();
        }
        /*
        Store the final grade
         */
        this.persistGrade(student, paboGrade, pPublicComment, pPrivateComment);
    }

    /**
     * Stores the grade for a student for a single exam.
     * @param course Course that contains the exam and student
     * @param corrector User who entered the grade
     * @param exam Exam to upgrade
     * @param student Student to upgrade
     * @param pGrading New grading value
     * @param pPrivateComment Private comment
     * @param pPublicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws NoAccessException
     * @throws StudentNotFoundException
     * @throws NotGradableException
     * @throws ExamNotFoundException
     * @throws InvalidGradingException
     * @throws OverwritingGradeException
     */
    public void storeGrade(final Course course, final User corrector,
                           final Exam exam, final Student student,
                           final String pGrading, final String pPrivateComment,
                           final String pPublicComment, final boolean overwrite)
            throws NoAccessException, StudentNotFoundException,
            NotGradableException, ExamNotFoundException,
            InvalidGradingException, OverwritingGradeException {
        /*
        Check if the user is a lecturer or tutors
         */
        if (!userService.hasCourseRole(corrector, "PRIVILEGED_USER", course) &&
                !userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new NoAccessException();
        }
        /*
        Check if the student exists and if he/she is part of this course.
         */
        if (student == null
                || student.getCourse().getCourseId() != course.getCourseId()) {
            throw new StudentNotFoundException();
        }
        /*
        If the user is a tutor, check if he may grade this student
         */
        if (userService.hasCourseRole(corrector, "PRIVILEGED_USER", course) &&
                !this.mayGrade(corrector, student)) {
            throw new NotGradableException();
        }
        /*
        Check the exam.
         */
        if (exam == null
                || exam.getCourse().getCourseId() != course.getCourseId()) {
            throw new ExamNotFoundException();
        }
        /*
        Check if there is already a grading for this student
         */
        Grading grading = this.getGrading(student, exam);

        if (grading != null && !overwrite) {
            throw new OverwritingGradeException();
        }
        /*
        Check if the grading is valid
         */
        if (!exam.isValidGrading(pGrading)) {
            throw new InvalidGradingException();
        }
        /*
        Store the grading
         */
        if (grading == null) {
            this.persistGrade(corrector, student, exam, pGrading,
                    pPublicComment, pPrivateComment);
        }
        else {
            this.persistGrade(corrector, grading, pGrading,
                    pPublicComment, pPrivateComment);
        }
    }

    /**
     * Stores the grade for a group for a single exam.
     * @param course Course that contains the exam and student
     * @param corrector User who entered the grade
     * @param exam Exam to upgrade
     * @param group Group to upgrade
     * @param pGrading New grading value
     * @param pPrivateComment Private comment
     * @param pPublicComment Public comment
     * @param overwrite Flag if an existing grade shall be overwritten
     * @throws NoAccessException
     * @throws StudentNotFoundException
     * @throws NotGradableException
     * @throws ExamNotFoundException
     * @throws InvalidGradingException
     * @throws OverwritingGradeException
     */
    public void storeGrade(final Course course, final User corrector,
                           final Exam exam, final Group group,
                           final String pGrading, final String pPrivateComment,
                           final String pPublicComment, final boolean overwrite)
            throws NoAccessException, GroupNotFoundException,
            NotGradableException, ExamNotFoundException,
            InvalidGradingException, OverwritingGradeException {
        /*
        Check if the user is a lecturer or tutors
         */
        if (!userService.hasCourseRole(corrector, "PRIVILEGED_USER", course) &&
                !userService.hasCourseRole(corrector, "LECTURER", course)) {
            throw new NoAccessException();
        }
        /*
        Check if the group exists and if it is part of this course.
         */
        if (group == null
                || group.getCourse().getCourseId() != course.getCourseId()) {
            throw new GroupNotFoundException();
        }
        /*
        If the user is a tutor, check if he may grade this group
         */
        if (userService.hasCourseRole(corrector, "PRIVILEGED_USER", course) &&
                !this.mayGrade(corrector, group)) {
            throw new NotGradableException();
        }
        /*
        Check the exam.
         */
        if (exam == null
                || exam.getCourse().getCourseId() != course.getCourseId()) {
            throw new ExamNotFoundException();
        }
        /*
        Check if there is already a grading for any of the students
         */
        List<Student> students = group.getStudents();
        Map<Student, Grading> gradings = new HashMap<>();

        for (Student s : students) {
            Grading grading = this.getGrading(s, exam);

            if (grading != null && !overwrite) {
                throw new OverwritingGradeException();
            }
            else {
                gradings.put(s, grading);
            }
        }
        /*
        Check if the grading is valid
         */
        if (!exam.isValidGrading(pGrading)) {
            throw new InvalidGradingException();
        }
        /*
        Store the grading
         */
        for (Student s : students) {
            Grading grading = gradings.get(s);

            if (grading == null) {
                this.persistGrade(corrector, s, exam, pGrading,
                        pPublicComment, pPrivateComment);
            }
            else {
                this.persistGrade(corrector, grading, pGrading,
                        pPublicComment, pPrivateComment);
            }
        }
    }

    /**
     * Returns the exam with the given id for the given course.
     * @param course Course to search in
     * @param exam Id of the exam
     * @return Exam
     * @throws ExamNotFoundException
     */
    public Exam getExam(Course course, Long exam)
            throws ExamNotFoundException {
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
            throw new ExamNotFoundException();
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

    /**
     * Returns if a tutor may grade a student.
     * @param tutor Tutor who grades
     * @param student Student who should be graded
     * @return true if he may grade the student
     */
    public boolean mayGrade(User tutor, Student student) {
        List<PrivilegedUser> privileged = student.getTutorial().getTutors();

        for (PrivilegedUser p : privileged) {
            if (p.getUser().getUserId() == tutor.getUserId()) {
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
            if (p.getUser().getUserId() == tutor.getUserId()) {
                return true;
            }
        }

        return false;
    }
}
