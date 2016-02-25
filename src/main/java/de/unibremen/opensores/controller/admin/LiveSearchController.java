package de.unibremen.opensores.controller.admin;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The Backing Bean of multiple live search widgets across the system.
 *
 * @author Stefan Oberdörfer
 */
@ManagedBean
@ViewScoped
public class LiveSearchController {

    private static Logger log = LogManager.getLogger(LiveSearchController.class);

    /**
     * The UserService for listing all users.
     */
    private UserService userService;

    private List<User> userResultList;

    private String searchString;

    private User selectedUser;

    private List<User> selectedUsers;

    /**
     * Initialises the controller by getting the currently logged in User from
     * the session and adding him to the resultList.
     */
    @PostConstruct
    public void init() {
        selectedUsers = new ArrayList<>();
    }

    /**
     * Method invoked by the view on every string change event.
     * @param event Event fired by the view on every keyUp
     */
    public void searchForUsers(AjaxBehaviorEvent event){
        log.debug("searchForUsers: " + searchString);
        if (searchString.length() >= Constants.LIVE_SEARCH_THRESHOLD) {
            userResultList = userService.searchForUsers(searchString);
        }
    }

    public void selectUser(User user) {
        selectedUser = user;
        userResultList.clear();
    }

    /**
     * Add a user to the selection list.
     * @param user User to be added
     */
    public void addUser(User user) {
        if (!selectedUsers.contains(user)) {
            selectedUsers.add(user);
            userResultList.remove(user);
        }
    }

    /**
     * Remove a user from the selection list.
     * @param user User to be removed
     */
    public void removeUser(User user) {
        selectedUsers.remove(user);
    }

    /**
     * Injects the user service.
     * @param userService The user service to be injected.
     */
    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public List<User> getUserResultList() {
        return userResultList;
    }

    public void setUserResultList(List<User> userResultList) {
        this.userResultList = userResultList;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List<User> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<User> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }
}