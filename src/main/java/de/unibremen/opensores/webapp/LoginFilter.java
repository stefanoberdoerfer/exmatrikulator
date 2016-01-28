package de.unibremen.opensores.webapp;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * This class handles HTTP requests and checks whether the request can be
 * directed to a secured page based on whether the user is logged in and if the
 * user has the required rights to access that page.
 * @todo Discuss: Add /app/* root folder for all secured pages, e.g. /course/*?
 * @todo Collab: Find way to filter user-course privileges (session map?)
 * @todo Handle AJAX /resource requests.
 * @todo Move index.xhtml to /course/ etc., it can't be in the root webapp .
 * @author Kevin Scheck
 */
@WebFilter
public final class LoginFilter implements Filter {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(LoginFilter.class);


    /**
     * The session map attribute for a logged in user.
     */
    private static final String SESSION_USER = "user";

    /**
     * The context path of sites in which only an admin has access.
     */
    private static final String ADMIN_PATH
            = "/admin/";

    /**
     * The context path of sites in which only an lecturer has access.
     */
    private static final String LECTURER_ONLY_PATH
            = "/course/create/";


    /**
     * Main method for filtering requests.
     */
    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain filterChain)
            throws IOException, ServletException {
        log.debug("doFilter() called");
        final FilterContext context = new FilterContext(req, res, filterChain);
        log.debug("Context Path: " + context.httpRequest.getContextPath());
        //The requested path from the http request
        String path = context.httpRequest.getRequestURI()
                            .substring(context.httpRequest
                                    .getContextPath().length());
        log.debug("Path: " + path);

        boolean loggedIn = (context.httpSession != null)
                && (context.httpSession.getAttribute(SESSION_USER) != null);
        log.debug("User is logged in: " + loggedIn);

        if (!loggedIn) {
            context.httpResponse
                    .sendRedirect(context.httpRequest.getContextPath());
            return;
        }

        //Starting redirecting by specified static / dynamic user roles.
        if (path.startsWith(ADMIN_PATH)) {
            filterAdminPath(context);
        } else if (path.startsWith(LECTURER_ONLY_PATH)) {
            filterLecturerPath(context);
        } else {
            filterChain.doFilter(req, res);
        }
    }

    /**
     * Filters the redirection given the admin path gets gets requested.
     * @param context The FilterContext helper class.
     * @pre The user is logged in, the httpSession has a user object.
     */
    private void filterAdminPath(FilterContext context)
            throws IOException, ServletException {
        log.debug("filterAdminPath has been called: "
                + context.httpRequest.getRequestURI());
        User user = (User) context.httpSession.getAttribute(SESSION_USER);
        if (user.hasRole("ADMIN")) {
            context.filterChain.doFilter(context.request, context.response);
        } else {
            context.httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Filters the redirection of sites to which only a global lecturer
     * has access to.
     * @param context The filterContext helper class for this request.
     */
    private void filterLecturerPath(FilterContext context)
        throws IOException, ServletException {
        log.debug("filterLecturerPath has been called: "
                + context.httpRequest.getRequestURI());
        User user = (User) context.httpSession.getAttribute(SESSION_USER);
        if (user.hasRole("LECTURER")) {
            context.filterChain.doFilter(context.request, context.response);
        } else {
            context.httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Method called when the LoginFilter gets initialised.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }


    /**
     * Method called when the LoginFiler gets destroyed.
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * Private helper class which structures the context of a filtering.
     */
    private final class FilterContext {
        private ServletRequest request;
        private ServletResponse response;
        private FilterChain filterChain;

        private HttpServletRequest httpRequest;
        private HttpServletResponse httpResponse;
        private HttpSession httpSession;

        /**
         * Constructor of the FilterContext
         * @param request The ServletRequest of the filter.
         * @param response The ServletResponse of the filter.
         * @param filterChain The FilterChain of the filter.
         */
        public FilterContext(@NotNull ServletRequest request,
                             @NotNull ServletResponse response,
                             @NotNull FilterChain filterChain) {
            this.request = request;
            this.response = response;
            this.filterChain = filterChain;
            this.httpRequest = (HttpServletRequest) request;
            this.httpResponse = (HttpServletResponse) response;
            this.httpSession = httpRequest.getSession(false);
        }
    }
}
