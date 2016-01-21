package de.unibremen.opensores.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity class for the restricted Time slots.
 *
 * @author Lorenz Huether
 */
@Entity
@Table(name = "RestrictedTimeSlot")
public class RestrictedTimeSlot extends TimeSlot {

    @Column(name = "MINSTUDENTS", nullable = true)
    private int minStudents;

    @Column(name = "MAXSTUDENTS", nullable = true)
    private int maxStudents;

    @Column(name = "DEADLINE", nullable = false)
    private Date deadline;

    public int getMinStudents() {
        return minStudents;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setMinStudents(int minStudents) {
        this.minStudents = minStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
}
