package de.unibremen.opensores.util.csv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.unibremen.opensores.model.PaboData;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Student;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Class for parsing PABO CSV Files.
 * @author Kevin Scheck.
 */
public final class PaboParser {

    /**
     * The logger of this Parser.
     */
    private static Logger log = LogManager.getLogger(PaboParser.class);


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
     * The header  string array of the pabo csv file.
     */
    private static final String[] PABO_CSV_FILE_HEADER = { "Matrnr","Nachname",
        "Vorname","Prfdatum","Note","Prfbem","Versuch",
        "Teilprf","Studiengang","","","",""};

    /**
     * The separator used in Pabo course csv files.
     */
    private static final char SEPARATOR = ';';

    /**
     * The quote char used in pabo course csv files.
     */
    private static final char QUOTE_CHAR = CSVWriter.NO_QUOTE_CHARACTER;

    /**
     * The number of skipped lines by the parser.
     * Pabo csv files have one line
     */
    private static final int SKIPPED_LINES = 1;

    /**
     * The number of columns of a PABO csv file.
     */
    private static final int NUM_COLS = 13;

    /**
     * The column index of the matriculation number (indexes start with 0).
     */
    private static final int COL_MATRICULATION = 0;

    /**
     * The column index of the matriculation number (indexes start with 0).
     */
    private static final int COL_LAST_NAME = 1;

    /**
     * The column index of the matriculation number (indexes start with 0).
     */
    private static final int COL_FIRST_NAME = 2;


    /**
     * The column index of the exam date (indexes start with 0).
     */
    private static final int COL_EXAM_DATE = 3;

    /**
     * The column index of the grade (indexes start with 0).
     */
    private static final int COL_GRADE = 4;

    /**
     * The column index of the exam remark (indexes start with 0).
     */
    private static final int COL_EXAM_REMARK = 5;

    /**
     * The column index of the number of tries(indexes start with 0).
     */
    private static final int COL_EXAM_TRIES = 6;

    /**
     * The column index of the exam name(indexes start with 0).
     */
    private static final int COL_EXAM_NAME = 7;

    /**
     * The column index of the matriculation number (indexes start with 0).
     */
    private static final int COL_MAJOR = 8;

    /**
     * The column indexes which are always empty.
     */
    private static final int[] COLS_ALWAYS_EMPTY = {9, 10, 11};

    /**
     * The encoding of the supplied stud ip export file.
     */
    private static final String LATIN_ONE_ENCODING = "ISO-8859-1";



    /**
     * Empty constructor for no object creation.
     */
    private PaboParser() {
        throw new UnsupportedOperationException();
    }


    /**
     * Parses a Pabo CSV import file to an Array of PaboData. The array is the
     * number of rows of the file minus 1. Eeah pabo data object represents a row
     * of the file. If the row of the pabo data is not parsed correctly, a pabo
     * data with an invalid id gets set as object of the array.
     * @see PaboData
     * @param paboCSVFile The csv file which is a csv import.
     * @return An array ob PaboData representing the parsed rows.
     * @throws IOException If the import goes wrong (e.g. the file is not a csv file,
     *         the syntax of the file is wrong).
     */
    public static List<PaboData> parsePaboUpload(File paboCSVFile) throws IOException {
        String contentType = (paboCSVFile == null) ? null :
                Files.probeContentType(paboCSVFile.toPath());

        if (contentType == null || !validContentType(contentType)) {
            throw new IOException("The parameter studIpCSVFile must be a valid CSV File");
        }

        final CSVReader reader = new CSVReader(new InputStreamReader(
                new FileInputStream(paboCSVFile.getPath()),
                LATIN_ONE_ENCODING), SEPARATOR, QUOTE_CHAR, SKIPPED_LINES);
        final List<String[]> rows =  reader.readAll();

        List<PaboData> parsedData = new ArrayList<>(rows.size());

        int rowIdx = 0;
        for (String[] row: rows) {
            if (row == null || row.length != NUM_COLS) {
                if (row != null) {
                    log.error("The length of row " + (rowIdx + 1)
                            + " of the file equals " + row.length);
                }
                parsedData = addInvalidPaboData(PaboData.INVALID_ALL_EMPTY, parsedData);
                rowIdx++;
                continue;
            }


            if (emptyString(row[COL_MATRICULATION])) {
                parsedData = addInvalidPaboData(PaboData.INVALID_MATRICULATION, parsedData);
                rowIdx++;
                continue;
            }

            int attempts = -1;
            try {
                attempts = Integer.parseInt(row[COL_EXAM_TRIES]);
            } catch (NumberFormatException e) {
                // Do nothing, a check for attempts < 0 gets done later.
            }

            if (attempts < 0) {
                parsedData = addInvalidPaboData(PaboData.INVALID_ATTEMPT, parsedData);
                rowIdx++;
                continue;
            }

            if (emptyString(row[COL_EXAM_NAME])) {
                parsedData = addInvalidPaboData(PaboData.INVALID_EXAM_NAME, parsedData);
                rowIdx++;
                continue;
            }

            if (emptyString(row[COL_MAJOR])) {
                parsedData = addInvalidPaboData(PaboData.INVALID_MAJOR, parsedData);
                rowIdx++;
                continue;
            }

            PaboData data = new PaboData();
            data.setMajor(row[COL_MAJOR]);
            data.setExamName(row[COL_EXAM_NAME]);
            data.setMatriculation(row[COL_MATRICULATION]);
            data.setValidationId(PaboData.VALID);
            data.setPaboFirstName(row[COL_FIRST_NAME]);
            data.setPaboLastName(row[COL_LAST_NAME]);
            data.setAttempt(attempts);
            parsedData.add(data);
            rowIdx++;
        }
        reader.close();
        return parsedData;
    }


