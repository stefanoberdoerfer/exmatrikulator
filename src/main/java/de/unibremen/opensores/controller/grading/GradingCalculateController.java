package de.unibremen.opensores.controller.grading;

import de.unibremen.opensores.model.*;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

/**
 * @author Matthias Reichmann
 */
@ManagedBean
@ViewScoped
public class GradingCalculateController {
    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            GradingCalculateController.class);

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * UserService for database transactions related to users.
     */
    @EJB
    private UserService userService;

    /**
     * Calculates the final grades for the given course.
     * @param course Course which final grades shall be calculated
     */
    public void calculateFinalGrades(Course course) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                facesContext.getViewRoot().getLocale());
        /*
        Load the user
         */
        User user = (User)FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("user");
        /*
        Check if the user is a lecturer
         */
        if (!userService.hasCourseRole(user, "LECTURER", course)) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                    .SEVERITY_FATAL, bundle.getString("common.error"),
                    bundle.getString("common.noAccess")));
            return;
        }
        /*
        Todo: Calculate the final grades
         */
        /*
        Success
         */
        facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_INFO, bundle.getString("common.success"),
                bundle.getString("gradings.calculated")));
    }
}
