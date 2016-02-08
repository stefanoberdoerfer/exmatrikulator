package de.unibremen.opensores.testutil;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.faces.context.FacesContext;


/**
 * Factory class for basic mocked FacesContext objects, as documented by this blog
 * entry:
 * http://illegalargumentexception.blogspot.de/2011/12/jsf-mocking-facescontext-for-unit-tests.html#mockFacesCurrentInstance
 * Mocks the FacesContext class by extending it and calling the protected method
 * setCurrentInstance(mockedContext).
 */
public abstract class ContextMocker extends FacesContext {

    /**
     * Disabling the constructor.
     */
    private ContextMocker() {}

    /**
     * Constant properties of the release() answer implementation against leaks.
     */
    private static final Release RELEASE = new Release();

    /**
     * Implements the Mockito Answer interface when the release() method
     * gets called for the mocked context.
     * It sets the current instance of the FacesContext to null to avoid leaks.
     */
    private static class Release implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            setCurrentInstance(null);
            return null;
        }
    }

    /**
     * Returns a mocked instance of FacesContext.
     * If is only a basic mock and doesn't have any other mocks, e.g. for
     * http requests or session maps.
     * @return The mocked FacesContext object.
     */
    public static FacesContext mockBasicFacesContext() {
        FacesContext context = Mockito.mock(FacesContext.class);
        setCurrentInstance(context);
        Mockito.doAnswer(RELEASE)
                .when(context)
                .release();
        return context;
    }
}