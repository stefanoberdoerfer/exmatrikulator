package de.unibremen.opensores.webapp.filter;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.service.CourseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
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

/**
 * Filter to check access to courses. Also extracts the
 * course-id parameter from the HTTP request, fetches the
 * associated course and adds it to the sessionmap.
 *
 * @author Sören Tempel
 */
@Stateless
public class CourseFilter implements Filter {


    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(LoginFilter.class);


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
    private CourseService courseService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse hres = (HttpServletResponse) res;
        HttpServletRequest hreq = (HttpServletRequest) req;
        String path = hreq.getRequestURI().substring(hreq.getContextPath().length());
        log.debug("doFilter() called with path: " + path);
        String freq = hreq.getHeader("Faces-Request");
        if (freq != null && freq.equals("partial/ajax")) {
            filterChain.doFilter(req, res);
            return;
        }

        User user = (User) hreq.getSession().getAttribute("user");
        if (user == null) {
            hres.sendRedirect(hreq.getContextPath());
            return;
        }

        // Must pass here or the download doesnt work.
        if (path.startsWith(PATH_PABO_DOWNLOAD) || path.startsWith(PATH_CSV_DOWNLOAD) ) {
            log.debug("Letting pass  download path ");
            filterChain.doFilter(req, res);
            return;
        }

        String idStr = hreq.getParameter("course-id");
        if (idStr == null || idStr.trim().isEmpty()) {
            hres.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Course course = courseService.findCourseById(idStr);
        if (course == null) {
            hres.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (course.containsUser(user)) {
            filterChain.doFilter(req, res);
        } else {
            hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do here.
        return;
    }

    @Override
    public void destroy() {
        // Nothing to do here.
        return;
    }
}