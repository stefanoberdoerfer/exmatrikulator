package de.unibremen.opensores.util.csv;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.PaboData;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.testutil.DataHelper;
import de.unibremen.opensores.util.Constants;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This is a test class for the parser of pabo imports and exports.
 * @author Kevin Scheck
 */
public final class PaboParserTest {

    /**
     * The path to a shortened version of the provided pabo csv file.
     */
    private static final String SHORTENED_PROVIDED_PABO_CSV_PATH
            = "src/test/resources/csv/pabo/shortened_supplied_pabo_csv_file.csv";

    /**
     * The path to a shortened version of the provided pabo csv file.
     */
    private static final String CASCADE_UNVALID_FIELDS_PATH
            = "src/test/resources/csv/pabo/cascade_unvalid_types.csv";

    /**
     * The path to a shortened version of the provided pabo csv file.
     */
    private static final String UNVALID_FORMAT_CSV_PATH
            = "src/test/resources/csv/pabo/unvalid_format.csv";

    /**
     * The path to a shortened version of the provided pabo csv file.
     */
    private static final String NOT_A_CSV_PATH
            = "src/test/resources/csv/pabo/not_a_csv.txt";

    /**
     * The path which will point to an already existing file.
     */
    private static final String PATH_TEST_RES_CSV_PABO = "src/test/resources/csv/pabo/";

    /**
     * The filename of a file which got only the header parsed to.
     */
    private static final String JUST_HEADER_FILE_NAME = "just_header.csv";

    /**
     * The filename of a test file for parsing the one student with a numerical
     * pabo grade and a set exam date.
     */
    private static final String ONE_STUDENT_FILE_NAME = "download_one_student_with_exam_date.csv";

    /**
     * The filename of a test file for two different students with no exam date, and
     * cheated and neglected grades.
     */
    private static final String DIFFERENT_STUDENT_FILE_NAME = "download_dfiferent_students.csv";

    /**
     * The filename of an already existing file in path PATH_TEST_RES_CSV_PABO.
     */
    private static final String ALREADY_EXISTS_FILE_NAME = "unvalid_format.csv";

    /**
     * The header row text of a pabo csv file.
     */
    private static final String HEADER_ROW_PABO_CSV
            = "Matrnr;Nachname;Vorname;Prfdatum;Note;Prfbem;Versuch;Teilprf;Studiengang;;;;";

    /**
     * The shortened example pabo file supplied in the stud ip SWP2 folder.
     */
    private File shortenedProvidedPaboFile;

    /**
     * A File with the same contents as the supplied csv file, but it's a .txt file.
     */
    private File notACSVFile;

    /**
     * A File which has not a valid syntax (the number of columns of each row is
     * not as expected).
     */
    private File unvalidFormatFile;

    /**
     * The file which types are unvalid after each row.
     */
    private File cascadeUnvalidTypesFile;

    /**
     * A file which got created from parsing the student pabo data.
     */
    private File createdDownloadFile;

    /**
     * Loads all supplied Stud ip export csv files.
     */
    @Before
    public void loadFiles() {
        shortenedProvidedPaboFile = new File(SHORTENED_PROVIDED_PABO_CSV_PATH);
        notACSVFile = new File(NOT_A_CSV_PATH);
        unvalidFormatFile = new File(UNVALID_FORMAT_CSV_PATH);
        cascadeUnvalidTypesFile = new File(CASCADE_UNVALID_FIELDS_PATH);
    }

