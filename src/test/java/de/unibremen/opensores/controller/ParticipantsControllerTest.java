package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Lecturer;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.PasswordReset;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.PrivilegedUserService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.testutil.ContextMocker;
import de.unibremen.opensores.testutil.MockHelper;
import de.unibremen.opensores.testutil.DataHelper;
import de.unibremen.opensores.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test class implements white box tests of the ParticipantsController class.
 * It uses Mockito to mock the FacesContext and the service classes.
 * @author Kevin Scheck
 */
public class ParticipantsControllerTest {

    /**
     * The ParticipantsController under test.
     */
    private ParticipantsController participantsController;

    /**
     * The offset difference between to participation type ids.
     */
    private static final float DELTA_PART_TYPE_ID = (float) 0.0001;

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
     * A mock of the UserService for database transactions related to users.
     */
    @Mock
    private UserService userServiceMock;

    /**
     * A mock of the student service for database transactions related to students.
     */
    @Mock
    private StudentService studentServiceMock;

    /**
     * A mock of the privilege user service for database transactions
     * related to privileged users.
     */
    @Mock
    private PrivilegedUserService privilegedUserService;

    /**
     * A mock of the LogService to persist logs related to exmatrikulator actions.
     */
    @Mock
    private LogService logServiceMock;

    /**
     * The course object used for tests.
     */
    public Course course;

    /**
     * The custom session map with gets used by the mocked FacesContext.
     */
    private Map<String, Object> sessionMap;

    /**
     * The logged in user in user.
     */
    private User loggedIn;

    /**
     * Sets up the test environment for the ParticipantsController tests.
     * Mocks the dependencies of the ParticipantsController and generates dummy
     * data.
     */
    @Before
    public void setUp() {
        course = DataHelper.createBasicCourse();
        loggedIn = DataHelper.createBasicGlobalLecturer();
        Lecturer lecturer = DataHelper.createLecturerWith(course, loggedIn);

        sessionMap = new HashMap<>();
        sessionMap.put(Constants.SESSION_MAP_KEY_USER, loggedIn);

        participantsController = new ParticipantsController();

        contextMock = ContextMocker.mockBasicFacesContext();
        contextMock = MockHelper.addViewRootMock(contextMock);
        contextMock = MockHelper.addExternalContextMock(contextMock, sessionMap);

        courseServiceMock = MockHelper.mockCourseService(course);
        participantsController.setCourseService(courseServiceMock);

        userServiceMock = MockHelper.mockUserService();
        participantsController.setUserService(userServiceMock);

        privilegedUserService = Mockito.mock(PrivilegedUserService.class);
        participantsController.setPrivilegedUserService(privilegedUserService);

        logServiceMock = Mockito.mock(LogService.class);
        participantsController.setLogService(logServiceMock);

        studentServiceMock = Mockito.mock(StudentService.class);
        participantsController.setStudentService(studentServiceMock);

        uiComponentMock = Mockito.mock(UIComponent.class);

        participantsController.init();
    }

    /**
     * Tests if the mocked dependencies got injected properly, and the test
     * data is in the participantsController.
     * Tests if the participation type id which got selected corresponds to the
     * first participation type id.
     */
    @Test
    public void testInit() {
        assertSame(course, participantsController.getCourse());
        assertSame(loggedIn, participantsController.getLoggedInUser());
        assertEquals(course.getDefaultParticipationType().getPartTypeId(),
                participantsController.getSelectedParticipationTypeId(),
                DELTA_PART_TYPE_ID);
        assertTrue(participantsController.isLoggedInUserCanManageStudents());
    }

    /**
     * Tests if the values get reset if the user dialog gets called.
     */
    @Test
    public void testOnCreateUserDialogCalled() {
        participantsController.onCreateUserDialogCalled();
        testResetValues();
        assertNull(participantsController.getSelectedUser().getUserId());
    }

