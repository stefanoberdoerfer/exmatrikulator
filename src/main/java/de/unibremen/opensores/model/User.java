package de.unibremen.opensores.model;

import de.unibremen.opensores.util.mail.Mail;
import de.unibremen.opensores.util.mail.MailJob;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.CollectionTable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.mail.internet.AddressException;
import javax.mail.MessagingException;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * Entity bean of the User class.
 * @TODO Discuss max. field length (e.g. firstName)
 */
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue
    private Long userId;

    @Column(unique = true, nullable = false, length = 64)
    private String email;

    @Column(columnDefinition = "LONG VARCHAR", nullable = true)
    private String password;

    @Column(nullable = false, length = 32)
    private String firstName;

    @Column(nullable = false, length = 32)
    private String lastName;

    @Column(nullable = true, length = 2048)
    private String profileInfo;

    @Column(nullable = true)
    private String language;

    @Column(nullable = false)
    private Boolean isBlocked = false;

    @Column(nullable = true)
    private String matriculationNumber;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Integer.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "userId"))
    @Column
    private List<Integer> roles = new ArrayList<>();

    @OneToOne(optional = true, mappedBy = "user", cascade = CascadeType.ALL,
            targetEntity = PasswordReset.class, orphanRemoval = true)
    private PasswordReset resetToken;

    /**
     * Send an email to this user.
     *
     * @param subject Subject of the email.
     * @param text Body of the email.
     */
    public void sendEmail(String subject, String text) throws AddressException,
           MessagingException, IOException {
        MailJob mj = new MailJob(new String[] {email}, subject, text);
        new Mail().issue(new MailJob[] {mj});
    }

    /**
     * Returns true if the user has the given global role.
     *
     * @param role Role the user should have.
     * @return True if he has the given role, false otherwise.
     */
    public boolean hasGlobalRole(GlobalRole role) {
        return roles.contains(role.getId());
    }

    /**
     * Returns true if the user has the given global role.
     *
     * @param roleStr Role (as a string) the user should have.
     * @return True if he has the given role, false otherwise.
     */
    public boolean hasGlobalRole(String roleStr) {
        return hasGlobalRole(GlobalRole.valueOf(roleStr));
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

    public void addRole(GlobalRole role) {
        roles.add(role.getId());
    }

    public void setRoles(List<Integer> roles) {
        this.roles = roles;
    }

    public List<Integer> getRoles() {
        return roles;
    }

    public PasswordReset getToken() {
        return resetToken;
    }

    public void setToken(PasswordReset resetToken) {
        this.resetToken = resetToken;
    }

    @Override
    public int hashCode() {
        if (this.userId != null) {
            return userId.hashCode() + User.class.hashCode();

        } else {
            return super.hashCode();

        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this.userId != null
                && obj instanceof User
                && ((User) obj).getUserId() != null) {
            return this.userId.equals(((User)obj).userId);
        } else {
            return (obj == this);
        }
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

}
