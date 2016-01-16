package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.ServiceException;
import de.unibremen.opensores.service.UserService;
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
public class RegistrationController {

    @EJB
    private UserService userService;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    public String register() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (userService.isEmailRegistered(email)) {
            ResourceBundle bundle =
                    ResourceBundle.getBundle("messages",
                            facesContext.getViewRoot().getLocale());
            String text = bundle.getString("registration.alreadyRegistered");
            facesContext.addMessage("", new FacesMessage(text));
            return "#";
        }

        final String hashPW = BCrypt.hashpw(password, BCrypt.gensalt());

        User registeredUser = new User();
        registeredUser.setEmail(email);
        registeredUser.setPassword(password);
        registeredUser.setFirstName(firstName);
        registeredUser.setLastName(lastName);
        //TODO Change language by radio buttons etc.
        registeredUser.setLanguage("de");
        registeredUser.getRoles().add(Role.USER);

        userService.persist(registeredUser);

        //TODO Zu Login mit Erf
        return "login.xhtml";
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
