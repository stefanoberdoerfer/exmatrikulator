package de.unibremen.opensores.util.csv;

import com.opencsv.CSVReader;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class parses StudIp Course Export CSV Files to User Objects.

 * @author Kevin Scheck
 */
public final class StudIpParser {


    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(StudIpParser.class);


    /**
     * The different content type of a csv file.
     * text/csv is standard under RCF7111: http://tools.ietf.org/html/rfc7111
     * but: there is also:
     *  * http://mimeapplication.net/vnd-ms-excel
     *  * http://mimeapplication.net/csv
     * The parser crashes if it's used in Windows when these types are not used.
     */
    private static final String[] CSV_CONTENT_TYPES
            = {"text/csv","application/csv","application/vnd.ms-excel"};

    /**
     * The separator used in studIp course csv files.
     */
    private static final char SEPARATOR = ';';

    /**
     * The quote char used in studIp course csv files.
     */
    private static final char QUOTE_CHAR = '\"';

    /**
     * The number of skipped lines by the parser.
     * StudIp csv files have one line
     */
    private static final int SKIPPED_LINES = 1;

    /**
     * The number of columns of a studIp course csv file.
     */
    private static final int NUM_COLS = 11;

    /**
     * Column index of the first name of the csv file.
     */
    private static final int COL_FIRST_NAME = 1;

    /**
     * Column index of the last name of the csv file.
     */
    private static final int COL_LAST_NAME = 2;

    /**
     * Column index of the email address of the csv file.
     */
    private static final int COL_EMAIL = 7;

    /**
     * The encoding of the supplied stud ip export file.
     */
    private static final String LATIN_ONE_ENCODING = "ISO-8859-1";

    /**
     * Empty Constructor for no object creation.
     */
    private StudIpParser() {}

    /**
     * Parses a Stud IP course export csv file and returns a list of Users with
     * the first name, last name and emails for each row of the file.
     * @param studIpCSVFile The stud ip CSV File. It shouldnt be altered after
     *                      downloading from stud ip.
     * @return A List of users with the first name, last name and email fields
     *         of the CSV File. All users are not registered in the database nor
     *         have any other fields, nor interact as objects in any way with
     *         the system.
     * @throws IOException If the file can't be opened,
     *         the file doesn't havethe same structure as a normal stud ip
     *         course export file, or the names or emails are not valid.
     * @throws IOException If the Parameter @studIpCSVFile is null,
     *         or if parsing goes wrong
     */
    public static List<User> parseCSV(final File studIpCSVFile)
            throws IOException {

        if (studIpCSVFile == null) {
            throw new IOException(
                    "The parameter studIpCSVFile must be a valid CSV File");
        }

        final CSVReader reader = new CSVReader(new InputStreamReader(
                new FileInputStream(studIpCSVFile.getPath()),
                LATIN_ONE_ENCODING), SEPARATOR, QUOTE_CHAR, SKIPPED_LINES);
        final List<String[]> rows =  reader.readAll();

        final List<User> users = new ArrayList<>(rows.size());

        for (String[] row: rows) {
            if (row == null || row.length != NUM_COLS) {
                log.error("Skipping row, the row is either null "
                         + "or  its length is not as expected");
                continue;
            }
            final String firstName = row[COL_FIRST_NAME];
            final String lastName = row[COL_LAST_NAME];
            final String email = row[COL_EMAIL];

            if (!validName(firstName) || !validName(lastName)
                    || !validEmail(email)) {
                log.error(String.format("The skippimg row,"
                       + "the first name %s, last name %s or email %s is not valid",
                        firstName, lastName, email));
                continue;
            }

            final User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            users.add(user);
        }

        return users;
    }

    /**
     * Checks if a String is a valid name.
     * A name is valid, if it consists of at least one character,
     * and all characters are nut numeric. We don't want to restrict the name
     * specifications too much(e.g. the first Name is uppercase, all characters
     * are only alphabetic, whitespace or dashes), because we the stud ip
     * specifications can change over time.
     * @param name The name to be checked.
     * @return True if the string is a valid name, false otherwise.
     */
    private static boolean validName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        //Only checking for numbers, because the Stud ip names may vary in the
        //future and we don't want incompatibility because of too strict
        //regulations
        for (char character: name.toCharArray()) {
            if (Character.isDigit(character)) {
                return false;
            }
        }
        return true;
    }

    /**
     * WIP Checks if a string is valid email for registration in
     * the Exmatrikulator. A valid email address must be registered in one of
     * the participating colleges of the Exmatrikulator.
     * @param email The string to be checked if it's a valid email address.
     * @return True if the email String is valid, false otherwise.
     */
    private static boolean validEmail(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(Constants.EMAIL_REGEX, email);
    }

    /**
     * Checks if the content type is in the array of given contentt types.
     * @param type The content type string to be checked
     * @return Returns true if the content type equals one of the specified
     *         content types.
     */
    private static boolean validContentType(final String type) {
        for (String validContentType: CSV_CONTENT_TYPES) {
            if (validContentType.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
