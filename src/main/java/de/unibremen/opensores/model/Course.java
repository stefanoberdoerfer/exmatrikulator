package de.unibremen.opensores.model;

/**
 * Entity bean for the Course class.
 */
@Entity
@Table(name = "STUDENT_TABLE")
public class Course {
    @Id
    @Column(name = "course_id", nullable = false)
    @GeneratedValue
    private Long courseId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "number", nullable = false, length = 64)
    private String number;

    @Column(name = "require_conformation", nullable = false)
    private Boolean requiresConformation;

    @Column(name = "email_template", nullable = false)
    private String emailTemplate;

    @Column(name = "min_group_size", nullable = false)
    private Integer minGroupSize;

    @Column(name = "max_group_size", nullable = false)
    private Integer maxGroupSize;

    @OneToOne(optional=false, mappedBy="student", targetEntity=Student.class)
    private Student student;

    public String getName() {
        return email;
    }

    public String getNumber() {
        return number;
    }

    public boolean requiresConformation() {
        return requiresConformation;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    public int getMinGroupSize() {
        return minGroupSize;
    }

    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public void setRequiresConformation(final boolean requiresConformation) {
        this.requiresConformation = requiresConformation;
    }

    public void setEmailTemplate(final String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public void setMinGroupSize(final int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    public void setMaxGroupSize(final int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }
}
