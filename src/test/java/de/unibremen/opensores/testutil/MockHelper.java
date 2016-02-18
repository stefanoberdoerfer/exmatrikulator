package de.unibremen.opensores.testutil;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeFormulaService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class to mock the most used dependencies.
 * It also holds constants related to the test setup, like the value for http
 * params.
 * Feel free to extend these methods to suit your test needs, as long as all
 * other tests still pass after you've extended these methods.
 * @author Kevin Scheck
 */
public final class MockHelper {

    /**
     * The locale of the mocked locale returned by the mocked facesContext,
     * if context mock gets extended by a mocked view root.
     */
    public static final String LOCALE_COUNTRY = "en";


    /**
     * Private Constructor for disabling object creation of this class.
     */
    private MockHelper() {
        //Do nothing.
    }

    /**
     * Mocks the basic functionality of the CourseService class
     * @param course The course object which is used for tests, cant be null.
     * @return The mocked CourseService.
     */
    public static CourseService mockCourseService(Course course) {
        if (course == null) {
            throw new IllegalArgumentException(
                    "The course object can't be null. Create a test data course"
                            + " object and pass it as parameter.");
        }

        CourseService courseServiceMock = mock(CourseService.class);
        when(courseServiceMock.find(Course.class, DataHelper.COURSE_ID)).thenReturn(course);
        when(courseServiceMock.update(course)).thenReturn(course);
        return courseServiceMock;
    }

    /**
     * Mocks the logService for Database transactions related to logs.
     * @return The mock of the LogService
     */
    public static LogService mockLogService(){
        LogService logServiceMock = mock(LogService.class);
        Answer<Log> returnSameObjAnswer = invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return (args.length > 0 && (args[0] instanceof Log)) ? (Log) args[0] : null;
        };
        when(logServiceMock.persist(any(Log.class))).thenAnswer(returnSameObjAnswer);
        when(logServiceMock.update(any(Log.class))).thenAnswer(returnSameObjAnswer);
        return logServiceMock;
    }


    /**
     * Mocks the student service class.
     * @return A mock of the student service class
     */
    public static StudentService mockStudentService() {
        StudentService studentServiceMock = mock(StudentService.class);
        //TODO Extend with desired functionality
        return studentServiceMock;
    }

    /**
     * Mocks the GradeFormulaService class.
     * @return A mock of the GradeFormulaService class.
     */
    public static GradeFormulaService mockGradeFormulaService() {
        GradeFormulaService gradeFormulaServiceMock = mock(GradeFormulaService.class);
        Answer<GradeFormula> returnSameObjAnswer = invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return (args.length > 0 && (args[0] instanceof GradeFormula))
                    ? (GradeFormula) args[0] : null;
        };
        when(gradeFormulaServiceMock.persist(any(GradeFormula.class)))
                .thenAnswer(returnSameObjAnswer);
        when(gradeFormulaServiceMock.update(any(GradeFormula.class)))
                .thenAnswer(returnSameObjAnswer);
        return gradeFormulaServiceMock;
    }

    /**
     * Mocks the functionality of f the CourseSerivce class.
     * @todo Extend with own desired basic functionality.
     * @return The Mocked UserService.
     */
    public static UserService mockUserService() {
        UserService userService = mock(UserService.class);
        return userService;
    }


    /**
     * Extends an FacesContext mock with a mocked external context.
     * It adds a  http request mock which has suitable parameters set (like course-id)
     * with defined values by this class.
     * @param contextMock The existing contextMock, which should be extended.
     * @param sessionMap The custom  map which should be set as session map in the
     *                   external context.
     * @return The mocked context with a mocked external context.
     */
    public static FacesContext addExternalContextMock(FacesContext contextMock,
                                                      Map<String,Object> sessionMap) {
        HttpServletRequest reqMock = mock(HttpServletRequest.class);
        when(reqMock.getParameter(Constants.HTTP_PARAM_COURSE_ID)).
                thenReturn(String.valueOf(DataHelper.COURSE_ID));
        //Extend here if you want more http params

        ExternalContext externalContextMock = mock(ExternalContext.class);
        when(externalContextMock.getSessionMap()).thenReturn(sessionMap);
        when(externalContextMock.getRequest()).thenReturn(reqMock);
        when(contextMock.getExternalContext()).thenReturn(externalContextMock);

        return contextMock;
    }

    /**
     * Extends the contextMock with a mock of the view root, which has set
     * the bundle country locale to the constant in this class.
     * @param contextMock The existing contextMock
     * @return The extended contextMock which returns a viewroot with a set
     *         default locale.
     */
    public static FacesContext addViewRootMock(FacesContext contextMock) {
        UIViewRoot uiViewRootMock = mock(UIViewRoot.class);
        Mockito.when(contextMock.getViewRoot())
                .thenReturn(uiViewRootMock);
        Mockito.when(contextMock.getViewRoot().getLocale())
                .thenReturn(new Locale(LOCALE_COUNTRY));
        return contextMock;
    }
}