    /**
     * Tests if the contents of rows with data gets parsed with the shortened
     * provided pabo csv file.
     * @throws Exception If there is an IOException while parsing this file, not
     *         expected.
     */
    @Test
    public void testPaboUploadProvidedPaboFile() throws Exception{
        List<PaboData> paboDataList = PaboParser.parsePaboUpload(shortenedProvidedPaboFile);

        int validRows = 5; // The first 5 rows
        int remainingEmptyRows = 6; // Last 6 rows are empty like in the example file

        PaboData expectedPaboData123450 = new PaboData();
        expectedPaboData123450.setAttempt(2);
        expectedPaboData123450.setExamName("Modulprüfung");
        expectedPaboData123450.setPaboFirstName("Vorname1");
        expectedPaboData123450.setPaboLastName("Nachname1");
        expectedPaboData123450.setMajor("Bachelor Informatik");
        expectedPaboData123450.setMatriculation("123450");

        assertTrue(paboDataList.get(0).equalsContents(expectedPaboData123450));

        PaboData expectedPaboData123454 = new PaboData();
        expectedPaboData123454.setAttempt(3);
        expectedPaboData123454.setPaboFirstName("Vorname5");
        expectedPaboData123454.setPaboLastName("Nachname5");
        expectedPaboData123454.setExamName("Modulprüfung");
        expectedPaboData123454.setMajor("Bachelor Informatik");
        expectedPaboData123454.setMatriculation("123454");

        assertTrue(paboDataList.get(1).equalsContents(expectedPaboData123454));

        PaboData expectedPaboData123463 = new PaboData();
        expectedPaboData123463.setAttempt(2);
        expectedPaboData123463.setPaboFirstName("Vorname14");
        expectedPaboData123463.setPaboLastName("Nachname14");
        expectedPaboData123463.setExamName("INF-3 Softwareprojekt 1 inkl. Datenbankgrundlagen");
        expectedPaboData123463.setMajor("Bachelor Wirtschaftsinformatik");
        expectedPaboData123463.setMatriculation("123463");

        assertTrue(paboDataList.get(2).equalsContents(expectedPaboData123463));


        PaboData expectedPaboData123464 = new PaboData();
        expectedPaboData123464.setAttempt(2);
        expectedPaboData123464.setPaboFirstName("Vorname15");
        expectedPaboData123464.setPaboLastName("Nachname15");

        expectedPaboData123464.setExamName("INF-3 Softwareprojekt 1 inkl. Datenbankgrundlagen");
        expectedPaboData123464.setMajor("Bachelor Wirtschaftsinformatik");
        expectedPaboData123464.setMatriculation("123464");

        assertTrue(paboDataList.get(3).equalsContents(expectedPaboData123464));

        PaboData expectedPaboData123465 = new PaboData();
        expectedPaboData123465.setAttempt(2);
        expectedPaboData123465.setPaboFirstName("Vorname16");
        expectedPaboData123465.setPaboLastName("Nachname16");
        expectedPaboData123465.setExamName("Modulprüfung");
        expectedPaboData123465.setMajor("Bachelor Informatik");
        expectedPaboData123465.setMatriculation("123465");

        assertTrue(paboDataList.get(4).equalsContents(expectedPaboData123465));

        for (int i = validRows; i < validRows-1 + remainingEmptyRows; i++) {
            assertFalse(paboDataList.get(i).isValdid());
        }
        assertTrue(paboDataList.size() == validRows+remainingEmptyRows);
    }

    /**
     * Tests if the correct unvalid ids gets passed in the PaboData.
     * The fields get unvalid cascading, so that the first row has the first
     * parsed field unvalid, the second row has the first parsed field valid, but the
     * second field unvalid and so on.
     * @throws Exception Thrown by the parser, is unexpected.
     */
    @Test
    public void testPaboUploadCascadeUnvalidFields() throws Exception {
        List<PaboData> paboDataList = PaboParser.parsePaboUpload(cascadeUnvalidTypesFile);
        int expectedNumRows = 6;
        assertTrue(paboDataList.size() == expectedNumRows);

        PaboData data = paboDataList.get(0);
        assertFalse(data.isValdid());
        assertTrue(data.getValidationId() == PaboData.UNVALID_MATRICULATION);

        data = paboDataList.get(1);
        assertFalse(data.isValdid());
        assertTrue(data.getValidationId() == PaboData.UNVALID_ATTEMPT);

        data = paboDataList.get(2);
        assertFalse(data.isValdid());
        assertTrue(data.getValidationId() == PaboData.UNVALID_EXAM_NAME);

        data = paboDataList.get(3);
        assertFalse(data.isValdid());
        assertTrue(data.getValidationId() == PaboData.UNVALID_MAJOR);

        data = paboDataList.get(4);
        assertTrue(data.isValdid());
        assertEquals(data.getValidationId(), PaboData.VALID);
        assertEquals((int) data.getAttempt(), 1);
        assertEquals(data.getExamName(),"Testexam");
        assertEquals(data.getMajor() ,"TestMajor");

        data = paboDataList.get(5);
        assertFalse(data.isValdid());
        assertTrue(data.getValidationId() == PaboData.UNVALID_ALL_EMPTY);
    }

