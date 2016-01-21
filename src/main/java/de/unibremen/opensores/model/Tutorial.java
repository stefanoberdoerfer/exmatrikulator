package de.unibremen.opensores.model;

import javax.persistence.*;
import java.util.List;

/**
 * Entity bean for the Tutorial class.
 */
@Entity
@Table(name = "TUTORIALS")
public class Tutorial {

    @Id
    @GeneratedValue
    private Long tutorialId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @OneToMany(mappedBy = "tutorial")
    private List<Student> students;

    //private List<Group> groups;

    //private List<PrivilegedUser> tutors;

    public Long getTutorialId() {
        return tutorialId;
    }

    public void setTutorialId(Long tutorialId) {
        this.tutorialId = tutorialId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    /*
    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<PrivilegedUser> getTutors() {
        return tutors;
    }

    public void setTutors(List<PrivilegedUser> tutors) {
        this.tutors = tutors;
    }
    */
}
