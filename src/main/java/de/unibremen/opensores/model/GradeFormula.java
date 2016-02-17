package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
    @Column(name = "FORMULA_ID")
    private Long formulaId;

    @Lob
    @Column(length = 100000)
    private String formula;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId")
    private User editor;

    @Column(nullable = false)
    private Date saveDate;

    @Column(nullable = false)
    private String editDescription = "";

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "partTypeId")
    private ParticipationType participationType;

    @Column
    private boolean valid;

    @Override
    public boolean equals(Object object) {
        return object instanceof GradeFormula && (formulaId != null)
                ? formulaId.equals(((GradeFormula) object).formulaId)
                : (object == this);
    }

    @Override
    public int hashCode() {
        return formulaId != null
                ? this.getClass().hashCode() + formulaId.hashCode()
                : super.hashCode();
    }
    

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


    public Long getFormulaId() {
        return formulaId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getEditDescription() {
        return editDescription;
    }

    public void setEditDescription(String editDescription) {
        this.editDescription = editDescription;
    }


    public Date getSaveDate() {
        return new Date(saveDate.getTime());
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = new Date(saveDate.getTime());
    }

    public ParticipationType getParticipationType() {
        return participationType;
    }

    public void setParticipationType(ParticipationType participationType) {
        this.participationType = participationType;
    }
}