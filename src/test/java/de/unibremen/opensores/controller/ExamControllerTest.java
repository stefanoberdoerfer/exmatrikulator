package de.unibremen.opensores.controller;

import de.unibremen.opensores.controller.settings.ExamController;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.ExamService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.LogService;

import de.unibremen.opensores.testutil.ContextMocker;
import de.unibremen.opensores.testutil.DataHelper;
import de.unibremen.opensores.testutil.MockHelper;
import de.unibremen.opensores.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test class implements white box tests of the ExamController class.
 * It uses Mockito to mock the FacesContext and the service classes.
 * @author Kevin Scheck
 */
public class ExamControllerTest {

    /**
     * The examController under test.
     */
    private ExamController examController;

    /**
     * Mocked FacesContext for mocked resource bundles, http requests etc.
     */
    @Mock
    private FacesContext contextMock;

    /**
     * Mocked UI Component for ui validations methods.
     */
    @Mock
    private UIComponent uiComponentMock;

    /**
     * A mock of the course service which will return the course test object.
     */
    @Mock
    private CourseService courseServiceMock;

    /**
     * A mock of the grading service which will delete all gradings associated
     * with the exam.
     */
    @Mock
    private GradingService gradingServiceMock;

    /**
     * A mock of the exam service which is used for db transactions.
     */
    @Mock
    private ExamService examServiceMock;

    /**
     * A mock of the logService to persist exmatrikulator logs.
     */
    @Mock
    private LogService logServiceMock;

    /**
     * A map which gets used for the mockContext as session map.
     */
    private Map<String, Object> sessionMap;

    /**
     * The exam which is used to test the ExamController.
     */
    private Exam exam;

    /**
     * The course object with which the exam controller will be tested.
     */
    private Course course;

    /**
     * The logged in user in the test.
     */
    private User loggedIn;


    /**
     * Mocks the dependencies for the exam controller, and creates the test data.
     */
    @Before
    public void setUp() {
        sessionMap = new HashMap<>();

        course = DataHelper.createBasicCourse();

        loggedIn = DataHelper.createBasicGlobalLecturer();
        Lecturer lecturer = DataHelper.createLecturerWith(course, loggedIn);

        sessionMap = new HashMap<>();
        sessionMap.put(Constants.SESSION_MAP_KEY_USER, loggedIn);

        exam = new Exam();
        exam.setExamId((long)1);
        exam.setName("Test Exam");
        exam.setShortcut("TE");
        exam.setCourse(course);
        exam.setGradeType(GradeType.Percent.getId());
        exam.setDeadline(new Date());
        course.getExams().add(exam);
        course.setCourseId(DataHelper.getNewObjectId());

        examController = new ExamController();

        // Mocking dependencies
        courseServiceMock = MockHelper.mockCourseService(course);
        examController.setCourseService(courseServiceMock);

        examServiceMock = Mockito.mock(ExamService.class);
        examController.setExamService(examServiceMock);

        gradingServiceMock = Mockito.mock(GradingService.class);
        when(gradingServiceMock.deleteAllGradingsFromExam(exam)).thenReturn(0);
        examController.setGradingService(gradingServiceMock);

        logServiceMock = Mockito.mock(LogService.class);
        examController.setLogService(logServiceMock);

        // Mocking the FacesContext and related classes
        contextMock = MockHelper.addExternalContextMock(ContextMocker.mockBasicFacesContext(), sessionMap);
        contextMock = MockHelper.addViewRootMock(contextMock);

        uiComponentMock = Mockito.mock(UIComponent.class);

        examController.init();
    }

    /**
     * Tests if the mocked dependencies got injected properly, so that that the
     * test course gets returned by the mocks.
     */
    @Test
    public void testInit() {
        assertSame(course, examController.getCourse());
    }

    /**
     * Tests if a new Exam is created when onAddExamDialogCalled is called.
     */
    @Test
    public void testOnAddExamDialogCalled() {
        examController.onAddExamDialogCalled();
        assertTrue(examController.getSelectedExam().getName().isEmpty());
        // An empty course gets set for an exam because of JPA conflicts
        assertNull(examController.getSelectedExam().getCourse().getCourseId());
        assertNullOrEmpty(examController.getSelectedExam().getName());
        assertNullOrEmpty(examController.getSelectedExam().getShortcut());
        assertNull(examController.getSelectedExam().getDeadline());
    }

