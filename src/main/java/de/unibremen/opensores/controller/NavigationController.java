package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ManagedBean(name = "navigation")
@SessionScoped
public class NavigationController {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(NavigationController.class);

    /**
     * Current user locale.
     */
    private Locale userLocale;

    /**
     * UserService instance.
     */
    @EJB
    private UserService userService;

    /**
     * Map containing supported locales.
     */
    private static Map<String, Locale> languages;

    /**
     * @todo This is retarded, locales should be fetched from faces-config.xml.
     */
    static {
        languages = new LinkedHashMap<>();
        languages.put("Deutsch", Locale.GERMAN);
        languages.put("English", Locale.ENGLISH);
    }

    /**
     * Creates a new NavigationController instance.
     */
    public NavigationController() {
        this.userLocale = FacesContext
                .getCurrentInstance()
                .getViewRoot()
                .getLocale();
    }

    /**
     * Used to redirect to course-specific views.
     */
    public String viewCourse(Course course, String view) {
        String viewPath =  view + "?course-id=" + course.getCourseId() ;
        log.debug("viewCourse() called: " + viewPath);
        return viewPath;
    }

    /**
     * Returns a list of supported locales.
     *
     * @return List of supported locales.
     */
    public List<String> getLanguages() {
        return new ArrayList<>(languages.keySet());
    }

    /**
     * Returns the locale for the current user.
     *
     * @return Locale used by the current user.
     */
    public Locale getUserLocale() {
        return userLocale;
    }

    /**
     * Changes the locale for the logged in user.
     *
     * @param locale Locale to use.
     */
    public void setUserLocale(String locale) {
        Locale lang = languages.get(locale);
        if (lang == null) {
            return;
        }

        FacesContext.getCurrentInstance()
                .getViewRoot()
                .setLocale(lang);

        this.userLocale = lang;
    }

    /**
     * Returns the user that is currently logged in.
     *
     * @return Currently logged in user.
     */
    public User getUser() {
        return (User)FacesContext
            .getCurrentInstance()
            .getExternalContext()
            .getSessionMap()
            .get("user");
    }

    /**
     * Whether the given FacesMessage is an error message.
     *
     * @return True if it is, false otherwise.
     */
    public boolean isError(FacesMessage message) {
        return message.getSeverity().equals(FacesMessage.SEVERITY_ERROR)
                || message.getSeverity().equals(FacesMessage.SEVERITY_FATAL);
    }

    /**
     * Whether the given FacesMessage is a warning message.
     *
     * @return True if it is, false otherwise.
     */
    public boolean isWarning(FacesMessage message) {
        return message.getSeverity().equals(FacesMessage.SEVERITY_WARN);
    }

    /**
     * Whether the given FacesMessage is a success message.
     *
     * @return True if it is, false otherwise.
     */
    public boolean isSuccess(FacesMessage message) {
        return message.getSeverity().equals(FacesMessage.SEVERITY_INFO);
    }
}
