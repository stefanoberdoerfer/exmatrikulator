package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PABODATA")
public class PaboData {

    /**
     * The int value of a value PaboData object with all fields set.
     */
    public static final int VALID = 0;

    /**
     * The int value of an invalid attempt field.
     */
    public static final int UNVALID_ATTEMPT = 1;

    /**
     * The int value of an invalid exam field.
     */
    public static final int UNVALID_EXAM_NAME = 2;

    /**
     * The int value of an invalid matriculation number field.
     */
    public static final int UNVALID_MATRICULATION = 3;

    /**
     * The int value of an invalid major field.
     */
    public static final int UNVALID_MAJOR = 4;

    /**
     * The int value of all empty fields while parsing.
     */
    public static final int UNVALID_ALL_EMPTY = 5;

    @Id
    @GeneratedValue
    private Long paboDataId;

    @Column
    private Integer attempt;

    @Column
    private String examName;

    @Column
    private String matriculation;

    @Column
    private String paboFirstName;

    @Column
    private String paboLastName;

    @Column
    private String major;

    @Column(nullable = false)
    private int validationId;

    /**
     * Method which checks if two paboData have the same content.
     * This isnt implemented in the equals() method because it is used to primarily
     * check the database ids of the pabo data.
     * @param data The data which should be compared to this paboData.
     * @return True if the contents of this pabodata match the pabodata parameter.
     */
    public boolean equalsContents(PaboData data) {
        if (data == null) {
            return false;
        }
        if (this.validationId != data.validationId) {
            return false;
        }
        return checkEqual(this.attempt, data.attempt)
               && checkEqual(this.examName, data.examName)
                && checkEqual(this.paboFirstName, data.paboFirstName)
                && checkEqual(this.paboLastName, data.paboLastName)
                && checkEqual(this.major, data.major)
                && checkEqual(this.matriculation, data.matriculation);
    }

    @Override
    public boolean equals(Object data) {
        if (! (data instanceof PaboData)) {
            return false;
        }
        return equalsContents((PaboData) data);
    }

    @Override
    public int hashCode() {
        int hCode = validationId;

        if (this.attempt != null) {
            hCode += attempt.hashCode();
        }

        if (this.matriculation != null) {
            hCode += matriculation.hashCode();
        }

        if (this.paboFirstName != null) {
            hCode += paboFirstName.hashCode();
        }

        if (this.paboLastName != null) {
            hCode += paboLastName.hashCode();
        }

        if (this.major != null) {
            hCode += major.hashCode();
        }

        if (this.examName != null) {
            hCode += examName.hashCode();
        }

        return hCode;
    }


    public void setPaboData(Long paboDataId) {
        this.paboDataId = paboDataId;
    }


    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Long getPaboDataId() {
        return paboDataId;
    }


    public Integer getAttempt() {
        return attempt;
    }

    public String getExamName() {
        return examName;
    }

    public String getMajor() {
        return major;
    }

    public String getMatriculation() {
        return matriculation;
    }

    public void setMatriculation(String matriculation) {
        this.matriculation = matriculation;
    }

    public int getValidationId() {
        return validationId;
    }

    public void setValidationId(int validationId) {
        this.validationId = validationId;
    }

    public boolean isValdid() {
        return validationId == VALID;
    }

    public String getPaboFirstName() {
        return paboFirstName;
    }

    public void setPaboFirstName(String paboFirstName) {
        this.paboFirstName = paboFirstName;
    }

    public String getPaboLastName() {
        return paboLastName;
    }

    public void setPaboLastName(String paboLastName) {
        this.paboLastName = paboLastName;
    }

    public String getFullName() {
        return paboFirstName + " " + paboLastName;
    }

    private boolean checkEqual(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 != null) {
                return false;
            }
        } else {
            if (!o1.equals(o2)) {
                return false;
            }
        }
        return true;
    }
}
