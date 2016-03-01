package de.unibremen.opensores.controller;

import de.unibremen.opensores.controller.settings.GradeScriptController;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeFormulaService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.testutil.ContextMocker;
import de.unibremen.opensores.testutil.DataHelper;
import de.unibremen.opensores.testutil.MockHelper;
import de.unibremen.opensores.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is test class for the GradeScript controller.
 * @author Kevin Scheck
 */
public class GradeScriptControllerTest {

    /**
     * The GradeScriptController under test.
     */
    private GradeScriptController scriptController;

    /**
     * A mock of the course service mock.
     */
    @Mock
    private CourseService courseServiceMock;

    /**
     * A mock of the log service.
     */
    @Mock
    private LogService logServiceMock;

    /**
     * A mock of the student service.
     */
    @Mock
    private StudentService studentServiceMock;

    /**
     * A mock of the grade formula service class.
     */
    @Mock
    private GradeFormulaService gradeFormulaServiceMock;

    /**
     * A mock of the python interpreter.
     */
    @Mock
    private PythonInterpreter pyInterpreterMock;

    /**
     * A mock of the FacesContext.
     */
    @Mock
    private FacesContext contextMock;

    /**
     * A mock of an UI Component.
     */
    @Mock
    private UIComponent uiComponentMock;

    /**
     * The session map which will be used in the mocked FacesContext.
     */
    private Map<String, Object> sessionMap;

    /**
     * The course which will be used as test object to edit grade scripts.
     */
    private Course course;

    /**
     * The logged in user which edits grades; is a lecturer in the course.
     */
    private User user;

    /**
     * The default participation type used for the tests.
     */
    private ParticipationType defaultPartType;

    /**
     * Another participation type used in this test testing switching
     * between two participation types.
     */
    private ParticipationType otherPartType;

    @Before
    public void setUp() {
        course = DataHelper.createBasicCourse();
        defaultPartType = course.getDefaultParticipationType();
        otherPartType = DataHelper.createParticipationTypeIn(course);
        assertTrue(course.getParticipationTypes().size() == 2);

        user = DataHelper.createBasicGlobalLecturer();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);

        sessionMap = new HashMap<>();
        sessionMap.put(Constants.SESSION_MAP_KEY_USER, user);

        scriptController = new GradeScriptController();
        courseServiceMock = MockHelper.mockCourseService(course);
        scriptController.setCourseService(courseServiceMock);

        studentServiceMock = MockHelper.mockStudentService();
        when(studentServiceMock.getUndeletedAndConfirmedStudentsOf
                (course.getParticipationTypes()))
                //TODO
                .thenReturn(new HashMap<>());
        scriptController.setStudentService(studentServiceMock);

        logServiceMock = MockHelper.mockLogService();
        scriptController.setLogService(logServiceMock);

        gradeFormulaServiceMock = MockHelper.mockGradeFormulaService();
        scriptController.setGradeFormulaService(gradeFormulaServiceMock);

        pyInterpreterMock = mock(PythonInterpreter.class);
        doAnswer(invocationOnMock -> null).when(pyInterpreterMock).close();
        doAnswer(invocationOnMock -> null).when(pyInterpreterMock).cleanup();
        when(pyInterpreterMock.getSystemState()).thenReturn(new PySystemState());
        scriptController.setPythonInterpreter(pyInterpreterMock);

        contextMock = ContextMocker.mockBasicFacesContext();
        contextMock = MockHelper.addExternalContextMock(contextMock, sessionMap);
        contextMock = MockHelper.addViewRootMock(contextMock);

        uiComponentMock = mock(UIComponent.class);

        scriptController.init();
    }

    /**
     * Tests if the mocks got injected properly and the dummy data is in the
     * GradeScriptController under test.
     */
    @Test
    public void testInit() {
        assertSame(course, scriptController.getCourse());
        assertSame(user, scriptController.getLoggedInUser());
        assertSame(course.getDefaultParticipationType(),
                scriptController.getSelectedParticipationType());
        assertTrue(scriptController.getScriptOutput().isEmpty());
        assertTrue(scriptController.getUserIsLecturer());
        assertTrue(scriptController.getActiveTabIndex()
                == course.getParticipationTypes().indexOf(course.getDefaultParticipationType()));
        assertSame(scriptController.getSelectedParticipationType(), defaultPartType);
    }

    /**
     * Tests if the python interpreter gets closed when the bean gets destroyed.
     */
    @Test
    public void testDestroy() {
        scriptController.destroy();
        verify(pyInterpreterMock, times(1)).cleanup();
        verify(pyInterpreterMock, times(1)).close();
    }

    /**
     * Tests if an IllegalArgumentException gets thrown if the tab change
     * ui callback gets called with null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnTabChangedNull() {
        scriptController.onTabChange(null);
    }

    /**
     * Tests if an IllegalArgumentException gets thrown if the tab change
     * ui callback has no TabView component
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnTabChangedComponentNull() {
        TabChangeEvent eventMock = mock(TabChangeEvent.class);
        when(eventMock.getComponent()).thenReturn(null);
        scriptController.onTabChange(eventMock);
    }

    /**
     * Tests if an IllegalArgumentException gets thrown if the tab change
     * ui callback has no TabView component
     */
    @Test
    public void testOnTabChanged() {
        int newTabIdx = 1;
        TabChangeEvent eventMock = mock(TabChangeEvent.class);
        TabView tabViewMock = mock(TabView.class);
        when(tabViewMock.getActiveIndex()).thenReturn(newTabIdx);
        when(eventMock.getComponent()).thenReturn(tabViewMock);
        scriptController.onTabChange(eventMock);
        assertTrue(scriptController.getActiveTabIndex() == newTabIdx);
        assertSame(scriptController.getActiveParticipationType(),
                (course.getParticipationTypes().get(newTabIdx)));
    }

    /**
     * Tears down the test environment. The FacesContext mock gets released
     * to prevent memory leaks.
     */
    @After
    public void tearDown() {
        if (contextMock != null) {
            contextMock.release();
        }
    }
}