    /**
     * Tests if the parser throws an IOException if the file parameter is null.
     */
    @Test(expected = IOException.class)
    public void testPaboUploadNullParam() throws Exception {
        PaboParser.parsePaboUpload(null);
    }

    /**
     * Tests if the parser throws an IOException if the file parameter is not a
     * csv file.
     */
    @Test(expected = IOException.class)
    public void testPaboUploadNotACSVFile() throws Exception {
        PaboParser.parsePaboUpload(notACSVFile);
    }

    /**
     * Tests if the parser returns a PaboData object with an unvalid format id
     * as validation id
     * @see PaboData
     */
    @Test
    public void testPaboUploadUnvalidFormat() throws Exception {
        int numExpectedRows = 5;
        List<PaboData> dataList = PaboParser.parsePaboUpload(unvalidFormatFile);
        for (int i = 0; i < numExpectedRows; i++) {
            PaboData data = dataList.get(i);
            assertFalse(data.isValdid());
            assertTrue(data.getValidationId() == PaboData.UNVALID_ALL_EMPTY);
        }
    }

    /**
     * Tests if an IllegalArgumentException gets thrown if one of the parameters
     * is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParsePaboDownloadNullParamStudents() throws Exception{
        PaboParser.parsePaboDownload(null, new Date(), "","");
    }

    /**
     * Tests if an IllegalArgumentException gets thrown if the file name is null;
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParsePaboDownloadNullParamFileName() throws Exception {
        PaboParser.parsePaboDownload(new ArrayList<>(), new Date(),null, "");
    }

    /**
     * Tests if an IllegalArgumentException gets thrown the dir path is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParsePaboDownloadNullParamDirPath() throws Exception {
        PaboParser.parsePaboDownload(new ArrayList<>(), new Date(), "", null);

    }


    //TODO DELETE
    ///**
    // * Tests if an IOException gets thrown if the specified file already exists.
    // */
    //@Test(expected = IOException.class)
    //public void testParsePaboDownloaFileExists() throws Exception {
    //    PaboParser.parsePaboDownload(new ArrayList<>(), new Date(),
    //            ALREADY_EXISTS_FILE_NAME, PATH_TEST_RES_CSV_PABO);
    //}
//
    /**
     * Tests if just the header gets parsed if no students get supplied.
     */
    @Test
    public void testParsePaboDownloaFileNoStudents() throws Exception {
        File justHeaderFile = new File(PATH_TEST_RES_CSV_PABO,JUST_HEADER_FILE_NAME);
        if (justHeaderFile.exists()) {
            fail();
        }
        PaboParser.parsePaboDownload(new ArrayList<>(), new Date(),
                JUST_HEADER_FILE_NAME, PATH_TEST_RES_CSV_PABO);


        justHeaderFile = new File(PATH_TEST_RES_CSV_PABO,JUST_HEADER_FILE_NAME);
        if(!justHeaderFile.exists()) {
           fail();
        }
        List<String> lines = FileUtils.readLines(justHeaderFile);
        assertTrue(lines.size() == 1);
        assertEquals(lines.get(0), HEADER_ROW_PABO_CSV);
    }

