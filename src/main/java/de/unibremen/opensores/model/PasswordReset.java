package de.unibremen.opensores.model;

import java.util.Calendar;
import java.util.Date;
import java.sql.Time;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Password reset entity.
 *
 * @author Sören Tempel
 */
@Entity
@Table(name = "PASSWORD_RESET_TABLE")
public class PasswordReset {
    @Id
    @GeneratedValue
    private Long resetId;

    @Column(unique = false, nullable = false, length = 64)
    private String token;

    @Column(unique = false)
    private Time expires;

    @OneToOne(optional = false)
    @JoinColumn(name = "userId")
    private User user;

    /**
     * Whether or not the password reset has expired.
     *
     * @return True if it has expired, false otherwise.
     */
    public boolean hasExpired() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();

        Time time = new Time(now.getTime());
        return expires.after(time);
    }

    /**
     * Change the expire timestamp for this password reset.
     *
     * @param timestamp Expire timestamp to use.
     */
    public void setExpires(Time expires) {
        this.expires = expires;
    }

    /**
     * Return the token used for this password reset.
     *
     * @return Token used for password reset.
     */
    public String getToken() {
        return token;
    }

    /**
     * Set the token used for this password reset.
     *
     * @param token Token used for password reset./
     */
    public void setToken(final String token) {
        this.token = token;
    }

    /**
     * Return the user used by this password reset request.
     *
     * @return User for this password reset request.
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the user used by this password reset request.
     *
     * @param user User used for this password reset request.
     */
    public void setUser(User user) {
        this.user = user;
    }
}