    /**
     * Tests if the the values get reset if an user who is already existing
     * in the exmatrikulator is to be added as user.
     */
    @Test
    public void testOnAddExistingUserCalled() {
        User user = DataHelper.createBasicUser();
        user.setUserId(DataHelper.getNewObjectId());
        assertNotSame(user, participantsController.getSelectedUser());
        participantsController.onAddExistingUserCalled(user);
        assertSame(user, participantsController.getSelectedUser());
        testResetValues();
    }

    /**
     * Tests if an IllegalArgumentException gets thrown with a null parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnAddExistingUserCalledNullParam() {
        participantsController.onAddExistingUserCalled(null);
    }

    /**
     * Tests if the values from a privileged user gets passed correctly to the
     * ParticipantsController when the edit dialog gets called with an Privileged
     * user.
     */
    @Test
    public void testOnEditPrivilegedUserDialogCalled() {
        //Testing a privileged user with all priviliges
        User user = DataHelper.createBasicUserWithNewId();
        PrivilegedUser privilegedUser = DataHelper.createPrivUserWith(course, user);
        privilegedUser.setSecretary(true);
        List<Integer> privileges = new ArrayList<>();
        privileges.add(Privilege.EditExams.getId());
        privileges.add(Privilege.ManageStudents.getId());
        privileges.add(Privilege.EditFormulas.getId());
        privileges.add(Privilege.ExportData.getId());
        privileges.add(Privilege.ManageTutorials.getId());
        privileges.add(Privilege.GenerateCredits.getId());
        privilegedUser.setPrivileges(privileges);

        assertNotNull(course.getPrivilegedUserFromUser(user));
        assertNotSame(privilegedUser.getUser(), participantsController.getSelectedUser());
        participantsController.onEditPrivilegedUserDialogCalled(privilegedUser);
        assertSame(privilegedUser.getUser(), participantsController.getSelectedUser());
        assertEquals((int) Role.PRIVILEGED_USER.getId(),
                participantsController.getSelectedRoleId());
        assertTrue(participantsController.isPrivilegedUserSecretary());
        assertTrue(participantsController.isPrivilegeExams());
        assertTrue(participantsController.isPrivilegeFormula());
        assertTrue(participantsController.isPrivilegeStudents());
        assertTrue(participantsController.isPrivilegeExportData());
        assertTrue(participantsController.isPrivilegeGenerateCredits());


        // Testing a privileged user without any privileges

        User anotherUser = DataHelper.createBasicUserWithNewId();
        PrivilegedUser withoutPriv = DataHelper.createPrivUserWith(course, anotherUser);
        withoutPriv.setSecretary(false);
        withoutPriv.setPrivileges(new ArrayList<>());

        assertNotNull(course.getPrivilegedUserFromUser(anotherUser));
        assertNotSame(withoutPriv.getUser(), participantsController.getSelectedUser());
        participantsController.onEditPrivilegedUserDialogCalled(withoutPriv);
        assertSame(withoutPriv.getUser(), participantsController.getSelectedUser());
        assertEquals((int) Role.PRIVILEGED_USER.getId(),
                participantsController.getSelectedRoleId());
        assertFalse(participantsController.isPrivilegedUserSecretary());
        assertFalse(participantsController.isPrivilegeExams());
        assertFalse(participantsController.isPrivilegeFormula());
        assertFalse(participantsController.isPrivilegeStudents());
        assertFalse(participantsController.isPrivilegeExportData());
        assertFalse(participantsController.isPrivilegeGenerateCredits());
    }