    /**
     * Parses a Pabo export csv File with a list of students.
     * Each student in this list is not null,
     * has a valid matriculation number, and has a valid pabo grade.
     * @param students The list of students which pabo export should occur. The students
     *                 must all have valid pabo data and a valid pabo grade.
     * @param examDate The examDate of the course. If it's null, an empty string gets
     *                 written for the examDate.
     * @param fileName The fileName of the CSV file with the .csv ending.
     * @param dirPath The path to the directory to which the file should be written.
     * @see PaboData
     * @see PaboGrade
     * @return The csv file of the Pabo export.
     */
    public static File parsePaboDownload(List<Student> students, Date examDate,
                                         String fileName, String dirPath) throws IOException {
        String examDateStr = examDate == null ? ""
                : new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
                .format(examDate);

        log.debug(String.format("parsePaboDownload() called with exam date %s,"
                + " fileName %s and path %s", examDateStr, fileName, dirPath));

        if (students == null || dirPath == null || fileName == null) {
            throw new IllegalArgumentException("The students, fileName and dirPath cant be null");
        }

        File csv = new File(dirPath, fileName);

        log.debug("Got file: " + csv.toPath());
        log.debug("Opening CSVWriter");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(
                new FileOutputStream(csv), Charsets.ISO_8859_1), SEPARATOR, QUOTE_CHAR);

        log.debug("Writing headline");
        writer.writeNext(PABO_CSV_FILE_HEADER);

        for (Student student: students) {
            writer.writeNext(parseStudentToRow(student, examDateStr));
        }

        log.debug("Trying to flush");
        writer.flush();
        log.debug("Trying to close");
        writer.close();
        return csv;
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

    /**
     * Adds a new invalid pabo data object to the list.
     * @param invalidIdx The invalid idx of the pabo data.
     * @param dataList The list to which the object  should be added.
     * @see PaboData
     * @return The list with the data appended.
     */
    private static List<PaboData> addInvalidPaboData(int invalidIdx, List<PaboData> dataList) {
        PaboData data = new PaboData();
        data.setValidationId(invalidIdx);
        dataList.add(data);
        return dataList;
    }

    /**
     * Checks if a string is  null or empty.
     * @param string The string to be checked.
     * @return True if the string is null or empty, false otherwise.
     */
    private static boolean emptyString(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * Parses the information of the student to a string array which gets
     * written to the csv file.
     * @param student The student which information should be parsed. Should have
     *                valid pabo data and a pabo grade.
     * @param examDateStr A string of the PaboExamDate of the course. Can be empty.
     * @see PaboData
     * @see PaboGrade
     * @return A String array of the values to write for the csv file.
     */
    private static final String[] parseStudentToRow(Student student, String examDateStr) {
        log.debug("parseStudentToRow called with student" + student.getUser());
        String[] paboCSVRows = new String[NUM_COLS];
        paboCSVRows[COL_MATRICULATION] = student.getPaboData().getMatriculation();
        paboCSVRows[COL_LAST_NAME] = student.getPaboData().getPaboLastName();
        paboCSVRows[COL_FIRST_NAME] = student.getPaboData().getPaboFirstName();
        paboCSVRows[COL_EXAM_DATE] = examDateStr;
        //Initializing the grade here thanks to checkstyle
        PaboGrade grade = PaboGrade.valueOf(student.getPaboGrade());
        paboCSVRows[COL_GRADE] = grade.isNumeric() ? grade.getGradeName() : "";
        paboCSVRows[COL_EXAM_REMARK] = String.valueOf(grade.getGradeRemark());
        paboCSVRows[COL_EXAM_TRIES] = String.valueOf(student.getPaboData().getAttempt());
        paboCSVRows[COL_EXAM_NAME] = student.getPaboData().getExamName();
        paboCSVRows[COL_MAJOR] = student.getPaboData().getMajor();

        for (int emptyColIdx: COLS_ALWAYS_EMPTY) {
            paboCSVRows[emptyColIdx] = "";
        }

        return paboCSVRows;
    }
}
