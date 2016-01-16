package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.ServiceException;
import de.unibremen.opensores.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

/**
 * Created by kevin on 14.01.16.
 */
@ManagedBean
@RequestScoped
public class LoginController {

    @EJB
    private UserService userService;

    private String email;

    private String password;


    public String login(){
        final String hashPW = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = null;
        try {
            user = userService.findByLogin(email, hashPW);
            if(user == null){
                FacesContext facesContext = FacesContext.getCurrentInstance();
                FacesMessage facesMessage = new FacesMessage("The credentials are wrong.");
                facesContext.addMessage(null, facesMessage);
                return "#";
            } else {
                //TODO Add user in SessionMap
                FacesContext facesContext = FacesContext.getCurrentInstance();
                FacesMessage facesMessage = new FacesMessage("You are registered, woohoo.");
                facesContext.addMessage(null, facesMessage);
                return "#";
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
