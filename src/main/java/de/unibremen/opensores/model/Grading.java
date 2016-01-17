package de.unibremen.opensores.model;

/**
 * Entity bean for the Grading class.
 */
public class Grading {

    private Long gradingId;

    private User corrector;

    private Grade grade;

    private String publicComment;

    private String privateComment;

    private Group group;

    public User getCorrector() {
        return corrector;
    }

    public void setCorrector(User corrector) {
        this.corrector = corrector;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public String getPublicComment() {
        return publicComment;
    }

    public void setPublicComment(String publicComment) {
        this.publicComment = publicComment;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public void setPrivateComment(String privateComment) {
        this.privateComment = privateComment;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
