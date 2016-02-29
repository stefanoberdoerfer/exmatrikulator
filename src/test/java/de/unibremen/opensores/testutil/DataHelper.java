package de.unibremen.opensores.testutil;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.GlobalRole;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PaboData;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Semester;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;

/**
 * An utility class which creates basic test data objects, like logged in user
 * objects, which can be used by many test classes.
 * @author Kevin Scheck
 */
public final class DataHelper {

    /**
     * A static id counter for creating separate ids in this class.
     */
    private static long idCount = 1;

    /**
     * The id of the test course passed as http parameter in the mocked request.
     * It is also used to find the course by the CourseService class.
     * This constant should be used for the course id in tests.
     */
    public static final long COURSE_ID = idCount++;

    /**
     * String value of the course id.
     */
    public static final String COURSE_ID_STR = String.valueOf(COURSE_ID);

    /**
     * The id of the default participation type of the basic course.
     */
    public static final long DEFAULT_PART_TYPE_ID = idCount++;

    public static final long BASIC_USER_ID = idCount++;

    public static final long GLOBAL_LECTURER_ID = idCount++;


    /**
     * Private constructor so no objects of this class can be created.
     */
    private DataHelper() {
        // Do nothing
    }

    /**
     * Gets an id for custom object ids.
     * The id increments after calling this method.
     * @return The id which gets incremented after calling this method.
     */
    public static long getNewObjectId() {
        return idCount++;
    }

    /**
     * Creates a basic Course object.
     * @return The basic course object.
     */
    public static Course createBasicCourse() {
        Course course = new Course();
        course.setName("Test Course");
        course.setCourseId(COURSE_ID);
        course.setDefaultCreditPoints(9);
        course.getNumbers().add("VAK TEST-COURSE");
        course.setMinGroupSize(1);
        course.setMaxGroupSize(6);
        course.setRequiresConfirmation(true);
        course.setDefaultSws("2");

        Semester semester = new Semester();
        semester.setIsWinter(true);
        semester.setSemesterYear(2016);
        course.setSemester(semester);

        ParticipationType participationType = new ParticipationType();
        participationType.setIsDefaultParttype(true);
        participationType.setPartTypeId(DEFAULT_PART_TYPE_ID);
        participationType.setName("Default participation type");
        participationType.setCourse(course);
        GradeFormula formula = createGradeFormulaIn(participationType);

        course.getParticipationTypes().add(participationType);
        return course;
    }

    /**
     * Creates a basic dumymuser object which has basic properties,
     * like first name, last name, etc.
     * The user has only the Global Role USER.
     * The user has the id BASIC_USER_ID.
     * @return The basic user.
     */
    public static User createBasicUser() {
        User user = new User();
        user.setEmail("test@exmat.de");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("testuserpass");
        user.setBlocked(false);
        user.setProfileInfo("Hi, I'm a test user.");
        user.setMatriculationNumber("123456");
        user.addRole(GlobalRole.USER);
        user.setUserId(BASIC_USER_ID);
        return user;
    }

    /**
     * Creates a basic dumymuser object which has basic properties,
     * like first name, last name, etc.
     * The user has a custom user id.
     * @return The basic user.
     */
    public static User createBasicUserWithNewId() {
        User user = createBasicUser();
        user.setUserId(getNewObjectId());
        return user;
    }

    /**
     * Creates an user which has also the GlobalRole LECTURER.
     * The user has the user id GLOBAL_LECTURER_ID.
     * @return The created user which has Global Lecturer role.
     */
    public static User createBasicGlobalLecturer() {
        User lecturer = createBasicUser();
        lecturer.setLastName("GlobalLecturer");
        lecturer.addRole(GlobalRole.LECTURER);
        lecturer.setUserId(GLOBAL_LECTURER_ID);
        return lecturer;
    }

