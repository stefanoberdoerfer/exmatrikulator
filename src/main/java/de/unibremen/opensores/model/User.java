package de.unibremen.opensores.model;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity bean of the User class
 * @TODO Discuss Feedback from tutor
 * @TODO Discuss max. field length (e.g. firstName)
 * @TODO Add Activivation & Password-Recovery-Keys
 */
@Entity
@Table(name = "USER_TABLE")
public class User {

    @Id
    @GeneratedValue
    private Long userId;

    @Column(name = "email", unique = true, nullable = false, length = 64)
    private String email;

    @Column(name = "password", nullable = false, length = 256)
    private String password;

    @Column(name = "first_name", nullable = false, length = 32)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 32)
    private String lastName;

    @Column(name = "profile_info", nullable = true, length = 2048)
    private String profileInfo;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "is_lecturer", nullable = false)
    private Boolean isLecturer = false;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Role.class)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<Role> roles = new ArrayList<>();


    public boolean hasRole(String roleString) {
        return roles.contains(Role.valueOf(roleString));
    }


    /*
     * GETTERS AND SETTERS
     */

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

    public String getProfileInfo() {
        return profileInfo;
    }

    public void setProfileInfo(String profileInfo) {
        this.profileInfo = profileInfo;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isLecturer() {
        return isLecturer;
    }

    public void setLecturer(boolean lecturer) {
        isLecturer = lecturer;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
