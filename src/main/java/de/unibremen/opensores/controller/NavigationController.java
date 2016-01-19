package de.unibremen.opensores.controller;

import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;

@ManagedBean
@SessionScoped
public class NavigationController {
    private Locale userLocale = Locale.GERMAN;

    private static Map<String, Locale> languages;
    static {
        languages = new LinkedHashMap<> ();
        languages.put("Deutsch", Locale.GERMAN);
        languages.put("English", Locale.ENGLISH);
    }

    public List<String> getLanguages() {
        return new ArrayList<>(languages.keySet());
    }

    public Locale getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String l) {
        System.out.println("Language setting to " + l);
        Locale lang = languages.get(l);

        if (lang == null) {
            return;
        }

        FacesContext
                .getCurrentInstance()
                .getViewRoot()
                .setLocale(lang);

        this.userLocale = lang;
        System.out.println("Language set to " + l);
    }

    public User getUser() {
        return (User)FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("user");
    }
}