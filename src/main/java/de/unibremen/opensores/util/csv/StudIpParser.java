package de.unibremen.opensores.util.csv;

import com.opencsv.CSVReader;
import de.unibremen.opensores.model.User;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class parses StudIp Course Export CSV Files to User Objects.

 * @author Kevin Scheck
 */
public final class StudIpParser {

    /**
     * Valid Email from libAwesome. Also used in registration
     */
    private static final String VALID_MAIL_REGEX
           = "[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\."
               + "[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]";
    /**
     * The content type of a csv file.
     */
    private static final String CSV_CONTENT_TYPE = "text/csv";

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
     * Colum index of the email address of the csv file.
     */
    private static final int COL_EMAIL = 7;

    /**
     * The encoding of the supplied stud ip export file.
     */
    private static final String LATIN_ONE_ENCODING = "ISO-8859-1";

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
     * @throws IllegalArgumentException If the Parameter @studIpCSVFile is not
     *         a valid File object, or is not a .csv-File
     *         (the file content type should equal).
     */
    public static List<User> parseCSV(final File studIpCSVFile)
            throws IOException {
        String contentType = (studIpCSVFile == null) ? null :
            Files.probeContentType(studIpCSVFile.toPath());

        if (contentType == null || !contentType.equals(CSV_CONTENT_TYPE)) {
            throw new IllegalArgumentException(
                    "The parameter studIpCSVFile must be a valid CSV File");
        }

        final CSVReader reader = new CSVReader(new InputStreamReader(
                new FileInputStream(studIpCSVFile.getPath()),
                LATIN_ONE_ENCODING), SEPARATOR, QUOTE_CHAR, SKIPPED_LINES);
        final List<String[]> rows =  reader.readAll();

        final List<User> users = new ArrayList<>(rows.size());

        for (String[] row: rows) {
            if (row == null || row.length != NUM_COLS) {
                reader.close();
                throw new IOException("One row of the csv file doesn't have "
                        + "eleven columns");
            }
            final String firstName = row[COL_FIRST_NAME];
            final String lastName = row[COL_LAST_NAME];
            final String email = row[COL_EMAIL];

            if (!validName(firstName) || !validName(lastName)
                    || !validEmail(email)) {
                reader.close();
                throw new IOException(
                        "The name or the email address is not valid");
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
     * @TODO Discuss Email Validation with others
     */
    private static boolean validEmail(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(VALID_MAIL_REGEX, email);
    }
}
