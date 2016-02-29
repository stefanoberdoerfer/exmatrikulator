package de.unibremen.opensores.util.csv;

import com.opencsv.CSVWriter;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Group;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.RegEx;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Parses information about course data in a csv file.
 * @autor Kevin Scheck.
 */
public final class CourseDataParser {

    /**
     * Regex to filter all characters which are not numerical or alphabetical.
     */
    @RegEx
    private static final String REGEX_KILL_SPECIAL_CHARS
            = "[^a-zäÄüÜöÖßA-Z0-9]";

    private static final char SEPARATOR = ';';
    private static final char QUOTE_CHAR = CSVWriter.NO_QUOTE_CHARACTER;

    private static final int NUM_COLS_WO_EXAMS = 5;
    private static final String HEADER_MATR = "matr";
    private static final String HEADER_TUTORIAL = "tutorial";
    private static final String HEADER_GROUP = "group";
    private static final String HEADER_PART_TYPE = "participation_type";
    private static final String HEADER_FINAL_GRADE = "final_grade";

    private static final int COL_MATR = 0;
    private static final int COL_TUTORIAL = 1;
    private static final int COL_GROUP = 2;
    private static final int COL_PART_TYPE = 3;
    private static final int COL_FINAL_GRADE = 4;

    private static final String HIDDEN = "?";
    /**
     * The logger of this Parser.
     */
    private static Logger log = LogManager.getLogger(PaboParser.class);


    /**
     * Private Constructor for no object creation.
     */
    private CourseDataParser() {}


    /**
     * Parses the data of a course (the students, gradings, tutorials etc.) into
     * a csv file.
     * The CSV File has the matriculation number, the tutorial, the group and
     * every grading of the student. If the user is not a lecturer, only the data
     * the privileged user has access to (gradings, tutorial, groups) get displayed.
     * If the data is not available, an empty string gets placed in the data.
     * If the user has no access to the data, a question mark ('?') gets displayed
     * instead of the data.
     * The data is in .csv file format.
     * @param course the course from which the data should be parsed.
     * @param isLecturer Whether the user is a lecturer.
     * @param privUser The privileged user when the user is not a lecturer. Should
     *                 be null if the user is lecturer, shouldn't be when he is not.
     * @throws IllegalArgumentException If the course, dirPath, or fileName is null.
     *                                  If the user is not a lecturer but the privUser
     *                                  is null.
     * @throws IOException If the parsing the file goes wrong.
     *
     */
    public static File parseCourseToCSV(Course course, boolean isLecturer,
                                        PrivilegedUser privUser, String dirPath,
                                        String fileName) throws IOException {
        if (dirPath == null || fileName == null || course == null) {
            throw new IllegalArgumentException("The course, dir path and file name cant "
                    + " be null.");
        }
        if (!isLecturer && privUser == null) {
            throw new IllegalArgumentException("When the user is not a lecturer "
                    + " the priv user cant be null.");
        }
        File csv = new File(dirPath, fileName);

        log.debug("Ceating the csv writer");
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(
                new FileOutputStream(csv), Charsets.UTF_8),
                SEPARATOR, QUOTE_CHAR);

        log.debug("Writing header line");
        writer.writeNext(getHeader(course));

        log.debug("Writing the data of students");
        for (Student student: course.getStudents()) {
            log.debug("Writing for "  + student.getUser());
            writer.writeNext(getStudentDataRow(student, isLecturer, privUser));
        }

