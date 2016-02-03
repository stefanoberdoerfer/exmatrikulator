package de.unibremen.opensores.util.mail;

import java.util.Arrays;

/**
 * MailJob Class for creating mail jobs to be processed by
 * the Mail Class.
 *
 * @author Lorenz Hüther
 * @author Sören Tempel
 */
public class MailJob {
    /**
     * Addresses addressed by this email.
     */
    private String[] addresses = null;

    /**
     * Subject for this email.
     */
    private String subject = null;

    /**
     * Body for this email.
     */
    private String text = null;

    /**
     * Consturctor for the MailJob class.
     *
     * @param addresses Array of E-Mail addresses.
     * @param subject Mail subject.
     * @param text Body of the mail.
     */
    public MailJob(String[] addresses, String subject, String text) {
        this.addresses = Arrays.copyOf(addresses, addresses.length);
        this.subject = subject;
        this.text = text;
    }

    /**
     * Getter for address.
     *
     * @return String Array of addresses.
     */
    public String[] getAddresses() {
        return Arrays.copyOf(addresses, addresses.length);
    }

    /**
     * Getter for subject.
     *
     * @return String subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Getter for the body.
     *
     * @return String body.
     */
    public String getText() {
        return text;
    }
}
