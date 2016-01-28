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
    private String name;

    @Column(nullable = false)
    private Boolean isWinter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isWinter() {
        return isWinter;
    }

    public void setIsWinter(Boolean winter) {
        isWinter = winter;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    @Override
    public String toString() {
        if (name == null) {
            return null;
        }

        return ((isWinter) ? "WiSe" : "SoSe") + " " + name;
    }
}
