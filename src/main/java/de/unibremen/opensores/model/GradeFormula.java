package de.unibremen.opensores.model;

import java.util.Date;

/**
 * Entity bean for the GradeFormula class.
 */
public class GradeFormula {

    private Long formulaId;

    private String formula;

    private User editor;

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
}