    /**
     * Tests if the passed exam in the testOnEditExamDialogCalled is the
     * selected exam.
     */
    @Test
    public void testOnEditExamDialogCalled() {
        examController.onEditExamDialogCalled(course.getExams().get(0));
        assertSame(examController.getSelectedExam(), course.getExams().get(0));
    }

    /**
     * Tests if an IllegalArgumentException gets thrown if null is passed in the
     * method onEditExamDialogCalled.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnEditExamDialogCalledNullParam() {
        examController.onEditExamDialogCalled(null);
    }

    /**
     * Tests if the passed exam in the testOnEditExamDialogCalled is the
     * selected exam.
     */
    @Test
    public void testOnDeleteExamDialogCalled() {
        examController.onDeleteExamDialogCalled(course.getExams().get(0));
        assertSame(examController.getSelectedExam(), course.getExams().get(0));
        assertTrue(examController.getExamNameDeletionTextInput().isEmpty());
    }

    /**
     * Tests if an IllegalArgumentException gets thrown if null is passed in the
     * method onDeleteExamDialogCalled.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnDeleteExamDialogCalledNullParam() {
        examController.onDeleteExamDialogCalled(null);
    }


    /**
     * Tests if add exam adds a new exam to the course when addExam() is called.
     */
    @Test
    public void testAddExam() {
        Exam toBeAdded = new Exam();
        assertFalse(course.getExams().contains(toBeAdded));
        toBeAdded.setWithAttendance(false);
        toBeAdded.setDeadline(new Date());
        toBeAdded.setMaxPoints(new BigDecimal("1"));
        toBeAdded.setGradeType(GradeType.Percent.getId());
        examController.setSelectedExam(toBeAdded);
        examController.addExam();
        assertSame(course, toBeAdded.getCourse());
        assertTrue(course.getExams().contains(toBeAdded));
        assertNull(toBeAdded.getMaxPoints());
        assertNull(toBeAdded.getDeadline());


        Exam uploadExam = new Exam();
        assertFalse(course.getExams().contains(uploadExam));
        examController.setSelectedExam(uploadExam);
        uploadExam.setGradeType(GradeType.Point.getId());
        uploadExam.setMaxPoints(new BigDecimal("1"));
        uploadExam.setWithAttendance(true);
        uploadExam.setDeadline(new Date());

        examController.addExam();
        assertNotNull(uploadExam.getDeadline());
        assertTrue(course.getExams().contains(uploadExam));
    }


    /**
     * Tests if the edit exam method correctly deletes gradings if the exam
     * grade type changes.
     */
    @Test
    public void testEditExam() {
        assertTrue(course.getExams().contains(exam));
        exam.setGradeType(GradeType.Percent.getId());
        examController.setSelectedExam(exam);
        exam.setGradeType(GradeType.Boolean.getId());
        examController.editExam();
        verify(gradingServiceMock, times(1)).deleteAllGradingsFromExam(exam);
        assertTrue(course.getExams().contains(exam));
    }

    /**
     * Tests if the exam gets deleted from the course.
     */
    @Test
    public void testDeleteExam() {
        examController.onDeleteExamDialogCalled(exam);
        examController.deleteExam();
        assertFalse(examController.getCourse().getExams().contains(exam));
        verify(gradingServiceMock, times(1)).deleteAllGradingsFromExam(exam);
    }


    /**
     * Tests if the method for deadline validation throws an validation exception
     * if the shortcut for a new course is already in the course.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateShortCutIsInCourseOnAdd() {
        examController.onAddExamDialogCalled();
        String existingShortcut = course.getExams().get(0).getShortcut();
        examController.validateShortCut(contextMock, uiComponentMock, existingShortcut);
    }

    /**
     * Tests if the method for deadline validation passes the validation because
     * the exam which gets edited is already in the course
     */
    @Test
    public void testValidateShortCutIsInCourseOnEdit() {
        examController.onEditExamDialogCalled(exam);
        String existingShortcut = exam.getShortcut();
        examController.validateShortCut(contextMock, uiComponentMock, existingShortcut);
    }

    /**
     * Tests if the method for shortcut validation throws an validation exception
     * if the shortcut is null
     */
    @Test(expected = ValidatorException.class)
    public void testValidateShortCutIsNull() {
        examController.validateShortCut(contextMock, uiComponentMock, null);
    }

    /**
     * Tests if the method for shortcut validation throws a ValidatorException
     * if the shortcut is an empty string.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateShortcutEmptyString() {
        examController.validateShortCut(contextMock, uiComponentMock, "");
    }

    /**
     * Tests if the method for shortcut validation throws a ValidatorException
     * if the shortcut contains digits which are not digits nor letters
     */
    @Test(expected = ValidatorException.class)
    public void testValidateShortcutNotDigitNorLetters() {
        examController.validateShortCut(contextMock, uiComponentMock, "uebung-1");
    }

