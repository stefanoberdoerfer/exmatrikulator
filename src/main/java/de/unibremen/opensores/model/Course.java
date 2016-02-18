package de.unibremen.opensores.model;

import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity bean for the Course class.
 *
 * @author Stefan Oberdoerfer
 * @author Sören Tempel
 * @author Lorenz Hüther
 * @author Matthias Reichmann
 */
@Entity
@Table(name = "COURSES")
public class Course {

    @Id
    @GeneratedValue
    private Long courseId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer sws;

    @Column(nullable = false)
    private Integer creditPoints;

    @ElementCollection
    @CollectionTable(name = "COURSE_NUMBERS", joinColumns = @JoinColumn(name = "courseId"))
    @Column
    private List<String> numbers = new ArrayList<>();

    @Column(nullable = false)
    private Boolean requiresConfirmation;

    @Column(nullable = false)
    private Boolean studentsCanSeeFormula;

    @Column(nullable = true)
    @Type(type = "date")
    private Date lastFinalization;

    @OneToOne(mappedBy = "course", cascade = CascadeType.MERGE)
    private MailTemplate emailTemplate;

    @Column
    private Integer minGroupSize;

    @Column
    private Integer maxGroupSize;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Group> groups = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Lecturer> lecturers = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tutorial> tutorials = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<PrivilegedUser> tutors = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<ParticipationType> participationTypes = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Exam> exams = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "semesterId")
    private Semester semester;


    /**
     * Checks if an user is associated with this course in any way (as student,
     * privileged user, or lecturer).
     * @param user The user for whom should be checked if hes in the course.
     * @return False if the user is null or not in the course as student,
     *         privileged user, or lecturer; True otherwise.
     */
    public boolean containsUser(User user) {
        if (user == null) {
            return false;
        }
        for (Student student: students) {
            if (student.getUser().equals(user)) {
                return true;
            }
        }
        for (PrivilegedUser privilegedUser: tutors) {
            if (privilegedUser.getUser().equals(user)) {
                return true;
            }
        }
        for (Lecturer lecturer: lecturers) {
            if (lecturer.getUser().equals(user)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Gets the student association from a given user. This method ignores the
     * hidden attribute in the student association.
     * @param user The user for whom the student association should be searched for.
     * @return The Student association from the user in this course, if it is available.
     *         Null, if the user is null or the user doesn't have a student association
     *         in this course.
     */
    public Student getStudentFromUser(User user) {
        if (user == null) {
            return null;
        }
        for (Student student: students) {
            if (student.getUser().equals(user)) {
                return student;
            }
        }
        return null;
    }

    /**
     * Gets the privileged user association from a given user. This method ignores the
     * hidden attribute in the privileged user association.
     * @param user The user for whom the privileged user association should be searched for.
     * @return The PrivilegedUser association from the user in this course, if it is available.
     *         Null, if the user is null or the user doesn't have a privileged User association
     *         in this course.
     */
    public PrivilegedUser getPrivilegedUserFromUser(User user) {
        if (user == null) {
            return null;
        }
        for (PrivilegedUser privilegedUser: tutors) {
            if (privilegedUser.getUser().equals(user)) {
                return privilegedUser;
            }
        }
        return null;
    }


    /**
     * Gets the lecturer role from a given user.
     * @return The lecturer role from a given user, or null if the user is not a
     *         lecturer in this course.
     */
    public Lecturer getLecturerFromUser(User user) {
        if (user == null) {
            return null;
        }
        for (Lecturer lecturer: lecturers) {
            if (lecturer.getUser().equals(user)) {
                return lecturer;
            }
        }
        return null;
    }

    /**
     * Gets the students which are not hidden in this course
     * @return The undeleted students of this course.
     */
    public List<Student> getConfirmedUndeletedStudents() {
        return students.stream()
                .filter(student -> !student.isDeleted() && student.isConfirmed())
                .collect(Collectors.toList());
    }

    /**
     * Gets the students which have registered to the course by themselves but
     * are not confirmed by the lecturers or privileged users yet. The students
     * also shouldn't be hidden.
     * @return The list of unconfirmed students.
     */
    public List<Student> getUnconfirmedAndUndeletedStudents() {
        return students.stream()
                .filter(student -> !student.isDeleted() && !student.isConfirmed())
                .collect(Collectors.toList());
    }

    /**
     * Gets the lecturers which are not hidden in this course.
     * @return The unhidden lecturers of this course.
     */
    public List<Lecturer> getUndeletedLecturers() {
        return lecturers.stream()
                .filter(lecturer -> !lecturer.isDeleted())
                .collect(Collectors.toList());
    }

    /**
     * Gets the unhidden tutors of this course.
     * @return The unhidden tutors of this course.
     */
    public List<PrivilegedUser> getUndeletedTutors() {
        return tutors.stream()
                .filter(tutor -> !tutor.isDeleted())
                .collect(Collectors.toList());
    }

    /**
     * Gets the default participation type
     * @return The default participation type of this course, or null if this
     *         course has no participation type.
     */
    public ParticipationType getDefaultParticipationType() {
        for (ParticipationType type: participationTypes) {
            if (type.isDefaultParttype()) {
                return type;
            }
        }
        return null;
    }


    public Integer getCreditPoints() {
        return creditPoints;
    }

    public void setCreditPoints(Integer creditPoints) {
        this.creditPoints = creditPoints;
    }

    public Integer getSws() {
        return sws;
    }

    public void setSws(Integer sws) {
        this.sws = sws;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public List<PrivilegedUser> getTutors() {
        return tutors;
    }

    public void setTutors(List<PrivilegedUser> tutors) {
        this.tutors = tutors;
    }


    public List<Lecturer> getLecturers() {
        return lecturers;
    }

    public void setLecturers(List<Lecturer> lecturers) {
        this.lecturers = lecturers;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public void setCourseId(long id) {
        this.courseId = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public List<Tutorial> getTutorials() {
        return tutorials;
    }

    public void setTutorials(List<Tutorial> tutorials) {
        this.tutorials = tutorials;
    }

    public List<ParticipationType> getParticipationTypes() {
        return participationTypes;
    }

    public void setParticipationTypes(List<ParticipationType> participationTypes) {
        this.participationTypes = participationTypes;
    }

    public MailTemplate getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(MailTemplate emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public Boolean getStudentsCanSeeFormula() {
        return studentsCanSeeFormula;
    }

    public void setStudentsCanSeeFormula(Boolean studentsCanSeeFormula) {
        this.studentsCanSeeFormula = studentsCanSeeFormula;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public Boolean getRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(Boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public Integer getMinGroupSize() {
        return minGroupSize;
    }

    public void setMinGroupSize(Integer minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    public Integer getMaxGroupSize() {
        return maxGroupSize;
    }

    public void setMaxGroupSize(Integer maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    /**
     * Returns the date of the last finalization of the grades. Custom getter
     * necessary to not expose internal representation.
     * @return Date of the last finalization
     */
    public Date getLastFinalization() {
        if (lastFinalization == null) {
            return null;
        } else {
            return new Date(lastFinalization.getTime());
        }
    }

    /**
     * Sets the date of the last finalization of the grades. Custom setter
     * necessary to not expose internal representation.
     * @param lastFinalization New date of last finalization
     */
    public void setLastFinalization(final Date lastFinalization) {
        if (lastFinalization == null) {
            this.lastFinalization = null;
        } else {
            this.lastFinalization = new Date(lastFinalization.getTime());
        }
    }
}