    /**
     * Tests if one student gets passed correctly with a set exam date and
     * numerical pabo grade.
     */
    @Test
    public void testParsePaboDownloaFileSameStudent() throws Exception {
        File sameStudentFile = new File(PATH_TEST_RES_CSV_PABO, ONE_STUDENT_FILE_NAME);
        if (sameStudentFile.exists()) {
            fail();
        }

        User user = DataHelper.createBasicUser();
        Course course = DataHelper.createBasicCourse();
        Student student = DataHelper.createStudentWith(course, user);
        student = DataHelper.basicValidPaboDataFor(student);
        student.setPaboGrade(PaboGrade.GRADE_4_0.name());
        List<Student> studentList = new ArrayList<>();
        studentList.add(student);
        studentList.add(student);
        PaboParser.parsePaboDownload(studentList, getTestExamDate(),
                ONE_STUDENT_FILE_NAME, PATH_TEST_RES_CSV_PABO);


        sameStudentFile = new File(PATH_TEST_RES_CSV_PABO,ONE_STUDENT_FILE_NAME);
        if(!sameStudentFile.exists()) {
            fail();

        }
        final String STUDENT_ROW = "123456;TestLastName;TestFirstName;04.03.2016;"
                + "4;7;1;TestExamName;TestMajor;;;;";
        List<String> lines = FileUtils.readLines(sameStudentFile);
        assertTrue(lines.size() == 3);
        assertEquals(HEADER_ROW_PABO_CSV,lines.get(0));
        assertEquals(STUDENT_ROW, lines.get(1));
        assertEquals(STUDENT_ROW, lines.get(2));
    }


    /**
     * Tests if one student gets passed correctly no exam set and different students,
     * one who has cheated and one who was not there during the exam.
     */
    @Test
    public void testParsePaboDownloadDifferentStudentsNoExam() throws Exception {
        File differentStudentsFile = new File(PATH_TEST_RES_CSV_PABO, DIFFERENT_STUDENT_FILE_NAME);
        if (differentStudentsFile.exists()) {
            fail();
        }

        User user = DataHelper.createBasicUser();
        Course course = DataHelper.createBasicCourse();
        Student student = DataHelper.createStudentWith(course, user);
        student = DataHelper.basicValidPaboDataFor(student);
        student.setPaboGrade(PaboGrade.GRADE_CHEATED.name());

        Student student2 = DataHelper.basicValidPaboDataFor(
                DataHelper.createStudentWith(course, user), "Two");
        student2.setPaboGrade(PaboGrade.GRADE_NEGLECTED.name());
        student2.getPaboData().setAttempt(2);

        List<Student> studentList = new ArrayList<>();
        studentList.add(student);
        studentList.add(student2);
        PaboParser.parsePaboDownload(studentList, null,
                DIFFERENT_STUDENT_FILE_NAME, PATH_TEST_RES_CSV_PABO);


        differentStudentsFile = new File(PATH_TEST_RES_CSV_PABO,DIFFERENT_STUDENT_FILE_NAME);
        if(!differentStudentsFile.exists()) {
            fail();

        }
        final String STUDENT1_ROW = "123456;TestLastName;TestFirstName;;"
                + ";3;1;TestExamName;TestMajor;;;;";
        final String STUDENT2_ROW = "123456;TestLastNameTwo;TestFirstNameTwo;;"
                + ";2;2;TestExamNameTwo;TestMajorTwo;;;;";
        List<String> lines = FileUtils.readLines(differentStudentsFile);
        assertTrue(lines.size() == 3);
        assertEquals(HEADER_ROW_PABO_CSV,lines.get(0));
        assertEquals(STUDENT1_ROW, lines.get(1));
        assertEquals(STUDENT2_ROW, lines.get(2));
    }


    /**
     * Deletes the files after testing.
     */
    @After
    public void deleteCreatedFiles() {
        File justHeaderFile = new File(PATH_TEST_RES_CSV_PABO,JUST_HEADER_FILE_NAME);
        if (justHeaderFile.exists()) {
            justHeaderFile.delete();
        }

        File sameStudentFile = new File(PATH_TEST_RES_CSV_PABO, ONE_STUDENT_FILE_NAME);
        if (sameStudentFile.exists()) {
            sameStudentFile.delete();
        }
        File diffStudentFile = new File(PATH_TEST_RES_CSV_PABO, DIFFERENT_STUDENT_FILE_NAME);
        if (diffStudentFile.exists()) {
            diffStudentFile.delete();
        }
    }

    /**
     * Gets a specific test exam date.
     * @return The date objet of the specified date.
     */
    private Date getTestExamDate() {
        return new Calendar.Builder()
                .setTimeZone(TimeZone.getTimeZone(Constants.SYSTEM_TIMEZONE))
                .setDate(2016,02,04).build().getTime();
        // This is actually the 04.03.2016, thanks to the Java API March is
        // The 2th month in this method..
    }


}
