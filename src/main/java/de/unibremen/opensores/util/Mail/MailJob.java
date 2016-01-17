package de.unibremen.opensores.util.mail;

/**
 * MailJob Class for creating mail jobs to be processed by
 * the Mail Class.
 *
 * @author Lorenz Huether
 */
public class MailJob {
    private String[] address = null;
    private String subject = null;
    private String text = null;

    /**
    * Consturctor for the MailJob class.
    *
    * @param address String Array of E-Mail addresses.
    * @param subject String mail subject.
    * @param text String body of the mail.
    *
    */
    public MailJob(String[] address, String subject, String text) {
        this.address = address;
        this.subject = subject;
        this.text = text;
    }

    /**
    * Getter for address.
    *
    * @return String Array of addresses.
    */
    public String[] getAddresses() {
        return address;
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
