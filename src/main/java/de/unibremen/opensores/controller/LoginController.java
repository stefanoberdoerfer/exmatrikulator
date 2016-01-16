package de.unibremen.opensores.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 * Provisional LoginController to show usage of log4j logging.
 */

@ManagedBean
@SessionScoped
public class LoginController {
    /**
     * @todo.
     */
    private static Logger log = LogManager.getLogger(LoginController.class);

    /**
     * @todo.
     */
    private String email;

    /**
     * @todo.
     */
    private String password;

    /**
     * @todo.
     */
    public final String login() {
        log.error("login called");
        if (validate()) {
            log.debug("login succeeded");
            FacesMessage msg = new FacesMessage("Login success");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return "/index.xhtml";
        } else {
            log.debug("login failed");
            FacesMessage msg = new FacesMessage("Login failed");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return "/index.xhtml?faces-redirect=true";
        }
    }

    /**
     * @todo.
     */
    private boolean validate() {
        return (email == null || password == null);
    }

    /**
     * @todo.
     */
    public final String getEmail() {
        return email;
    }

    /**
     * @todo.
     */
    public final void setEmail(final String mail) {
        this.email = mail;
    }

    /**
     * @todo.
     */
    public final String getPassword() {
        return password;
    }

    /**
     * @todo.
     */
    public final void setPassword(final String pw) {
        this.password = pw;
    }
}
