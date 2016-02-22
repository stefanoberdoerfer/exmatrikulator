package de.unibremen.opensores.model;

import java.math.BigDecimal;

/**
 * Enumeration of the different GradeTypes of a Grade.
 */
public enum GradeType {
    Pabo(-1), Numeric(1), Boolean(2), Point(3), Percent(4);

    private Integer id;

    GradeType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    /**
     * Returns if the given value is valid. Max value will only be considered
     * for Point grades.
     * @param value Value to check
     * @param max Maximum value for Point grades
     * @return true if valid
     */
    public boolean isValidGrading(BigDecimal value, final BigDecimal max) {
        switch (this) {
            case Numeric:
                return value.compareTo(new BigDecimal("1.0")) >= 0
                        && value.compareTo(new BigDecimal("5.0")) <= 0;

            case Boolean:
                return value.compareTo(new BigDecimal("0")) == 0
                        || value.compareTo(new BigDecimal("1")) == 0;

            case Point:
                return value.compareTo(new BigDecimal("0")) >= 0
                        && value.compareTo(max) <= 0;

            case Percent:
                return value.compareTo(new BigDecimal("0")) >= 0
                        && value.compareTo(new BigDecimal("100")) <= 0;

            default:
                return false;
        }
    }

    /**
     * Returns if the given value can be considered as passed.
     * @param value Value to check
     * @return True if passed
     */
    public boolean hasPassed(BigDecimal value) {
        switch (this) {
            case Boolean:
                return value != null
                       && value.compareTo(new BigDecimal("1")) == 0;

            default:
                return false;
        }
    }
}
