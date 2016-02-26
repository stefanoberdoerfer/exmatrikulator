package de.unibremen.opensores.webapp.validator;

import de.unibremen.opensores.testutil.ContextMocker;
import de.unibremen.opensores.testutil.MockHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Test class for the deadline validator class.
 * @author Keivn Scheck
 */
public class DeadlineValidatorTest {

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
     * The DeadlineValidator under test.
     */
    private DeadlineValidator validator;

    @Before
    public void setUp() {
        validator = new DeadlineValidator();
        // Mocking the FacesContext and related classes
        contextMock = MockHelper.addExternalContextMock(ContextMocker.mockBasicFacesContext(), new HashMap<>());
        contextMock = MockHelper.addViewRootMock(contextMock);

        uiComponentMock = Mockito.mock(UIComponent.class);
    }

    /**
     * Tests if the method for deadline validation throws an validation exception
     * if the deadline is in the past.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateDeadLineTooEarly() {
        Date tooEarlyDeadline = new Calendar.Builder()
                .setDate(2000,01,01).build().getTime();
        validator.validate(contextMock, uiComponentMock, tooEarlyDeadline);
    }

    /**
     * Tests if the method for deadline validation throws an validation exception
     * if the deadline is null
     */
    @Test(expected = ValidatorException.class)
    public void testValidateDeadLineIsNull() {
        validator.validate(contextMock, uiComponentMock, null);
    }

    /**
     * Tests if the method for deadline validation with a correct deadline in
     * the future.
     */
    @Test
    public void testValidateDeadLine() {
        Date doomsDay = new Calendar.Builder()
                .setDate(2016,03,06).build().getTime();
        validator.validate(contextMock, uiComponentMock, doomsDay);
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
