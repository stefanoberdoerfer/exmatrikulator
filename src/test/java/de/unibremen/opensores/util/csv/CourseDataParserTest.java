package de.unibremen.opensores.util.csv;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.testutil.DataHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.python.bouncycastle.asn1.dvcs.Data;
import org.python.core.util.FileUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the CourseDataParser for parsing the data of the course to a csv file.
 * @author Kevin Scheck
 */
public class CourseDataParserTest {


    /**
     * The path which points to the directory in which the test files should be located.
     */
    private static final String PATH_TEST_RES_CSV_COURSE_DATA = "src/test/resources/csv/course_data/";


    /**
     * Tests if the lecturer can view all fields and the fields are valid.
     */
    private static final String FILE_NAME_TEST_LECTURER = "test_lecturer.csv";

    /**
     * Test file to check if the lecturer can view all fields and the fields are valid.
     */
    private static final String FILE_NAME_TEST_LECTURER_NULL_VALS = "test_lecturer_null_vals.csv";


    /**
     * Tests file to check a priv user cant view set fields which are not associated with him
     */
    private static final String FILE_NAME_TEST_PRIV_USER = "test_priv_user_hidden.csv";

    /**
     * Tests if an IllegalArgumentException is thrown if is the course null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullParamCourse() throws Exception {
        CourseDataParser.parseCourseToCSV(null, true, new PrivilegedUser(),
                "PATH_TEST_RES_CSV_COURSE_DATA","test.csv");
    }

    /**
     * Tests if an IllegalArgumentException is thrown if the file name is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullParamFileName() throws Exception {
        CourseDataParser.parseCourseToCSV(new Course(), true, new PrivilegedUser(),
                "PATH_TEST_RES_CSV_COURSE_DATA",null);
    }

    /**
     * Tests if an IllegalArgumentException is thrown if the path is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullParamDirPath() throws Exception {
        CourseDataParser.parseCourseToCSV(new Course(), true, new PrivilegedUser(),
                null,"test");
    }

    /**
     * Tests if an IllegalArgumentException is thrown if the user is not a lecturer
     * and the priv user is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullParamPrivUser() throws Exception {
        CourseDataParser.parseCourseToCSV(null, true, new PrivilegedUser(),
                "PATH_TEST_RES_CSV_COURSE_DATA","test.csv");
    }

    /**
     * Tests if the lecturer can see all fields and the headers and values of
     * the fields are correctly displayed.
     */
    @Test
    public void testLecturerExample() throws Exception {
        User user = DataHelper.createBasicUser();
        user.setMatriculationNumber("123456");
        User lecturerUser = DataHelper.createBasicUserWithNewId();
        Course course = DataHelper.createBasicCourse();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);
        Student student = DataHelper.createStudentWith(course, user);

        student.setPaboGrade(PaboGrade.GRADE_1_3.name());
        Tutorial tutorial = new Tutorial();
        tutorial.setName("TestTutorial...;;");
        student.setTutorial(tutorial);

        Exam pointExam = new Exam();
        pointExam.setName("--TestPointExam--");
        pointExam.setGradeType(GradeType.Point.getId());
        pointExam.setMaxPoints(new BigDecimal("23.1"));

        Group group = new Group();
        group.setName(",,,TestGroup");
        student.setGroup(group);
        Exam booleanExam = new Exam();
        booleanExam.setName("--TestBooleanExam--");
        booleanExam.setGradeType(GradeType.Boolean.getId());

        Grading pointGrading = new Grading();
        pointGrading.setStudent(student);
        pointGrading.setCorrector(lecturerUser);
        pointGrading.setExam(pointExam);
        Grade grade = new Grade();
        grade.setValue(new BigDecimal("10.25"));
        pointGrading.setGrade(grade);
        student.getGradings().add(pointGrading);

        Grading booleanGrading = new Grading();
        booleanGrading.setStudent(student);
        booleanGrading.setExam(booleanExam);
        booleanGrading.setCorrector(lecturerUser);
        Grade boolGrade = new Grade();
        boolGrade.setValue(new BigDecimal("1"));
        booleanGrading.setGrade(boolGrade);
        student.getGradings().add(booleanGrading);

        course.getExams().add(pointExam);
        course.getExams().add(booleanExam);
        course.getStudents().add(student); //Two times the same students to test loop

