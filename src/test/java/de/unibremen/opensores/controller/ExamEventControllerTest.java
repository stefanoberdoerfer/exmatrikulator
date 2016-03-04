package de.unibremen.opensores.controller;

import de.unibremen.opensores.controller.settings.ExamEventController;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.ExamEvent;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.ExamEventService;
import de.unibremen.opensores.service.ExamService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.TutorialService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.testutil.ContextMocker;
import de.unibremen.opensores.testutil.DataHelper;
import de.unibremen.opensores.testutil.MockHelper;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.DateUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is a test class for the controller ExamEventController
 * which manages the ExamEvents of one exam of a course.
 * @author Kevin Scheck
 */
public class ExamEventControllerTest {

    /**
     * A mock of the LogService.
     */
    @Mock
    private LogService logServiceMock;

    /**
     * A mock of the courseService.
     */
    @Mock
    private CourseService courseServiceMock;

    /**
     * A mock of the userService.
     */
    @Mock
    private UserService userServiceMock;

    /**
     * A mock of the ExamService.
     */
    @Mock
    private ExamService examServiceMock;

    /**
     * A mock of the ExamEventService.
     */
    @Mock
    private ExamEventService examEventServiceMock;

    /**
     * A mock of the StudentService..
     */
    @Mock
    private StudentService studentServiceMock;

    /**
     * A mock of the PrivilegedUserService
     */
    @Mock
    private PrivilegedUserService privilegedUserService;

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
     * The logged in test user in this test.
     */
    private User user;

    /**
     * The test course used in this test.
     */
    private Course course;

    /**
     * The test exam used in this test.
     */
    private Exam exam;

    /**
     * The test session map used in this test.
     */
    private Map<String, Object> sessionMap;

    /**
     * The ExamEventController under test.
     */
    private ExamEventController examEventController;

    /**
     * The priv user creating events.
     */
    private PrivilegedUser privilegedUser;

    /**
     * The ExamEvent used in tests.
     */
    private ExamEvent event;

    /**
     * Sets up all test classes in mocks.
     * Injects the mocks in the TutorialEventController.
     */
    @Before
    public void init() {
        course = DataHelper.createBasicCourse();

        user = DataHelper.createBasicGlobalLecturer();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);
        privilegedUser = DataHelper.createPrivUserWith(course, user);

        sessionMap = new HashMap<>();
        sessionMap.put(Constants.SESSION_MAP_KEY_USER, user);

        exam = new Exam();
        exam.setExamId(DataHelper.EXAM_ID);
        exam.setExamId((long)1);
        exam.setName("Test Exam");
        exam.setShortcut("TE");
        exam.setCourse(course);
        exam.setGradeType(GradeType.Percent.getId());
        exam.setDeadline(new Date());
        exam.setWithAttendance(true);
        exam.setDeadline(DateUtil.tenYearsLater());
        course.getExams().add(exam);

        event = new ExamEvent();

        examEventController = new ExamEventController();

        // Mocking dependencies
        courseServiceMock = MockHelper.mockCourseService(course);
        examEventController.setCourseService(courseServiceMock);

        examServiceMock = Mockito.mock(ExamService.class);
        when(examServiceMock.findExamById(DataHelper.EXAM_ID_STR))
                .thenReturn(exam);
        when(examServiceMock.update(exam)).thenReturn(exam);
        examEventController.setExamService(examServiceMock);

        logServiceMock = Mockito.mock(LogService.class);
        examEventController.setLogService(logServiceMock);

        studentServiceMock = MockHelper.mockStudentService();
        examEventController.setStudentService(studentServiceMock);

        examEventServiceMock = Mockito.mock(ExamEventService.class);
        when(examEventServiceMock.persist(event)).thenReturn(event);
        when(examEventServiceMock.update(event)).thenReturn(event);
        examEventController.setExamEventService(examEventServiceMock);

        privilegedUserService = Mockito.mock(PrivilegedUserService.class);
        when(privilegedUserService.update(privilegedUser)).thenReturn(privilegedUser);
        examEventController.setPrivilegedUserService(privilegedUserService);

        userServiceMock = MockHelper.mockUserService();
        when(userServiceMock.hasCourseRole(user, Role.LECTURER, course))
                .thenReturn(true);
        examEventController.setUserService(userServiceMock);

        contextMock = ContextMocker.mockBasicFacesContext();
        contextMock = MockHelper.addViewRootMock(contextMock);
        contextMock = MockHelper.addExternalContextMock(contextMock, sessionMap);
        uiComponentMock = Mockito.mock(UIComponent.class);

