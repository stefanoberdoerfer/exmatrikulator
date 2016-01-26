package de.unibremen.opensores.util.csv;

import de.unibremen.opensores.model.User;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for the StudIp CSV Parser Class StudIpParser.
 * @author Kevin Scheck
 */
public class StudIpParserTest {

    /**
     * The file path to the stud ip file which got supplied in SWP2.
     */
    private static final String PROVIDED_STUDIP_CSV_PATH
            = "src/test/resources/csv/studip/authors_swp2_uebungen.csv";

    /**
     * The file path to a stud ip file with wrong format.
     */
    private static final String WRONG_FORMAT_CSV_PATH
            = "src/test/resources/csv/studip/wrong_format_studip.csv";

    /**
     * The file path for a .txt file with right format.
     */
    private static final String NO_CSV_FILE_PATH
            = "src/test/resources/csv/studip/no_csv_file.txt";

    /**
     * The filep path for a .csv file in which one of the rows have
     */
    private static final String WRONG_EMAIL_PATH
            = "src/test/resources/csv/studip/wrong_email.csv";

    /**
     * The filep path for a .csv file in which one of the rows have
     */
    private static final String WRONG_NAME_PATH
            = "src/test/resources/csv/studip/wrong_name.csv";

    /**
     * The stud ip export file which got supplied in swp2.
     */
    private File providedStudIpFile;

    /**
     * The stud ip export file which is not a csv file.
     */
    private File noCSVContentTypeFile;

    /**
     * The stud ip export file which has not the right format.
     */
    private File wrongFormatFile;

    /**
     * A stud ip export file in which one row has a wrong email address.
     */
    private File wrongEmailFile;

    /**
     * A stud ip export file in which one row has a wrong name.
     */
    private File wrongNameFile;

    /**
     * Loads all supplied Stud ip export csv files.
     */
    @Before
    public void loadFiles() {
        providedStudIpFile = new File(PROVIDED_STUDIP_CSV_PATH);
        noCSVContentTypeFile = new File(NO_CSV_FILE_PATH);
        wrongFormatFile = new File(WRONG_FORMAT_CSV_PATH);
        wrongEmailFile = new File(WRONG_EMAIL_PATH);
        wrongNameFile = new File(WRONG_NAME_PATH);
    }


    /**
     * Tests if all users of the supplied example stud ip export file gets
     * returned as a list of users.
     * Expects the returned users only to have a first Name, last name and email
     * address.
     */
    @Test
    public void testProvidedStudIpFile() {
        List<User> expectedUsers = new ArrayList<>();

        User rainer = new User();
        rainer.setFirstName("Rainer");
        rainer.setLastName("Koschke");
        rainer.setEmail("koschke@uni-bremen.de");
        expectedUsers.add(rainer);

        User amadou = new User();
        amadou.setFirstName("Amadou");
        amadou.setLastName("Amadou");
        amadou.setEmail("amadou@uni-bremen.de");
        expectedUsers.add(amadou);

        User karsten = new User();
        karsten.setFirstName("Karsten");
        karsten.setLastName("Hölscher");
        karsten.setEmail("hoelsch@uni-bremen.de");
        expectedUsers.add(karsten);

        User sebastian = new User();
        sebastian.setFirstName("Sebastian");
        sebastian.setLastName("Offermann");
        sebastian.setEmail("se_of@uni-bremen.de");
        expectedUsers.add(sebastian);

        User hui = new User();
        hui.setFirstName("Hui");
        hui.setLastName("Shi");
        hui.setEmail("shihui@uni-bremen.de");
        expectedUsers.add(hui);

        User marcel = new User();
        marcel.setFirstName("Marcel");
        marcel.setLastName("Steinbeck");
        marcel.setEmail("s_cychgf@uni-bremen.de");
        expectedUsers.add(marcel);

        User max = new User();
        max.setFirstName("Max");
        max.setLastName("Mustermann");
        max.setEmail("maxmus@uni-bremen.de");
        expectedUsers.add(max);

        User martina = new User();
        martina.setFirstName("Martina");
        martina.setLastName("Musterfrau");
        martina.setEmail("marmus@uni-bremen.de");
        expectedUsers.add(martina);


        List<User> actualUsers = null;
        try {
            actualUsers = StudIpParser.parseCSV(providedStudIpFile);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        Iterator<User> actualUserIterator = actualUsers.iterator();
        for(final User expectedUser: expectedUsers) {
            final User actualUser = actualUserIterator.next();
            assertEquals(expectedUser.getFirstName(),
                    actualUser.getFirstName());
            assertEquals(expectedUser.getLastName(),
                    actualUser.getLastName());
            assertEquals(expectedUser.getEmail(),
                    actualUser.getEmail());
            assertNull(actualUser.getPassword());
            assertNull(actualUser.getProfileInfo());
            assertNull(actualUser.getPassword());
            assertNull(actualUser.getUserId());
        }
        assertFalse(actualUserIterator.hasNext());
    }


    /**
     * Tests if an IllegalArgumentException gets thrown if null gets passed
     * as parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseCSVNullParameter() {
        try {
            StudIpParser.parseCSV(null);
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }


    /**
     * Tests if an IllegalArgumentException gets thrown if a non-csv file
     * gets passed as parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNoCSVFileParameter() {
        try {
            StudIpParser.parseCSV(noCSVContentTypeFile);
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }


    /**
     * Tests if an IOException gets thrown if the csv file has the wrong format.
     */
    @Test(expected = IOException.class)
    public void testWrongFormatFile() throws Exception {
        StudIpParser.parseCSV(wrongFormatFile);
    }


    /**
     * Tests if an IOException gets thrown if a user in the csv file has the
     * wrong name.
     */
    @Test(expected = IOException.class)
    public void testWrongNameFile() throws Exception {
        StudIpParser.parseCSV(wrongNameFile);
    }


    /**
     * Tests if an IOException gets thrown if a user in the csv file has a wrong
     * email address.
     */
    @Test(expected = IOException.class)
    public void testWrongEmailFile() throws Exception {
        StudIpParser.parseCSV(wrongEmailFile);
    }

}