    /**
     * Tests if the method for shortcut validation with a correct shortcuts.
     */
    @Test
    public void testValidateShortcut() {
        examController.validateShortCut(contextMock, uiComponentMock, "klausur");
        examController.validateShortCut(contextMock, uiComponentMock, "uebung1");
        examController.validateShortCut(contextMock, uiComponentMock, "12");
        examController.validateShortCut(contextMock, uiComponentMock, "muendlichePruefung");
    }


    /**
     * Tests if the validation method for the maximal points of an exam
     * throws a ValidatorException if the points are not positive.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateExamMaxPointsNotPositive() {
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock, new BigDecimal("0"));
    }

    /**
     * Tests if the validation method for the maximal points of an exam
     * throws a ValidatorException if the points are null.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateExamMaxPointsNullParam() {
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock, null);
    }

    /**
     * Tests if the validation method for the maximal points with valid points.
     */
    @Test
    public void testValidateExamMaxPoints() {
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock, new BigDecimal("2.2"));
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock, new BigDecimal("0.2123123112313"));
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock,
                new BigDecimal("33333333331213128312831823812381283128328"
                        + ".2213123172312321123121232189"));
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock,
                new BigDecimal("0.00000000000000000000000000002"));
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock, new BigDecimal("201231283281.2"));
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock, new BigDecimal("10"));
        examController.validateExamMaxPoints(contextMock,
                uiComponentMock, new BigDecimal("1"));
    }

    /**
     * Checks if the exam deletion name gets correctly validated if the
     * user input is the same as the selected exam of the course.
     */
    @Test
    public void testValidateExamDeletionName() {
        examController.setSelectedExam(exam);
        examController.validateExamDeletionName(contextMock,
                uiComponentMock, exam.getName());
    }

    /**
     * Checks if the exam deletion name validation throws an ValidatorException
     * if the checked string input is not the same as the name of the exam.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateExamDeletionNameNotSameName() {
        examController.setSelectedExam(exam);
        examController.validateExamDeletionName(contextMock,
                uiComponentMock, exam.getName()+"NotTheSame");
    }

    /**
     * Checks if the exam deletion name validation throws an ValidatorException
     * if the checked string input is null.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateExamDeletionNameNullParam() {
        examController.validateExamDeletionName(contextMock,
                uiComponentMock, null);
    }

    /**
     * Tests if the file ending validation passes correct file endings inputs,
     * consisting of file endings separated by a comma, like "zip,pdf,tex,mp3,mp4".
     */
    @Test
    public void testValidateFileEndings() {
        // Not selecting a file ending is valid in front end logic
        examController.validateFileEndings(contextMock, uiComponentMock,null);
        examController.validateFileEndings(contextMock, uiComponentMock,"");

        examController.validateFileEndings(contextMock, uiComponentMock,"zip");
        examController.validateFileEndings(contextMock, uiComponentMock,"zip,pdf,mp3,mp4,csv,001");
        examController.validateFileEndings(contextMock, uiComponentMock,"zip,pdf,mp3,mp4,csv,001,webarchive");
        examController.validateFileEndings(contextMock, uiComponentMock,"7z.005,021,tar.xz,02_");
    }

    /**
     * Tests if the file ending validation throws an ValidatorException if
     * the file endings are not separated by a comma.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateFileEndingsNotSeperatedByComma() {
        examController.validateFileEndings(contextMock, uiComponentMock,"zip,pdf,tex csv");
    }

    /**
     * Tests if the file ending validation throws an ValidatorException if
     * the file endings have not valid chars.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateFileEndingUnvalidChars() {
        examController.validateFileEndings(contextMock, uiComponentMock,"@@x€");
    }

    /**
     * Checks if a string is null or empty.
     * @param actualString The string to be checked.
     */
    public void assertNullOrEmpty(String actualString) {
        assertTrue(actualString == null || actualString.isEmpty());
    }

    /**
     * Checks if a collection is null or empty.
     * @param collection The collection to be checked.
     */
    public void assertNullOrEmpty(Collection<?> collection) {
        assertTrue(collection == null || collection.isEmpty());
    }


    /**
     * Releases the mocked context after every test.
     */
    @After
    public void releaseContext() {
        if (contextMock != null) {
            contextMock.release();
        }
    }

}
