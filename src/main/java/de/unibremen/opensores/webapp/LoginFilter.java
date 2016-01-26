package de.unibremen.opensores.webapp;

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
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        log.debug("Context Path: " + request.getContextPath());
        //The requested path from the http request
        String path = request.getRequestURI()
                            .substring(request.getContextPath().length());
        log.debug("Path: " + path);

        boolean loggedIn = (session != null)
                && (session.getAttribute(SESSION_USER) != null);
        log.debug("User is logged in: " + loggedIn);

        if (!loggedIn) {
            response.sendRedirect(request.getContextPath());
            return;
        }

        // Getting the user from the session map, checking pages depending on
        // User Roles
        final User user = (User) session.getAttribute(SESSION_USER);

        if (path.startsWith(ADMIN_PATH)) {
            if (user.hasRole("ADMIN")) {
                filterChain.doFilter(req, res);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else if (path.startsWith(LECTURER_ONLY_PATH)) {
            if (user.hasRole("LECTURER")) {
                filterChain.doFilter(req, res);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            filterChain.doFilter(req, res);
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
}
