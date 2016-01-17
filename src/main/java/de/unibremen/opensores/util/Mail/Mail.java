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

/**
* Util used for sending mails.
*
* @author Lorenz Huether
*/
public final class Mail {

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
    *
    * @return boolean true on success.
    *
    * @throws AddressException if something with the adress is wrong.
    *
    * @throws MessagingException if the mail cannot be sent.
    */
    public boolean issue(final MailJob[] jobs) throws AddressException,
            MessagingException {
        String user = props.getProperty("de.unibremen.opensores.mail.user");
        String pass = props.getProperty("de.unibremen.opensores.mail.pass");
        String from = props.getProperty("de.unibremen.opensores.mail.from");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                }
        );

        for (MailJob j : jobs) {
            String[] mail = j.getAddresses();
            Address[] addr = new Address[mail.length];
            for (int i = 0; i < mail.length; i++) {
                addr[i] = InternetAddress.parse(mail[i])[0];
            }

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
        return true;
    }
}
