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
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Long getFormulaId() {
        return formulaId;
    }
}
