package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.RecordBookEntry;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Stateless service for database transactions related to Recordbookentries in the exmatrikulator.
 * @author Stefan Oberdörfer
 * @author Matthias Reichmann
 */
@Stateless
public class RecordBookService extends GenericService<RecordBookEntry> {
    public List<RecordBookEntry> getEntries(Course course, Student student) {
        return new ArrayList<>();
    }

    /**
     * Returns all entries of a user.
     * @param course Course to load entries from.
     * @param user User to load entries from
     * @return List of entries
     */
    public List<RecordBookEntry> getEntries(Course course, User user) {
        return em.createQuery("SELECT DISTINCT e "
                        + "FROM RecordBookEntry e "
                        + "WHERE e.course.courseId = :cid "
                        + "AND e.student.user.userId = :userId "
                        + "ORDER BY e.date ASC",
                    RecordBookEntry.class)
                .setParameter("cid", course.getCourseId())
                .setParameter("userId", user.getUserId())
                .getResultList();
    }

    /**
     * Adds a new entry to the record book.
     * @param course Course in which to add the entry
     * @param user User who adds the entry
     * @param exam Exam that is referenced in the entry
     * @param date Date of the entry
     * @param duration Duration in minutes
     * @param comment Optional comment
     * @throws IllegalAccessException If user may not create an entry
     */
    public void addEntry(Course course, User user, Exam exam, Date date,
                         Integer duration, String comment)
        throws IllegalAccessException {
        /*
        Get student from course and user
         */
        Student student = course.getStudentFromUser(user);

        if (student == null) {
            throw new IllegalAccessException("NO_ACCESS");
        }
        /*
        Check exam
         */
        if (exam == null || !exam.getCourse().equals(course)) {
            throw new IllegalAccessException("INVALID_EXAM");
        }
        /*
        Check duration
         */
        if (duration == null || duration < 0) {
            throw new IllegalArgumentException("INVALID_DURATION");
        }
        /*
        Strip comment of html tags. Replaces all tags with
         */
        if (comment != null) {
            comment = comment.replaceAll("<", "&lt;");
            comment = comment.replaceAll(">", "&gt;");
        }
        /*
        Store the entry.
         */
        RecordBookEntry entry = new RecordBookEntry();
        entry.setStudent(student);
        entry.setComment(comment);
        entry.setCourse(course);
        entry.setDate(date);
        entry.setDuration(duration);
        entry.setExam(exam);

        this.persist(entry);
    }

    /**
     * Updates an existing entry in the record book.
     * @param entry Entry to update
     * @param user User who updates
     * @param exam Exam referenced in the entry
     * @param date Date of the entry
     * @param duration Duration of the entry
     * @param comment Optional comment
     * @throws IllegalAccessException Thrown if user may not edit the entry.
     */
    public void editEntry(RecordBookEntry entry, User user, Exam exam, Date date,
                         Integer duration, String comment)
            throws IllegalAccessException {
        /*
        Check if the user doesn't try to change the entry of someone other.
         */
        if (!entry.getStudent().getUser().equals(user)) {
            throw new IllegalAccessException("NO_ACCESS");
        }
        /*
        Check exam
         */
        if (exam == null || !exam.getCourse().equals(entry.getCourse())) {
            throw new IllegalAccessException("INVALID_EXAM");
        }
        /*
        Check duration
         */
        if (duration == null || duration < 0) {
            throw new IllegalArgumentException("INVALID_DURATION");
        }
        /*
        Strip comment of html tags. Replaces all tags with
         */
        if (comment != null) {
            comment = comment.replaceAll("<", "&lt;");
            comment = comment.replaceAll(">", "&gt;");
        }
        /*
        Store the entry.
         */
        entry.setComment(comment);
        entry.setDate(date);
        entry.setDuration(duration);
        entry.setExam(exam);

        this.update(entry);
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
                            + "WHERE e.examId = :examId",
                    Exam.class)
                    .setParameter("cid", course.getCourseId())
                    .setParameter("examId", exam)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Removes the given entry.
     * @param course Course the entry references.
     * @param user User who added the entry.
     * @param entry Entry to remove.
     * @throws IllegalAccessException Thrown if user may not remove the entry.
     */
    public void removeEntry(Course course, User user, RecordBookEntry entry)
        throws IllegalAccessException {
        /*
        Check course and user of the entry.
         */
        if (!entry.getCourse().equals(course)) {
            throw new IllegalAccessException("NO_ACCESS");
        }

        if (!entry.getStudent().getUser().equals(user)) {
            throw new IllegalAccessException("NO_ACCESS");
        }
        /*
        Remove the entry
         */
        this.remove(entry);
    }
}
