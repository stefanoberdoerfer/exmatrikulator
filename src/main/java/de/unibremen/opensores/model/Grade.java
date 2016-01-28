package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * Class representation of a grade.
 */
@Entity
@Table(name = "GRADES")
public class Grade {

    @Id
    @GeneratedValue
    private Long gradeId;

    @Column(nullable = false)
    private Integer gradeType;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = true)
    private BigDecimal maxPoints;

    public Long getGradeId() {
        return gradeId;
    }

    public Integer getGradeType() {
        return gradeType;
    }

    public void setGradeType(Integer gradeType) {
        this.gradeType = gradeType;
    }

    public void setGradeTypeAsEnum(GradeType gradeType) {
        this.gradeType = gradeType.getId();
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(BigDecimal maxPoints) {
        this.maxPoints = maxPoints;
    }
}
