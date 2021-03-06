package de.unibremen.opensores.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.ArrayList;

/**
 * Entity bean for the Tutorial class.
 */
@Entity
@Table(name = "TUTORIALS")
public class Tutorial {

    @Id
    @GeneratedValue
    private Long tutorialId;

    @Column(nullable = false, length = 32)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @OneToMany(mappedBy = "tutorial", cascade = CascadeType.MERGE)
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "tutorial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Group> groups = new ArrayList<>();

    @OneToMany(mappedBy = "tutorial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TutorialEvent> events = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "TUTORIAL_PRIVUSER",
            joinColumns = {@JoinColumn(name = "tutorialId")},
            inverseJoinColumns = {@JoinColumn(name = "privUserId")})
    private List<PrivilegedUser> tutors = new ArrayList<>();

    @Override
    public boolean equals(Object object) {
        return object instanceof Tutorial && (tutorialId != null)
                ? tutorialId.equals(((Tutorial) object).tutorialId)
                : (object == this);
    }

    @Override
    public int hashCode() {
        return tutorialId != null
                ? this.getClass().hashCode() + tutorialId.hashCode()
                : super.hashCode();
    }

    public Long getTutorialId() {
        return tutorialId;
    }

    public void setTutorialId(Long id) {
        this.tutorialId = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TutorialEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TutorialEvent> events) {
        this.events = events;
    }
}
