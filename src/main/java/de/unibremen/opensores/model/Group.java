package de.unibremen.opensores.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity bean for the Group class.
 *
 * @author Stefan Oberdoerfer, Lorenz Huether
 */
@Entity
@Table(name = "GROUPS")
public class Group {

    @Id
    @GeneratedValue
    private Long groupId;

    @Column(nullable = false, length = 32)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH})
    private List<Student> students = new ArrayList<>();

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "tutorialId")
    private Tutorial tutorial;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Tutorial getTutorial() {
        return tutorial;
    }

    public void setTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