    /**
     * Tests if an IllegalArgumentException gets thrown with a null parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnEditPrivilegedUserDialogCalledNullParam() {
        participantsController.onEditPrivilegedUserDialogCalled(null);
    }

    /**
     * Tests if the values get updated correctly if a student gets called with
     * the edit dialog.
     */
    @Test
    public void testOnEditStudentDialogCalled() {
        ParticipationType newType = DataHelper.createParticipationTypeIn(course);
        User user = DataHelper.createBasicUserWithNewId();
        Student student = DataHelper.createStudentWith(course, user);
        student.setParticipationType(newType);
        assertNotSame(student.getUser(), participantsController.getSelectedUser());
        participantsController.onEditStudentDialogCalled(student);
        assertEquals((int) Role.STUDENT.getId(), participantsController.getSelectedRoleId());
        assertEquals((long)newType.getPartTypeId(),
                participantsController.getSelectedParticipationTypeId());
    }

    /**
     * Tests if an IllegalArgumentException gets thrown with a null parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnEditStudentDialogCalledNullParam() {
        participantsController.onEditStudentDialogCalled(null);
    }

    /**
     * Tests if the user of the to be deleted lecturer gets
     * set as selected user and all other selected values of the class get updated.
     */
    @Test
    public void testOnDeleteLecturerDialogCalled() {
        User user = DataHelper.createBasicUser();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);
        assertNotSame(lecturer.getUser(), participantsController.getSelectedUser());
        participantsController.onDeleteLecturerDialogCalled(lecturer);
        assertSame(lecturer.getUser(), participantsController.getSelectedUser());
        testResetValues();
    }

    /**
     * Tests if an IllegalArgumentException gets thrown with a null parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnDeleteLecturerDialogCalledNullParam() {
        participantsController.onDeleteLecturerDialogCalled(null);
    }

    /**
     * Tests if the user of the to be deleted privileged user gets
     * set as selected user and all other selected values of the class get updated.
     */
    @Test
    public void testOnDeletePrivilegedUserDialogCalled() {
        User user = DataHelper.createBasicUser();
        PrivilegedUser privilegedUser = DataHelper.createPrivUserWith(course, user);
        assertNotSame(privilegedUser.getUser(), participantsController.getSelectedUser());
        participantsController.onDeletePrivilegedUserDialogCalled(privilegedUser);
        assertSame(privilegedUser.getUser(), participantsController.getSelectedUser());
        testResetValues();
    }

    /**
     * Tests if an IllegalArgumentException gets thrown with a null parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnDeletePrivilegedUserDialogCalledNullParam() {
        participantsController.onDeletePrivilegedUserDialogCalled(null);
    }

    /**
     * Tests if the user of the to be deleted student gets set as selected user,
     * and all other selected values of the class get updated.
     */
    @Test
    public void testOnDeleteStudentDialogCalled() {
        User user = DataHelper.createBasicUser();
        Student student = DataHelper.createStudentWith(course, user);
        assertNotSame(student.getUser(), participantsController.getSelectedUser());
        participantsController.onDeleteStudentDialogCalled(student);
        assertSame(student.getUser(), participantsController.getSelectedUser());
        testResetValues();
    }

    /**
     * Tests if an IllegalArgumentException gets thrown with a null parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnDeleteStudentDialogCalledNullParam() {
        participantsController.onDeleteStudentDialogCalled(null);
    }

    /**
     * Tests if a new privileged user gets inserted correctly and a new
     * privileged user object with the right properties gets added to the course.
     */
    @Test
    public void testCreateNewUserWithPrivUser() {
        final int oldPrivUserCount = course.getTutors().size();
        User user = DataHelper.createBasicUser();
        user.setUserId(null);
        assertNull(user.getToken());

        participantsController.setSelectedUser(user);
        participantsController.setPrivilegedUserSecretary(true);
        participantsController.setPrivilegeExams(true);
        participantsController.setPrivilegeExportData(true);
        participantsController.setPrivilegeFormula(true);
        participantsController.setPrivilegeGenerateCredits(true);
        participantsController.setPrivilegeStudents(true);
        participantsController.setPrivilegeTutorials(true);
        participantsController.setPrivilegeStudents(true);

        when(userServiceMock.initPasswordReset(eq(user), anyInt()))
                .thenReturn(new PasswordReset());

        participantsController.setSelectedUser(user);
        participantsController.setSelectedRoleId(Role.PRIVILEGED_USER.getId());
        participantsController.createNewUser();

        final int newPrivUserCount = course.getTutors().size();
        assertTrue(newPrivUserCount > oldPrivUserCount);
        PrivilegedUser createdPrivUser = course.getPrivilegedUserFromUser(user);
        assertNotNull(createdPrivUser);
        assertTrue(createdPrivUser.isSecretary());
        assertTrue(createdPrivUser.hasPrivilege(Privilege.EditExams.name()));
        assertTrue(createdPrivUser.hasPrivilege(Privilege.EditFormulas.name()));
        assertTrue(createdPrivUser.hasPrivilege(Privilege.ManageTutorials.name()));
        assertTrue(createdPrivUser.hasPrivilege(Privilege.ManageStudents.name()));
        assertTrue(createdPrivUser.hasPrivilege(Privilege.ExportData.name()));
        assertTrue(createdPrivUser.hasPrivilege(Privilege.GenerateCredits.name()));

        verify(courseServiceMock, times(1)).update(course);
        verify(userServiceMock, times(1)).initPasswordReset(eq(user), anyInt());
    }

    /**
     * Tests if a new user gets inserted correctly and a new student object
     * gets added to the course.
     */
    @Test
    public void testCreateNewUserWithStudent() {
        final int oldStudentCount = course.getStudents().size();
        ParticipationType newType = DataHelper.createParticipationTypeIn(course);
        User user = DataHelper.createBasicUser();
        user.setUserId(null);
        assertNull(user.getToken());

        participantsController.setSelectedUser(user);
        participantsController.setSelectedParticipationTypeId(newType.getPartTypeId());
        participantsController.setSelectedRoleId(Role.STUDENT.getId());

        when(userServiceMock.initPasswordReset(eq(user), anyInt()))
                .thenReturn(new PasswordReset());

        participantsController.createNewUser();

        assertNotNull(user.getToken());
        assertNull(user.getPassword());
        verify(courseServiceMock, times(1)).update(course);
        verify(userServiceMock, times(1)).initPasswordReset(eq(user), anyInt());

        final int newStudentsCount = course.getStudents().size();
        assertTrue(oldStudentCount < newStudentsCount);
        Student createdStudent = course.getStudentFromUser(user);
        assertNotNull(createdStudent);
        assertEquals(newType, createdStudent.getParticipationType());
    }

    /**
     * Tests if the student gets hidden after delete Selected Participation gets
     * called.
     */
    @Test
    public void testDeleteSelectedParticipationForStudent() {
        User user = DataHelper.createBasicUserWithNewId();
        Student student = DataHelper.createStudentWith(course, user);
        assertTrue(course.containsUser(user));
        assertFalse(student.isHidden());
        participantsController.onDeleteStudentDialogCalled(student);
        participantsController.deleteSelectedParticipation();
        assertTrue(student.isHidden());
        verify(courseServiceMock, times(1)).update(course);
        testResetValues();
    }

    /**
     * Tests if the lecturer hidden after deleteSelectedParticipation
     * gets called.
     */
    @Test
    public void testDeleteSelectedParticipationForPrivUser() {
        User user = DataHelper.createBasicUserWithNewId();
        Lecturer lecturer = DataHelper.createLecturerWith(course, user);
        assertTrue(course.containsUser(user));
        assertFalse(lecturer.isHidden());
        participantsController.onDeleteLecturerDialogCalled(lecturer);
        participantsController.deleteSelectedParticipation();
        assertTrue(lecturer.isHidden());
        verify(courseServiceMock, times(1)).update(course);
        testResetValues();
    }

    /**
     * Tests if the privileged user gets hidden after deleteSelectedParticipation
     * gets called.
     */
    @Test
    public void testDeleteSelectedParticipationForLecturer() {
        User user = DataHelper.createBasicUserWithNewId();
        PrivilegedUser privUser = DataHelper.createPrivUserWith(course, user);
        assertTrue(course.containsUser(user));
        assertFalse(privUser.isHidden());
        participantsController.onDeletePrivilegedUserDialogCalled(privUser);
        participantsController.deleteSelectedParticipation();
        assertTrue(privUser.isHidden());
        verify(courseServiceMock, times(1)).update(course);
        testResetValues();
    }

    /**
     * Tests if an IllegalStateException gets thrown if deleteSelectedParticipation
     * gets called with no participation class selected.
     */
    @Test(expected = IllegalStateException.class)
    public void testDeleteSelectedParticipationNoneSelected() {
        participantsController.deleteSelectedParticipation();
    }

    /**
     * Tests if an already existing privileged user, which got changed to an existing
     * student gets updated correctly.
     * The existing privileged user association gets hidden, the old student
     * association must be unhidden.
     * Also a new selected participation type must be selected.
     */
    @Test
    public void testSaveEditChangesToStudent() {
        ParticipationType newType = DataHelper.createParticipationTypeIn(course);
        ParticipationType defaultType = course.getDefaultParticipationType();
        User user = DataHelper.createBasicUserWithNewId();
        Student student = DataHelper.createStudentWith(course, user);
        student.setHidden(true);
        student.setParticipationType(newType);
        PrivilegedUser privUser = DataHelper.createPrivUserWith(course, user);
        privUser.setHidden(false);
        participantsController.onEditPrivilegedUserDialogCalled(privUser);
        participantsController.setSelectedRoleId(Role.STUDENT.getId());
        participantsController.setSelectedParticipationTypeId(defaultType.getPartTypeId());
        participantsController.saveEditChanges();
        assertTrue(privUser.isHidden());
        assertFalse(student.isHidden());
        assertSame(defaultType, student.getParticipationType());
        verify(courseServiceMock, times(1)).update(course);
    }

    /**
     * Tests saving values from an existing student to an old privileged user.
     * The old student association must be hidden, the old privielged user
     * association must be unhidden an updated with new values.
     */
    @Test
    public void testSaveEditChangesToPrivUser() {
        User user = DataHelper.createBasicUserWithNewId();
        Student student = DataHelper.createStudentWith(course, user);
        student.setHidden(false);
        PrivilegedUser privUser = DataHelper.createPrivUserWith(course, user);
        privUser.setHidden(true);
        privUser.setPrivileges(new ArrayList<>());
        privUser.setSecretary(false);
        participantsController.onEditStudentDialogCalled(student);
        participantsController.setSelectedRoleId(Role.PRIVILEGED_USER.getId());
        participantsController.setPrivilegedUserSecretary(true);
        participantsController.setPrivilegeExams(true);
        participantsController.setPrivilegeFormula(true);
        participantsController.saveEditChanges();
        verify(courseServiceMock, times(1)).update(course);
        assertTrue(student.isHidden());
        assertFalse(privUser.isHidden());
        assertTrue(privUser.isSecretary());

        assertTrue(privUser.hasPrivilege(Privilege.EditExams.name()));
        assertTrue(privUser.hasPrivilege(Privilege.EditFormulas.name()));
        assertFalse(privUser.hasPrivilege(Privilege.ManageTutorials.name()));
        assertFalse(privUser.hasPrivilege(Privilege.ManageStudents.name()));
        assertFalse(privUser.hasPrivilege(Privilege.ExportData.name()));
        assertFalse(privUser.hasPrivilege(Privilege.GenerateCredits.name()));
    }

    /**
     * Tests if the student gets confirmed when confirmStudent gets called, and
     * the course gets updated.
     */
    @Test
    public void testConfirmStudent() {
        User user = DataHelper.createBasicUserWithNewId();
        Student student = DataHelper.createStudentWith(course, user);
        student.setConfirmed(false);
        participantsController.confirmStudent(student);
        assertTrue(student.isConfirmed());
        verify(courseServiceMock, times(1)).update(course);
    }

    /**
     * Tests if the method confirmStudent returns if the student is already
     * confirmed.
     */
    @Test
    public void testConfirmStudentAlreadyConfirmed() {
        User user = DataHelper.createBasicUserWithNewId();
        Student student = DataHelper.createStudentWith(course, user);
        student.setConfirmed(true);
        participantsController.confirmStudent(student);
        assertTrue(student.isConfirmed());
        verify(courseServiceMock, times(0)).update(course);
    }

    /**
     * Tests if an IllegalArgumentException gets thrown with a null parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConfirmStudentNullParam() {
        participantsController.confirmStudent(null);
    }

    /**
     * Tests if the result list for users is empty when null or an empty string
     * get inserted.
     * Tests if the result list gets filtered for users already associated with
     * the course (students, privileged users, lecturers).
     */
    @Test
    public void testSearchForExternalUsers() {
        participantsController.setUserTextInput("");
        participantsController.searchForExternalUsers();
        assertTrue(participantsController.getUserSearchResultList().isEmpty());

        participantsController.setUserTextInput(null);
        participantsController.searchForExternalUsers();
        assertTrue(participantsController.getUserSearchResultList().isEmpty());

        // Lecturer in the raw result list, should be filtered
        assertNotNull(course.getLecturerFromUser(loggedIn));
        List<User> expectedRawResults = new ArrayList<>();
        expectedRawResults.add(loggedIn);
        when(userServiceMock.searchForUsers(loggedIn.getFirstName())).thenReturn(expectedRawResults);
        participantsController.setUserTextInput(loggedIn.getFirstName());
        participantsController.searchForExternalUsers();
        //The logged user in the course should be filtered
        //because he is a lecturer in the course
        assertTrue(participantsController.getUserSearchResultList().isEmpty());
        expectedRawResults.clear();

        // Student in the raw result list, should be filtered
        User studentInCourse = DataHelper.createBasicUser();
        studentInCourse.setFirstName("In Course as student");
        Student student = DataHelper.createStudentWith(course, studentInCourse);
        assertNotNull(course.getStudentFromUser(studentInCourse));
        expectedRawResults.add(studentInCourse);
        when(userServiceMock.searchForUsers(studentInCourse.getFirstName()))
                .thenReturn(expectedRawResults);
        participantsController.setUserTextInput(studentInCourse.getFirstName());
        participantsController.searchForExternalUsers();
        //The logged user in the course should be filtered
        //because he is a student in the course
        assertTrue(participantsController.getUserSearchResultList().isEmpty());
        expectedRawResults.clear();

        // Privileged user in the raw result list, should be filtered
        User privUserInCourse = DataHelper.createBasicUser();
        studentInCourse.setFirstName("In Course as PrivUser");
        PrivilegedUser privilegedUser = DataHelper.createPrivUserWith(course, privUserInCourse);
        assertNotNull(course.getPrivilegedUserFromUser(privUserInCourse));
        expectedRawResults.add(privUserInCourse);
        when(userServiceMock.searchForUsers(privUserInCourse.getFirstName()))
                .thenReturn(expectedRawResults);
        participantsController.setUserTextInput(privUserInCourse.getFirstName());
        participantsController.searchForExternalUsers();
        //The logged user in the course should be filtered
        // because he is a privileged user in the course
        assertTrue(participantsController.getUserSearchResultList().isEmpty());
        expectedRawResults.clear();

        // External user in the raw result list, shouldn't be filtered and
        // should be contained in the result list displayed to the user.
        User externalUser = DataHelper.createBasicUser();
        externalUser.setUserId(DataHelper.getNewObjectId());
        externalUser.setFirstName("External");
        expectedRawResults.add(externalUser);
        assertFalse(course.containsUser(externalUser));
        when(userServiceMock.searchForUsers(externalUser.getFirstName()))
                .thenReturn(expectedRawResults);
        participantsController.setUserTextInput(externalUser.getFirstName());
        participantsController.searchForExternalUsers();
        assertTrue(participantsController.getUserSearchResultList().contains(externalUser));
    }

    /**
     * Tests if an ValidatorException in the validation for the deletion input
     * method if the passed in object value is null.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateDeletionNameInputNullParam() {
        participantsController.validateDeletionNameInput(contextMock,
                uiComponentMock, null);
    }

    /**
     * Tests if the ParticipantController validates the input of the name
     * of the selected user to be deleted correctly.
     * The String must equal the first name and the last name of the user.
     */
    @Test
    public void testValidateDeletionNameInput() {
        User user = DataHelper.createBasicUser();
        user.setFirstName("Delete");
        user.setLastName("Me");
        participantsController.setSelectedUser(user);
        participantsController.validateDeletionNameInput(contextMock, uiComponentMock,
                "Delete Me");

        user = DataHelper.createBasicUser();
        user.setFirstName("Hans Meier");
        user.setLastName("Bla");
        participantsController.setSelectedUser(user);
        participantsController.validateDeletionNameInput(contextMock, uiComponentMock,
                "Hans Meier Bla");
    }

    /**
     * Tests if the email validation method passes for valid emails.
     */
    @Test
    public void testValidateEmail() {
        User user = DataHelper.createBasicUserWithNewId();
        String validMail = "validmail@uni-bremen.de";
        user.setEmail(validMail);
        participantsController.setSelectedUser(user);
        //Acting as if the same user is already in the database.
        when(userServiceMock.findByEmail(validMail)).thenReturn(user);
        participantsController.validateEmail(contextMock, uiComponentMock, validMail);
    }

    /**
     * Tests if the email validation method passes for valid emails.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateEmailAlreadyRegistered() {
        User user = DataHelper.createBasicUserWithNewId();
        String validMail = "validmail@uni-bremen.de";
        user.setEmail(validMail);
        participantsController.setSelectedUser(user);
        //Acting as if another user is already in the database.
        User anotherUser = DataHelper.createBasicUserWithNewId();
        when(userServiceMock.findByEmail(validMail)).thenReturn(anotherUser);
        when(userServiceMock.isEmailRegistered(validMail)).thenReturn(true);
        assertNotEquals(anotherUser,user);
        participantsController.validateEmail(contextMock, uiComponentMock, validMail);
    }

    /**
     * Tests if the email validation method throws a ValidationException if
     * null is passed as parameter.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateEmailNullParam() {
        participantsController.validateEmail(contextMock, uiComponentMock, null);
    }

    /**
     * Tests if the email validation method throws a ValidationException if
     * an unvalid email (syntax wise) is passed as parameter.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateEmailUnvalidParam() {
        participantsController.validateEmail(contextMock, uiComponentMock, "my-email :^)");
    }

    /**
     * Tears down the test environment after every test.
     */
    @After
    public void tearDown() {
        if (contextMock != null) {
            contextMock.release();
        }
    }

    /**
     * Tests if the values of the ParticipationController are reset.
     */
    private void testResetValues() {
        assertFalse(participantsController.isPrivilegeExams());
        assertFalse(participantsController.isPrivilegeTutorials());
        assertFalse(participantsController.isPrivilegeFormula());
        assertFalse(participantsController.isPrivilegeStudents());
        assertFalse(participantsController.isPrivilegeExportData());
        assertFalse(participantsController.isPrivilegedUserSecretary());
        assertEquals(participantsController.getSelectedParticipationTypeId(),
                course.getDefaultParticipationType().getPartTypeId(),
                DELTA_PART_TYPE_ID);
        assertTrue(participantsController.getUserSearchResultList().isEmpty());
        assertTrue(participantsController.getUserTextInput().isEmpty());
    }
}
