package de.unibremen.opensores.model;

import de.unibremen.opensores.exception.SemesterFormatException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity bean for the Semester class.
 */
@Entity
@Table(name = "SEMESTERS")
public class Semester {

    @Id
    @GeneratedValue
    private Long semesterId;

    @Column(nullable = false)
    private Boolean isWinter;

    @Column(nullable = false)
    private Integer semesterYear;

    @Column(nullable = false)
    public Boolean isWinter() {
        return isWinter;
    }

    public void setIsWinter(Boolean winter) {
        isWinter = winter;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public Integer getSemesterYear() {
        return semesterYear;
    }

    public void setSemesterYear(Integer semesterYear) {
        this.semesterYear = semesterYear;
    }

    /**
     * Converts the given semester string to a semster object.
     *
     * @param str String to parse.
     * @return Parsed semester object.
     * @throws SemesterFormatException If the string is mailformated.
     */
    public static Semester valueOf(String str) {
        String[] splited = str.split(" ");
        if (splited.length != 2) {
            throw new SemesterFormatException("Missing space seperator");
        }

        Semester semester = new Semester();
        switch (splited[0]) {
            case "WiSe":
                semester.setIsWinter(true);
                break;
            case "SoSe":
                semester.setIsWinter(false);
                break;
            default:
                throw new SemesterFormatException("Invalid semester type");
        }

        String[] years = splited[1].split("/");
        if (years.length != 2 || years[0].length() !=
                years[1].length()) {
            throw new SemesterFormatException("Invalid year length");
        }

        String fmt = null;
        if (years[0].length() == 2) {
                fmt = "yy";
        } else if (years[0].length() == 4) {
                fmt = "yyyy";
        } else {
                throw new SemesterFormatException("Invalid year format");
        }

        Calendar cal = Calendar.getInstance();
        DateFormat sfmt = new SimpleDateFormat(fmt);
        try {
            cal.setTime(sfmt.parse(years[0]));
        } catch (ParseException e) {
            throw new SemesterFormatException(e.getMessage());
        }

        semester.setSemesterYear(cal.get(Calendar.YEAR));
        return semester;
    }

    @Override
    public String toString() {
        Integer nextYear = semesterYear + 1;
        String ty = semesterYear.toString();
        String ny = nextYear.toString();

        return ((isWinter) ? "WiSe" + ty + "/" + ny : "SoSe" + ty);
    }
}
