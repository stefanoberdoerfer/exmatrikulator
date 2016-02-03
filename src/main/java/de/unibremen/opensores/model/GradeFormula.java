package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Entity bean for the GradeFormula class.
 */
@Entity
@Table(name = "GRADEFORMULAS")
public class GradeFormula {

    @Id
    @GeneratedValue
    private Long formulaId;

    @Column(nullable = false)
    private String formula;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId")
    private User editor;

    @Column(nullable = false)
    private Date time;

    @Column
    private boolean valid;

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public User getEditor() {
        return editor;
    }

    public void setEditor(User editor) {
        this.editor = editor;
    }

    public Date getTime() {
        return new Date(time.getTime());
    }

    public void setTime(Date time) {
        this.time = new Date(time.getTime());
    }

    public Long getFormulaId() {
        return formulaId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