        System.out.println("Students of course size " + course.getStudents());
        File lecturerExample = CourseDataParser.parseCourseToCSV(course,
                true, null, PATH_TEST_RES_CSV_COURSE_DATA,FILE_NAME_TEST_LECTURER);
        List<String> lines = FileUtils.readLines(lecturerExample);
        assertTrue(lines.size() == 3);
        assertEquals("matr;tutorial;group;participation_type;final_grade;TestPointExam(Point max: 23.1);TestBooleanExam(Boolean)", lines.get(0));
        assertEquals("123456;TestTutorial;TestGroup;Defaultparticipationtype;1,3;10.25;1", lines.get(1));
        assertEquals("123456;TestTutorial;TestGroup;Defaultparticipationtype;1,3;10.25;1", lines.get(2));
    }


    /**
     * Tests if the lecturer can see all fields and the headers and values of
     * the fields are correctly displayed.
     */
    @Test
    public void testLecturerWithNullValues() throws Exception {
        User user = DataHelper.createBasicUser();
        user.setMatriculationNumber(null);
        User lecturerUser = DataHelper.createBasicUserWithNewId();
        Course course = DataHelper.createBasicCourse();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);
        Student student = DataHelper.createStudentWith(course, user);

        student.setPaboGrade(null);
        student.setTutorial(null);
        student.setGroup(null);
        student.setParticipationType(null);

        Exam pointExam = new Exam();
        pointExam.setName("--TestPointExam--");
        pointExam.setGradeType(GradeType.Point.getId());
        pointExam.setMaxPoints(new BigDecimal("23.1"));

        Exam booleanExam = new Exam();
        booleanExam.setName("--TestBooleanExam--");
        booleanExam.setGradeType(GradeType.Boolean.getId());

        course.getExams().add(pointExam);
        course.getExams().add(booleanExam);
        course.getStudents().add(student); //Two times the same students to test loop

        System.out.println("Students of course size " + course.getStudents());
        File lecturerExample = CourseDataParser.parseCourseToCSV(course,
                true, null, PATH_TEST_RES_CSV_COURSE_DATA,FILE_NAME_TEST_LECTURER_NULL_VALS);
        List<String> lines = FileUtils.readLines(lecturerExample);
    }

    /**
     * Tests if set fields are displayed as hidden if the user is not a lecturer
     * and is not associated with the fields.
     */
    @Test
    public void testPrivilegedUserWithNoRights() throws Exception {
        User user = DataHelper.createBasicUser();
        user.setMatriculationNumber("111111");
        User lecturerUser = DataHelper.createBasicUserWithNewId();
        Course course = DataHelper.createBasicCourse();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);
        Student student = DataHelper.createStudentWith(course, user);

        User anotherUser = DataHelper.createBasicUserWithNewId();

        PrivilegedUser lecturerPrivUser = DataHelper.createPrivUserWith(course, lecturerUser);
        PrivilegedUser otherPrivUser = DataHelper.createPrivUserWith(course, anotherUser);

        student.setPaboGrade(PaboGrade.GRADE_1_3.name());
        Tutorial tutorial = new Tutorial();
        tutorial.setName("TestTutorial...;;");
        tutorial.getTutors().add(lecturerPrivUser);
        student.setTutorial(tutorial);

        Exam numericExam = new Exam();
        numericExam.setName("--TestNumeric Grade Exam--");
        numericExam.setGradeType(GradeType.Numeric.getId());

        Group group = new Group();
        group.setName(",,,TestGroup");
        student.setGroup(group);
        group.setTutorial(tutorial);
        Exam booleanExam = new Exam();
        booleanExam.setName("--Prozentuebungsblatt--");
        booleanExam.setGradeType(GradeType.Percent.getId());

        Grading numericGrading = new Grading();
        numericGrading.setStudent(student);
        numericGrading.setCorrector(lecturerUser);
        numericGrading.setExam(numericExam);
        Grade grade = new Grade();
        grade.setValue(new BigDecimal("10.25"));
        numericGrading.setGrade(grade);
        student.getGradings().add(numericGrading);

        Grading percentGrading = new Grading();
        percentGrading.setStudent(student);
        percentGrading.setExam(booleanExam);
        percentGrading.setCorrector(lecturerUser);
        Grade boolGrade = new Grade();
        boolGrade.setValue(new BigDecimal("12"));
        percentGrading.setGrade(boolGrade);
        student.getGradings().add(percentGrading);

        course.getExams().add(numericExam);
        course.getExams().add(booleanExam);

        System.out.println("Students of course size " + course.getStudents());
        File lecturerExample = CourseDataParser.parseCourseToCSV(course,
                false, otherPrivUser, PATH_TEST_RES_CSV_COURSE_DATA,FILE_NAME_TEST_PRIV_USER);
        List<String> lines = FileUtils.readLines(lecturerExample);
        assertTrue(lines.size() == 2);
        assertEquals("matr;tutorial;group;participation_type;final_grade;TestNumericGradeExam(Numeric);Prozentuebungsblatt(Percent)", lines.get(0));
        assertEquals("111111;?;?;Defaultparticipationtype;?;?;?", lines.get(1));

    }
}
