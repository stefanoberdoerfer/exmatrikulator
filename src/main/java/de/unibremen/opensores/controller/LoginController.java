package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.ServiceException;
import de.unibremen.opensores.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

/**
 * Created by kevin on 14.01.16.
 */
@ManagedBean
@RequestScoped
public class LoginController {

    private static Logger log = LogManager.getLogger(LoginController.class);

    @EJB
    private UserService userService;

    private String email;

    private String password;

    /**
     * Login method which tries to lookup user with given email and password.
     * @return redirection link if user was successfully logged in. '#' if not.
     */
    public String login() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        User user = null;
        try {
            user = userService.findByLogin(email, password);
            if (user == null) {
                log.debug("login fail");
                ResourceBundle bundle =
                        ResourceBundle.getBundle("messages",
                                facesContext.getViewRoot().getLocale());
                String text = bundle.getString("login.fail");
                facesContext.addMessage("", new FacesMessage(text));
                return "#";
            } else {
                log.debug("login success");
                FacesMessage facesMessage = new FacesMessage(//user.getRoles().get(0).toString() + " " +
                        user.getFirstName() + " you are registered, woohoo.");
                facesContext.addMessage(null, facesMessage);
                FacesContext.getCurrentInstance()
                        .getExternalContext().getSessionMap().put("user", user);
                return "index.xhtml";
            }
        } catch (ServiceException e) {
            e.printStackTrace();
            return "#";
        }
    }

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
