package de.unibremen.opensores.webapp.filter;

import com.sun.scenario.effect.FilterContext;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.UserService;

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
            hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
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
