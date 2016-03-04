package de.unibremen.opensores.webapp.validator;

import de.unibremen.opensores.testutil.ContextMocker;
import de.unibremen.opensores.testutil.MockHelper;
import de.unibremen.opensores.util.DateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * This class tests the Date range validator which validates that the end date
 * is after the start date of a date range.
 * @author Kevin Scheck.
 */
public class DateRangeValidatorTest {

    /**
     * A mock of the ui component, which has a start date
     */
    @Mock
    private UIComponent uiComponentMock;

    /**
     * A mock of the facescontext for faces messages.
     */
    @Mock
    private FacesContext contextMock;

    /**
     * The DateRangeValidator under test.
     */
    private DateRangeValidator dateRangeValidator;

    /**
     * A mocked attributes map of the uiComponentMock mock.
     */
    private Map<String, Object> attributesMap;

    /**
     * Sets up the mocks before every test.
     */
    @Before
    public void setUp() {
        uiComponentMock = Mockito.mock(UIComponent.class);
        attributesMap = new HashMap<>();
        attributesMap.put("start-date", DateUtil.tenYearsAgo());
        when(uiComponentMock.getAttributes()).thenReturn(attributesMap);

        contextMock = ContextMocker.mockBasicFacesContext();
        contextMock = MockHelper.addViewRootMock(contextMock);
        contextMock = MockHelper.addExternalContextMock(contextMock, new HashMap<>());

        dateRangeValidator = new DateRangeValidator();
    }

    /**
     * Tests if no ValidatorException gehts thrown if the start date is older
     * than the end date.
     */
    @Test
    public void testOlderDate() {
        dateRangeValidator.validate(contextMock, uiComponentMock, new Date());
    }

    /**
     * Tests if a ValidatorException gets thrown if the start date is the same
     * as the end date.
     */
    @Test(expected = ValidatorException.class)
    public void testSameDate() {
        Date date = new Date();
        attributesMap.put("start-date", date);
        when(uiComponentMock.getAttributes()).thenReturn(attributesMap);
        dateRangeValidator.validate(contextMock, uiComponentMock, date);
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
