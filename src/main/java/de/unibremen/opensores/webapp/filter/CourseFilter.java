package de.unibremen.opensores.webapp.filter;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.service.CourseService;

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
 * Fitler to check access to courses. Also extracts the
 * course-id parameter from the HTTP request, fetches the
 * associated course and adds it to the sessionmap.
 *
 * @author Sören Tempel
 */
@Stateless
public class CourseFilter implements Filter {
    /**
     * The course service for connecting to the database.
     */
    @EJB
    private transient CourseService courseService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse hres = (HttpServletResponse) res;
        HttpServletRequest hreq = (HttpServletRequest) req;

        HttpSession session = hreq.getSession();
        String idStr = hreq.getParameter("course-id");

        User user = (User) session.getAttribute("user");
        if (user == null) {
            hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Course oldCourse = (Course) session.getAttribute("course");
        if (idStr == null || idStr.trim().isEmpty()) {
            /* ajax requests and (possibly) other shit doesn't keep the
             * parameter therefore it's probably a good idea not the remove the
             * course from the session map and only remove it if a new course
             * was given. */

            if (oldCourse != null && !oldCourse.containsUser(user)) {
                hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                filterChain.doFilter(req, res);
            }

            return;
        }

        Course newCourse;
        try {
            newCourse = courseService.find(Course.class,
                Integer.valueOf(idStr).longValue());
        } catch (NumberFormatException e) {
            newCourse = null;
        }

        if (newCourse == null) {
            session.removeAttribute("course");
            hres.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (oldCourse == null || !newCourse.getCourseId()
                .equals(oldCourse.getCourseId())) {
            session.setAttribute("course", newCourse);
            if (!newCourse.containsUser(user)) {
                hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(req, res);
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