        examEventController.init();
    }

    /**
     * Tests if all mocks got injected properly, the test data for the user,
     * course and exam got set properly in the controller.
     * Tests if the Controller has zero events in its event modal at the beginning.
     * Tests if the selected exam event is null when the controller is initialized.
     * Tests if the deadline is not due, because the deadline got set to the
     * current date plus 10 years.
     * Tests if the student is not registered to the exam, because the logged
     * in user is not a student.
     */
    @Test
    public void testInit() {
        assertSame(course, examEventController.getCourse());
        assertSame(exam, examEventController.getExam());
        assertTrue(examEventController.getExamEventModel().getEvents().isEmpty());
        assertTrue(examEventController.isUserLecturer());
        assertFalse(examEventController.isDeadlineDue());
        assertNull(examEventController.getEvent());
        assertFalse(examEventController.isStudentRegisteredToExam());
    }

    /**
     * Tests if the selected event in the SelectEvents parameter gets set as selected
     * event in the controller.
     * Tests if the course is newly fetched if the event is editable by the user.
     */
    @Test
    public void testOnEventSelect() {
        ExamEvent event = new ExamEvent();
        event.setStartDate(new Date());
        event.setEndDate(new Date());
        examEventController.setExamEvent(event);
        SelectEvent selectEvent = Mockito.mock(SelectEvent.class);
        when (selectEvent.getObject()).thenReturn(event);
        examEventController.onEventSelect(selectEvent);
        assertSame(event, examEventController.getEvent());
        //First time in the init() method
        verify(courseServiceMock,times(2)).findCourseById(DataHelper.COURSE_ID_STR);
    }

    /**
     * Test if a new ExamEvent gets created with the selected date. The new
     * exam event must have the same start and end date as the selected date.
     * Checks if the course gets newly feteched.
     */
    @Test
    public void testOnDateSelect() {
        Date date = new Date();
        SelectEvent selectEvent = Mockito.mock(SelectEvent.class);
        when (selectEvent.getObject()).thenReturn(date);
        examEventController.onDateSelect(selectEvent);
        assertEquals(date, examEventController.getEvent().getStartDate());
        assertEquals(date, examEventController.getEvent().getEndDate());
        //First time in the init() method
        verify(courseServiceMock,times(2)).findCourseById(DataHelper.COURSE_ID_STR);
    }

    /**
     * Tests if events with no PrimeFaces Ids get added to the ExamEventModel.
     * Tests if events with PrimeFaces Ids get updated in the ExamEventModel.
     * Tests if the exam gets updated its events from the ExamEventModel.
     */
    @Test
    public void testAddEvent() {
        Assert.assertTrue(examEventController.getExamEventModel().getEvents().isEmpty());
        event.setStartDate(new Date());
        event.setEndDate(new Date());
        event.setExaminer(privilegedUser);
        SelectEvent selectEvent = Mockito.mock(SelectEvent.class);
        when (selectEvent.getObject()).thenReturn(event);
        examEventController.onEventSelect(selectEvent);
        examEventController.addEvent(null);
        Assert.assertSame(event, examEventController.getExamEventModel().getEvents().get(0));
        verify(examServiceMock, times(1)).update(exam);
        verify(examEventServiceMock,times(1)).persist(event);

        examEventController.getExamEventModel().getEvents().clear();
        event.setId("1");
        examEventController.onEventSelect(selectEvent);
        event.setStartDate(new Date());
        event.setEndDate(new Date());
        examEventController.setExamEvent(event);
        examEventController.addEvent(null);

        verify(examServiceMock, times(2)).update(exam);
        verify(examEventServiceMock,times(1)).persist(event);
        Assert.assertTrue(examEventController.getExamEventModel().getEvents().isEmpty());

    }

    /**
     * Tests if the event is removed from the event model, the exam is updated
     * from the event model, the examiner doesnht have the event saved in his
     * events any more.
     */
    @Test
    public void testRemoveEvent() {
        Assert.assertTrue(examEventController.getExamEventModel().getEvents().isEmpty());
        event.setStartDate(new Date());
        event.setEndDate(new Date());
        event.setExaminer(privilegedUser);
        privilegedUser.getExamEvents().add(event);
        SelectEvent selectEvent = Mockito.mock(SelectEvent.class);
        when (selectEvent.getObject()).thenReturn(event);
        examEventController.onEventSelect(selectEvent);
        examEventController.addEvent(null);
        Assert.assertSame(event, examEventController.getExamEventModel().getEvents().get(0));
        verify(examServiceMock, times(1)).update(exam);
        verify(examEventServiceMock,times(1)).persist(event);

        examEventController.onEventSelect(selectEvent);
        examEventController.removeEvent(null);
        Assert.assertTrue(examEventController.getExamEventModel().getEvents().isEmpty());
        assertFalse(privilegedUser.getExamEvents().contains(event));
        verify(examServiceMock, times(2)).update(exam);
        verify(privilegedUserService, times(1)).update(privilegedUser);
    }
    /**
     * Tests if the course gets updated by calling this method.
     */
    @Test
    public void testUpdateDeadline() {
        examEventController.updateDeadLine();
        verify(examServiceMock,times(1)).update(exam);
    }

    /**
     * Tests if no ValidatorException gets thrown if the student number
     * of the picked students is equal to the set student size of the event.
     */
    @Test
    public void testValidateStudentSizeSameSize() {
        int testNumStudents = 5;
        ExamEvent event = new ExamEvent();
        examEventController.setExamEvent(event);

        event.setMaxNumStudents(testNumStudents);
        List<Object> arrayListMock = Mockito.mock(ArrayList.class);
        when(arrayListMock.size()).thenReturn(testNumStudents);
        DualListModel<Object> dualListModelMock = Mockito.mock(DualListModel.class);
        when(dualListModelMock.getTarget()).thenReturn(arrayListMock);

        examEventController.validateStudentSize(contextMock,uiComponentMock,dualListModelMock);
    }

    /**
     * Tests if a ValidatorException gets thrown if the number of picked students
     * for the event is larger than the maximal size of students of the event
     * by one..
     */
    @Test(expected = ValidatorException.class)
    public void testValidateStudentSizeOneToLargeSize() {
        int testNumStudents = 5;
        ExamEvent event = new ExamEvent();
        examEventController.setExamEvent(event);

        event.setMaxNumStudents(testNumStudents);
        List<Object> arrayListMock = Mockito.mock(ArrayList.class);
        when(arrayListMock.size()).thenReturn(testNumStudents + 1);
        DualListModel<Object> dualListModelMock = Mockito.mock(DualListModel.class);
        when(dualListModelMock.getTarget()).thenReturn(arrayListMock);

        examEventController.validateStudentSize(contextMock,uiComponentMock,dualListModelMock);
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
