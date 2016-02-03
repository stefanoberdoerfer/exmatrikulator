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
@Table(name = "RESTRICTEDTIMESLOTS")
public class RestrictedTimeSlot extends TimeSlot {

    @Column(nullable = true)
    private int minStudents;

    @Column(nullable = true)
    private int maxStudents;

    @Column(nullable = false)
    private Date deadline;

    public int getMinStudents() {
        return minStudents;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public Date getDeadline() {
        return new Date(deadline.getTime());
    }

    public void setMinStudents(int minStudents) {
        this.minStudents = minStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }

    public void setDeadline(Date deadline) {
        this.deadline = new Date(deadline.getTime());
    }
}