    /**
     * Creates a basic lecturer with the given course and user.
     * The lecturer is not hidden.
     * @Post The course adds the lecturer to its objects.
     * @param course The course in which the user is lecturer.
     * @param user The user which should be lecturer.
     * @return The newly created lecturer object.
     */
    public static Lecturer createLecturerWith(Course course, User user) {
        Lecturer lecturer = new Lecturer();
        course.getLecturers().add(lecturer);
        lecturer.setUser(user);
        lecturer.setCourse(course);
        lecturer.setHidden(false);
        lecturer.setLecturerId(idCount++);
        return lecturer;
    }


    /**
     * Creates a basic unconfirmed student with the given course and user.
     * @param course The course for which the user should be added
     *               as student. It must have at least one participation
     *               type.
     * @param user The user which should be added as student.
     * @return The student association object between the course and user.
     */
    public static Student createStudentWith(Course course, User user) {
        Student student = new Student();
        student.setStudentId(idCount++);
        student.setHidden(false);
        student.setUser(user);
        student.setDeleted(false);
        student.setTries(0);
        student.setHidden(false);
        student.setConfirmed(true);
        student.setCourse(course);
        student.setParticipationType(course.getDefaultParticipationType());

        course.getStudents().add(student);
        return student;
    }

    /**
     * Creates a basic privileged user with the given course and user.
     * The privielged user has no privileges and is not a secretary.
     * @param course The course for which the user should be added
     *               as privileged user.
     * @param user The user which should be added as privilege duser.
     * @return The privileged user object between the course and user.
     */
    public static PrivilegedUser createPrivUserWith(Course course, User user) {
        PrivilegedUser privilegedUser = new PrivilegedUser();
        privilegedUser.setUser(user);
        privilegedUser.setCourse(course);
        privilegedUser.setHidden(false);
        course.getTutors().add(privilegedUser);
        return privilegedUser;
    }

    /**
     * Creates a new basic participation type which is not the default part type.
     * @param course the course in which the participation type is in.
     * @return The created particpation type object.
     */
    public static ParticipationType createParticipationTypeIn(Course course) {
        ParticipationType type = new ParticipationType();
        type.setPartTypeId(getNewObjectId());
        type.setName("Participation Type" + type.getPartTypeId());
        type.setIsDefaultParttype(false);
        type.setCourse(course);
        GradeFormula formula = createGradeFormulaIn(type);
        course.getParticipationTypes().add(type);
        return type;
    }

    /**
     * Creates a basic grade formula in the participation type.
     * @param type The type in which the formula should be created in.
     * @return The created formula.
     */
    public static GradeFormula createGradeFormulaIn(ParticipationType type) {
        GradeFormula formula = new GradeFormula();
        formula.setValid(false);
        formula.setEditDescription("Basic Test Formula");
        formula.setFormula("def set_final_grade(grades):\n"
                + "    return PaboGrade.GRADE_5_0");
        formula.setParticipationType(type);
        type.addNewFormula(formula);
        return formula;
    }

    /**
     * Creates a basic pabo data with test names
     * @param student The student which should get valid pabo data
     * @return The student with valid pabo data.
     */
    public static Student basicValidPaboDataFor(Student student) {
        return basicValidPaboDataFor(student,"");
    }

    /**
     * Creates a basic pabo data with test names
     * @param student The student which should get valid pabo data
     * @param appendStr An String which get appended for test nams like TestExamName
     *                  for testing unique objects.
     * @return The student with valid pabo data.
     */
    public static Student basicValidPaboDataFor(Student student, String appendStr) {
        PaboData data = new PaboData();
        data.setAttempt(1);
        data.setMatriculation("123456");
        data.setValidationId(PaboData.VALID);
        data.setPaboFirstName("TestFirstName" + appendStr);
        data.setPaboLastName("TestLastName" + appendStr);
        data.setMajor("TestMajor" + appendStr);
        data.setExamName("TestExamName" + appendStr);
        student.setPaboData(data);
        return student;
    }


}
