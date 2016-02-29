package de.unibremen.opensores.model;

/**
 * Enumeration of different Pabo Grades.
 */
public enum PaboGrade {
    GRADE_1_0(1.0,7,"1"),
    GRADE_1_3(1.3,7,"1,3"),
    GRADE_1_7(1.7,7,"1,7"),
    GRADE_2_0(2.0,7,"2"),
    GRADE_2_3(2.3,7,"2,3"),
    GRADE_2_7(2.7,7,"2,7"),
    GRADE_3_0(3.0,7,"3"),
    GRADE_3_3(3.0,7,"3,3"),
    GRADE_3_7(3.0,7,"3,7"),
    GRADE_4_0(4.0,7,"4"),
    GRADE_5_0(5.0,8,"5"),
    GRADE_CHEATED(null,3,"Täuschung"),
    GRADE_NEGLECTED(null,2,"Versäumnis");

    private final Double gradeValue;
    private final String gradeName;
    private final int gradeRemark;

    PaboGrade(final Double gradeValue, final int gradeRemark,
              final String gradeName) {
        this.gradeValue = gradeValue;
        this.gradeRemark = gradeRemark;
        this.gradeName = gradeName;
    }


    @Override
    public String toString() {
        return gradeName;
    }

    public double getGradeValue() {
        return gradeValue;
    }

    public String getGradeName() {
        return gradeName;
    }

    public int getGradeRemark() {
        return gradeRemark;
    }

    public boolean isNumeric() {
        return gradeValue != null;
    }

    /**
     * Searches for an enum entry by the value of the pabo grade.
     * @param value Value to search for
     * @return Enum entry
     */
    public static PaboGrade valueOf(double value) {
        for (PaboGrade p : PaboGrade.values()) {
            if (p.getGradeValue() == value) {
                return p;
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Searches for an enum entry by the name of the pabo grade.
     * @param name Name to search for
     * @return Enum entry
     */
    public static PaboGrade valueOfName(String name) {
        for (PaboGrade p : PaboGrade.values()) {
            if (p.getGradeName().equals(name)) {
                return p;
            }
        }

        throw new IllegalArgumentException();
    }
}