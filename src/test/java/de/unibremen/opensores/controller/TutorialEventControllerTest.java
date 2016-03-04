package de.unibremen.opensores.controller;

import de.unibremen.opensores.controller.tutorial.TutorialEventController;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.TutorialEvent;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.TutorialService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.testutil.ContextMocker;
import de.unibremen.opensores.testutil.DataHelper;
import de.unibremen.opensores.testutil.MockHelper;
import de.unibremen.opensores.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.primefaces.event.SelectEvent;

import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is a Whitebox tests for the controller TutorialEventController
 * which manages the events of a single tutorial
 * @author Kevin Scheck
 */
public class TutorialEventControllerTest {

    /**
     * A mock of the TutorialService.
     */
    @Mock
    private TutorialService tutorialServiceMock;


    /**
     * A mock of the LogService.
     */
    @Mock
    private LogService logServiceMock;

    /**
     * A mock of the CourseService.
     */
    @Mock
    private CourseService courseServiceMock;

    /**
     * A mock of the UserService.
     */
    @Mock
    private UserService userServiceMock;

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
     * The TutorialEventController under test.
     */
    private TutorialEventController tutEventController;

    /**
     * The logged in test user in this test.
     */
    private User user;

    /**
     * The test course used in this test.
     */
    private Course course;

    /**
     * The test tutorial used in this test.
     */
    private Tutorial tutorial;

    /**
     * The test session map used in this test.
     */
    private Map<String, Object> sessionMap;

    /**
     * Sets up all test classes in mocks.
     * Injects the mocks in the TutorialEventController.
     */
    @Before
    public void setUp() {
        course = DataHelper.createBasicCourse();
        user = DataHelper.createBasicGlobalLecturer();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);
        PrivilegedUser privilegedUser = DataHelper.createPrivUserWith(course, user);

        tutorial = new Tutorial();
        tutorial.setName("Test tut");
        tutorial.setCourse(course);
        tutorial.getTutors().add(privilegedUser);
        privilegedUser.getTutorials().add(tutorial);
        tutorial.setEvents(new ArrayList<>());
        tutorial.setTutorialId(DataHelper.TUTORIAL_ID);
        course.getTutorials().add(tutorial);

        sessionMap = new HashMap<>();
        sessionMap.put(Constants.SESSION_MAP_KEY_USER, user);

        contextMock = ContextMocker.mockBasicFacesContext();
        contextMock = MockHelper.addViewRootMock(contextMock);
        contextMock = MockHelper.addExternalContextMock(contextMock, sessionMap);
        uiComponentMock = Mockito.mock(UIComponent.class);

        tutEventController = new TutorialEventController();

        courseServiceMock = MockHelper.mockCourseService(course);
        tutEventController.setCourseService(courseServiceMock);

        userServiceMock = MockHelper.mockUserService();
        when(userServiceMock.hasCourseRole(user,
                Role.LECTURER.name(), course)).thenReturn(true);
        tutEventController.setUserService(userServiceMock);

        tutorialServiceMock = Mockito.mock(TutorialService.class);
        when(tutorialServiceMock.findTutorialById(String.valueOf(DataHelper.TUTORIAL_ID)))
                .thenReturn(tutorial);
        when(tutorialServiceMock.getTutorOf(tutorial, user)).thenReturn(privilegedUser);
        when(tutorialServiceMock.update(tutorial)).thenReturn(tutorial);

        tutEventController.setTutorialService(tutorialServiceMock);

        logServiceMock = Mockito.mock(LogService.class);
        tutEventController.setLogService(logServiceMock);

