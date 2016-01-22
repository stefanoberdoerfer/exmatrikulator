package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;

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
    private Locale userLocale;

    @EJB
    private UserService userService;

    private static Map<String, Locale> languages;

    static {
        languages = new LinkedHashMap<>();
        languages.put("Deutsch", Locale.GERMAN);
        languages.put("English", Locale.ENGLISH);
    }


    /**
     * @todo.
     */
    public NavigationController() {
        this.userLocale = FacesContext
                .getCurrentInstance()
                .getViewRoot()
                .getLocale();
    }

    public List<String> getLanguages() {
        return new ArrayList<>(languages.keySet());
    }

    public Locale getUserLocale() {
        return userLocale;
    }

    /**
     * @todo.
     * @param locale .
     */
    public void setUserLocale(String locale) {
        System.out.println("Language setting to " + locale);
        Locale lang = languages.get(locale);

        if (lang == null) {
            return;
        }

        FacesContext
                .getCurrentInstance()
                .getViewRoot()
                .setLocale(lang);

        this.userLocale = lang;
        System.out.println("Language set to " + locale);
    }

    /**
     * @todo.
     * @return.
     */
    public User getUser() {
        return (User)FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("user");
    }

    public boolean isError(FacesMessage message) {
        return  message.getSeverity().equals(FacesMessage.SEVERITY_ERROR)
                || message.getSeverity().equals(FacesMessage.SEVERITY_FATAL);
    }

    public boolean isWarning(FacesMessage message) {
        return  message.getSeverity().equals(FacesMessage.SEVERITY_WARN);
    }

    public boolean isSuccess(FacesMessage message) {
        return  message.getSeverity().equals(FacesMessage.SEVERITY_INFO);
    }
}