package de.unibremen.opensores.model;

import javax.persistence.*;

/**
 * Entity bean for the Lecturer class.
 */
@Entity
@Table(name = "LECTURERS")
public class Lecturer {

    @Id
    @GeneratedValue
    private Long lecturerId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courseId")
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
