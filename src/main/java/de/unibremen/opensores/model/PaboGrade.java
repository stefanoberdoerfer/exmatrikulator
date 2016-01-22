package de.unibremen.opensores.model;

/**
 * Enumeration of different Pabo Grades.
 */
public enum PaboGrade {
    GRADE_1_0(1.0,"1,0"),
    GRADE_1_3(1.3,"1,3"),
    GRADE_1_7(1.7,"1,7"),
    GRADE_2_0(2.0,"2,0"),
    GRADE_2_3(2.3,"2,3"),
    GRADE_2_7(2.7,"2,7"),
    GRADE_3_0(3.0,"3,0"),
    GRADE_3_3(3.0,"3,3"),
    GRADE_3_7(3.0,"3,7"),
    GRADE_4_0(4.0,"4,0"),
    GRADE_5_0(5.0,"5,0"),
    GRADE_CHEATED(5.0,"Täuschung"),
    GRADE_NEGLECTED(5.0,"Versäumnis");

    private final double gradeValue;
    private final String gradeName;

    PaboGrade(final double gradeValue, final String gradeName) {
        this.gradeValue = gradeValue;
        this.gradeName = gradeName;
    }


    @Override
    public String toString() {
        return gradeName;
    }
}
