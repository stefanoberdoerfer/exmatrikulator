package de.unibremen.opensores.webapp.filter;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Allows access only to non students.
 * @author Matthias Reichmann
 */
@WebFilter
public final class NoStudentsFilter implements Filter {


    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(NoStudentsFilter.class);

    /**
     *  The path for the exam events page to which students have access for
     *  registering to exam events.
     */
    private static final String PATH_EXAM_EVENTS = "/settings/exams/events";


    /**
     *  Paths which must be passed because downloads require non ajax requests
     *  with own http parameters.
     */
    private static final String PATH_PABO_DOWNLOAD = "/settings/pabo.xhtml";
    private static final String PATH_CSV_DOWNLOAD = "/settings/overview.xhtml";

    /**
     * The course service for connecting to the database.
     */
    @EJB
    private transient CourseService courseService;
    /**
     * The user service for connecting to the database.
     */
    @EJB
    private transient UserService userService;

    /**
     * Main method for filtering requests.
     */
    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletResponse hres = (HttpServletResponse) res;
        HttpServletRequest hreq = (HttpServletRequest) req;
        String path = hreq.getRequestURI().substring(hreq.getContextPath().length());
        log.debug("doFilter() called with path " + path);

        // Must pass here or the download doesnt work.
        if (path.startsWith(PATH_PABO_DOWNLOAD) || path.startsWith(PATH_CSV_DOWNLOAD) ) {
            log.debug("Letting pass  download path ");
            filterChain.doFilter(req, res);
            return;
        }

        // XXX this is _super_ insecure literally everyone can set this
        // header and thereby render this filter absolutly useless.
        String freq = hreq.getHeader("Faces-Request");
        if (freq != null && freq.equals("partial/ajax")) {
            filterChain.doFilter(req, res);
            return;
        }

        User user = (User) hreq.getSession().getAttribute("user");

        if (user == null) {
            hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String idStr = hreq.getParameter("course-id");
        Course course = courseService.findCourseById(idStr);

        if (course == null) {
            hres.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (userService.hasCourseRole(user, Role.LECTURER, course)
                || userService.hasCourseRole(user, Role.PRIVILEGED_USER, course)) {
            filterChain.doFilter(req, res);
        } else {
            if (path.startsWith(PATH_EXAM_EVENTS)
                    && userService.hasCourseRole(user, Role.STUDENT, course)) {
                filterChain.doFilter(req, res);
            } else {
                hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
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