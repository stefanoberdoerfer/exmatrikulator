package de.unibremen.opensores.util.mail;

import de.unibremen.opensores.util.ServerProperties;

import java.io.IOException;
import java.util.Properties;
import java.util.Date;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import javax.mail.PasswordAuthentication;
import java.util.StringJoiner;

/**
 * Util used for sending mails.
 *
 * @author Lorenz Hüther
 */
public final class Mail {
    /**
     * Properties file.
     */
    private Properties props = null;

    /**
     * Loads the properties file.
     *
     * @throws IOException Should the config file cannot be opened.
     */
    public Mail() throws IOException {
        props = ServerProperties.getProperties();
    }

    /**
     * Issues mails, supporting starttls only at the moment,
     * uses BCC should there be more than one recipient.
     *
     * @param jobs list for the mail jobs.
     * @throws AddressException if something with the adress is wrong.
     * @throws MessagingException if the mail cannot be sent.
     */
    public void issue(final MailJob[] jobs) throws AddressException,
            MessagingException {
        String user = props.getProperty("exmatrikulator.mail.user");
        String pass = props.getProperty("exmatrikulator.mail.pass");
        String from = props.getProperty("exmatrikulator.mail.from");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                }
        );

        for (MailJob j : jobs) {
            StringJoiner sj = new StringJoiner(",");
            for (String mail : j.getAddresses()) {
                sj.add(mail);
            }

            Address[] addr = InternetAddress.parse(sj.toString());
            MimeMessage msg = new MimeMessage(session);

            if (addr.length > 1) {
                msg.setRecipients(javax.mail.Message.RecipientType.BCC,
                        addr);
            } else {
                msg.setRecipients(javax.mail.Message.RecipientType.TO,
                        addr);
            }

            msg.setSubject(j.getSubject());
            msg.setText(j.getText());
            msg.setFrom(new InternetAddress(from));
            msg.setSentDate(new Date());
            Transport.send(msg);
        }
    }
}