        tutEventController.init();
    }

    /**
     * Tests if all mocks got injected properly, the test data for the user,
     * course and tutorial got set properly in the controller.
     * Tests if the Controller has zero events in its event modal at the beginning.
     * Tests if the selected tutorial event is null when the controller is initialized.
     */
    @Test
    public void testInit() {
        assertSame(course, tutEventController.getCourse());
        assertSame(tutorial, tutEventController.getTutorial());
        assertSame(user, tutEventController.getLoggedInUser());
        assertTrue(tutEventController.getTutorialEventModel().getEvents().isEmpty());
        assertNull(tutEventController.getEvent());
        assertTrue(tutEventController.isUserLecturer());
        assertTrue(tutEventController.isUserTutor());
    }

    /**
     * Tests if an event with no PrimeFaces id gets added to the event model.
     * Tests if an event with PrimeFaces id gets updated, and not added to the
     * event model.
     * Tests if the tutorial gets updated after every call of the method.
     */
    @Test
    public void testAddEventNewEvent() {
        assertTrue(tutEventController.getTutorialEventModel().getEvents().isEmpty());
        TutorialEvent event = new TutorialEvent();
        event.setStartDate(new Date());
        event.setEndDate(new Date());
        tutEventController.setEvent(event);
        tutEventController.addEvent(null);
        assertSame(event, tutEventController.getTutorialEventModel().getEvents().get(0));
        verify(tutorialServiceMock, times(1)).update(tutorial);

        event.setId("1");
        event.setStartDate(new Date());
        event.setEndDate(new Date());
        tutEventController.setEvent(event);
        tutEventController.getTutorialEventModel().clear();
        tutEventController.addEvent(null);
        assertTrue(tutEventController.getTutorialEventModel().getEvents().isEmpty());
        verify(tutorialServiceMock, times(2)).update(tutorial);

    }


    /**
     * Tests if no events gets removed if the selected event is null or has no
     * Primafaces id.
     * Tests if the selected event is removed from the event model if it is not null.
     * Tests if the tutorial gets updated according to the updated event model.
     */
    @Test
    public void testRemoveEvent() {
        tutEventController.removeEvent(null);
        assertTrue(tutEventController.getTutorialEventModel().getEvents().isEmpty());
        verify(tutorialServiceMock, times(1)).update(tutorial);

        //Check if an event without primefaces id doesnt get
        TutorialEvent event = new TutorialEvent();
        event.setStartDate(new Date());
        event.setEndDate(new Date());
        tutEventController.getTutorialEventModel().getEvents().add(event);
        tutEventController.setEvent(event);
        tutEventController.removeEvent(null);
        verify(tutorialServiceMock, times(2)).update(tutorial);
        assertSame(event, tutEventController.getTutorialEventModel().getEvents().get(0));

        event.setId("1");
        tutEventController.setEvent(event);
        tutEventController.removeEvent(null);
        verify(tutorialServiceMock, times(3)).update(tutorial);
        assertTrue(tutEventController.getTutorialEventModel().getEvents().isEmpty());
    }

    /**
     * Tests if the currently selected tutorial event gets set as the
     * selected tutorial event when an event is selected in the method
     * onEventSelect().
     */
    @Test
    public void testOnEventSelect() {
        assertNull(tutEventController.getEvent());
        Date startDate = new Date();
        Date endDate = new Date();
        TutorialEvent event = new TutorialEvent();
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        SelectEvent selectEvent = Mockito.mock(SelectEvent.class);
        when (selectEvent.getObject()).thenReturn(event);
        tutEventController.onEventSelect(selectEvent);
        assertSame(event, tutEventController.getEvent());
    }

    /**
     * Tests if a new TutorialEvent gets set when a new date gets selected.
     * The new TutorialEvent must have the same start and end date as the
     * selected date, the tutorial set to the tutorial of the TutorialController
     * and must have the user id of the logged in user as creator id.
     */
    @Test
    public void testOnDateSelect() {
        assertNull(tutEventController.getEvent());
        Date date = new Date();
        SelectEvent selectEvent = Mockito.mock(SelectEvent.class);
        when (selectEvent.getObject()).thenReturn(date);
        tutEventController.onDateSelect(selectEvent);
        assertNotNull(tutEventController.getEvent());
        assertEquals((long)user.getUserId(), tutEventController.getEvent().getCreatorId());
        assertEquals(date, tutEventController.getEvent().getStartDate());
        assertEquals(date, tutEventController.getEvent().getEndDate());
        assertSame(tutorial, tutEventController.getEvent().getTutorial());
    }

    /**
     * Tests if the rights to edit tutorial events are set correctly.
     * If the default event is null, false should get returned.
     * A TutorialEvent without primefaces id gets newly created, therefore has
     * no creator id. A privileged user or lecturer can edit these events.
     * A privileged user or a lecturer can only edit the event, and they must
     * be the creator of the tutorial event.
     */
    @Test
    public void testCanUserEditEvent() {
        assertFalse(tutEventController.canUserEditEvent());//False because event is null

        tutEventController.setEvent(new TutorialEvent()); //True because no creator id
        assertTrue(tutEventController.canUserEditEvent());

        TutorialEvent event = new TutorialEvent();
        event.setId("1");
        event.setCreatorId(-1);
        tutEventController.setEvent(event);
        assertFalse(tutEventController.canUserEditEvent());

        tutEventController.getEvent().setCreatorId(user.getUserId());
        assertTrue(tutEventController.canUserEditEvent());
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

