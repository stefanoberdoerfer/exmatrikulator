package main.java.de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by matthias on 19.01.16.
 */
@ManagedBean
@ViewScoped
public class GradingController {
    /**
     * The path to the course overview site.
     */
    private static final String PATH_TO_COURSE_OVERVIEW =
            "/course/overview.xhtml?faces-redirect=true";

    /**
     * The http parameter key by which the course id gets passed.
     */
    private static final String HTTP_PARAM_COURSE_ID = "course-id";

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(GradingController.class);

    /**
     * The course which exams get edited.
     */
    private Course course;
    /**
     * Method called after initialisation.
     * Gets the corresponding course from the http param.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");

        HttpServletRequest httpReq
                = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        log.debug("Request URI: " + httpReq.getRequestURI());
        final String courseIdString = httpReq.getParameter(HTTP_PARAM_COURSE_ID);

        log.debug("course-id: " + courseIdString);
        long courseId = -1;
        if (courseIdString != null) {
            try {
                courseId = Long.parseLong(courseIdString.trim());
            } catch (NumberFormatException e) {
                log.debug("NumberFormatException while parsing courseId");
            }
        }

        if (courseId != -1) {
            course = courseService.findById(courseId);
        }

        log.debug("Loaded course object: " + course);

        if (course == null) {
            log.debug("trying to redirect to /course/overview");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + PATH_TO_COURSE_OVERVIEW);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                log.fatal("Could not redirect to " + PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }
        log.debug("Course exam list size: " + course.getExams().size());

        gradeTypeLabels = new HashMap<>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());

        gradeTypeLabels.put(GradeType.Numeric.getId(), bundle.getString("gradeType.grade"));
        gradeTypeLabels.put(GradeType.Point.getId(), bundle.getString("gradeType.points"));
        gradeTypeLabels.put(GradeType.Boolean.getId(), bundle.getString("gradeType.boolean"));
        gradeTypeLabels.put(GradeType.Percent.getId(), bundle.getString("gradeType.percent"));
    }
}
