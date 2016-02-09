package de.unibremen.opensores.webapp.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.ResourceHandler;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to prevent caching during production
 * so that Matthias wont waste time for gui stuff.
 * Copy pasted from Stackoverflow, to be removed for production.
 * http://stackoverflow.com/questions/10305718/avoid-back-button-on-jsf-web-application
 */
public class NoCacheFilter implements Filter {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(NoCacheFilter.class);


    /**
     * Initialisation of a Web Filter.
     */
    public void init(FilterConfig config) throws ServletException {
        // TODO Auto-generated method stub
    }


    /**
     * @see Filter#destroy().
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /**
     * Main Filter method for disabling caching for ServletRequests.
     */
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // apply no caching for all web pages except resources//
        // you can customize that to be applied for specific pages
        if (!req.getRequestURI().startsWith(req.getContextPath()
                + ResourceHandler.RESOURCE_IDENTIFIER)) {
            // Skip JSF resources (CSS/JS/Images/etc)
            res.setHeader("Cache-Control",
                    "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            res.setDateHeader("Expires", 0); // Proxies.
        }
        chain.doFilter(request, response);
    }


}