        log.debug("Trying to flush");
        writer.flush();
        log.debug("Trying to close");
        writer.close();
        return csv;
    }

    /**
     * Gets the header line with the matriculation, the tutorial, the group and
     * all the gradings.
     * @param course The course which is used to get the header line.
     * @return A string array of header lines.
     */
    private static String[] getHeader(Course course) {
        int numHeaders = NUM_COLS_WO_EXAMS + course.getExams().size();
        String[] headers = new String[numHeaders];
        headers[COL_MATR] = HEADER_MATR;
        headers[COL_FINAL_GRADE] = HEADER_FINAL_GRADE;
        headers[COL_PART_TYPE] = HEADER_PART_TYPE;
        headers[COL_TUTORIAL] = HEADER_TUTORIAL;
        headers[COL_GROUP] = HEADER_GROUP;

        int colIdxExam = NUM_COLS_WO_EXAMS;

        for (Exam exam: course.getExams()) {
            String headerExam = cleanString(exam.getName())
                    + "(" + GradeType.getById(exam.getGradeType()).name();
            if (exam.getGradeType().equals(GradeType.Point.getId())) {
                headerExam += " max: " + exam.getMaxPoints();
            }
            headerExam += ")";
            headers[colIdxExam] = headerExam;
            colIdxExam++;
        }

        return headers;
    }

    /**
     * Writes the data of a student to a csv line. Uses a string array for the data.
     * @param student The student which data should be written in the line.
     * @param isLecturer Wheter the logged in user was a lecturer.
     * @param privilegedUser If the logged user is not a lecturer, use the privileged
     *                       usr to check to which data the user has the rights to view.
     * @return The line of th csv file as string array.
     */
    private static String[] getStudentDataRow(Student student,
                                                    boolean isLecturer,
                                                    PrivilegedUser privilegedUser) {
        int numHeaders = NUM_COLS_WO_EXAMS + student.getCourse().getExams().size();

        String[] row = new String[numHeaders];
        row[COL_MATR] = getMatriculationString(student);
        row[COL_FINAL_GRADE] = getPaboGradeString(student, isLecturer, privilegedUser);
        row[COL_PART_TYPE] = getPartTypeString(student);
        row[COL_TUTORIAL] = getTutorialString(student.getTutorial(), isLecturer, privilegedUser);
        row[COL_GROUP] = getGroupString(student.getGroup(), isLecturer, privilegedUser);

        int colIdxExam = NUM_COLS_WO_EXAMS;
        for (Exam exam: student.getCourse().getExams()) {
            String foundGradingStr = null;
            for (Grading grading: student.getGradings()) {
                if (grading != null && grading.getExam() != null
                        && grading.getExam().equals(exam)) {
                    Grade grade = grading.getGrade();
                    if (grade != null && grade.getValue() != null) {
                        if (isLecturer || grading.getCorrector()
                                .equals(privilegedUser.getUser())) {
                            foundGradingStr = grading.getGrade().getValue().toString();
                        } else {
                            foundGradingStr = HIDDEN;
                        }
                    }
                }
            }
            if (foundGradingStr == null) {
                foundGradingStr = "";
            }
            row[colIdxExam] = foundGradingStr;
            colIdxExam++;
        }
        return row;
    }

    private static String getPartTypeString(Student student) {
        if (student.getParticipationType() == null) {
            return "";
        } else {
            return cleanString(student.getParticipationType().getName());
        }
    }

    private static String getMatriculationString(Student student) {
        String matr = student.getUser().getMatriculationNumber();
        return matr == null ? "" : cleanString(matr);
    }

    private static String getPaboGradeString(Student student, boolean isLecturer,
                                                   PrivilegedUser privilegedUser) {
        if (student.getPaboGrade() == null) {
            return "";
        }
        if (isLecturer || privilegedUser.hasPrivilege(Privilege.ExportData)) {
            return PaboGrade.valueOf(student.getPaboGrade()).getGradeName();
        } else {
            return HIDDEN;
        }
    }

    private static String getTutorialString(Tutorial tutorial, boolean isLecturer,
                                                  PrivilegedUser privilegedUser) {
        if (tutorial == null) {
            return "";
        }

        if (isLecturer || tutorial.getTutors().contains(privilegedUser)) {
            return cleanString(tutorial.getName());
        } else {
            return HIDDEN;
        }
    }

    private static String getGroupString(Group group, boolean isLecturer,
                                               PrivilegedUser privilegedUser) {
        if (group == null) {
            return "";
        }

        if (isLecturer || group.getTutorial().getTutors().contains(privilegedUser)) {
            return cleanString(group.getName());
        } else {
            return HIDDEN;
        }
    }


    /**
     * Cleans string from non numeric, non alphabetic, non whitespace characters.
     * Replaces unwanted characters with no chars.
     * @param string The string to be cleaned.
     * @return The cleaned string.
     */
    private static String cleanString(String string) {
        return string.replaceAll(REGEX_KILL_SPECIAL_CHARS,"");
    }


}
