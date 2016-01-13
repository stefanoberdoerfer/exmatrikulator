package de.unibremen.opensores.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class LoginController {

    private static Logger log = LogManager.getLogger(LoginController.class);

    private String email;
    private String password;

    public String login() {
        log.error("login called");
        if(validate()){
            log.debug("login succeeded");
            FacesMessage msg = new FacesMessage("Login success");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return "/index.xhtml";
        }else{
            log.debug("login failed");
            FacesMessage msg = new FacesMessage("Login failed");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return "/index.xhtml?faces-redirect=true";
        }
    }

    private boolean validate() {
        return (email == null || password == null);
    }

    /* Getters and Setters */

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
