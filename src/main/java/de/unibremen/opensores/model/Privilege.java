package de.unibremen.opensores.model;

/**
 * Enumeration of Privileges a PrivilegedUser can have.
 */
public enum Privilege {
    EditFormulas(0), EditExams(1), ManageStudents(2), ExportData(3), ManageTutorials(4),
    GenerateCredits(5), CreateExamEvents(6), ManageRecordBooks(7);

    private Integer id;

    Privilege(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
