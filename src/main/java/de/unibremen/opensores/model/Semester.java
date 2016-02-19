package de.unibremen.opensores.model;

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

    @Override
    public String toString() {
        Integer nextYear = semesterYear + 1;
        String ty = semesterYear.toString();
        String ny = nextYear.toString();

        return ((isWinter) ? "WiSe" + ty + "/" + ny : "SoSe" + ty);
    }
}
