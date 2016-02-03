package de.unibremen.opensores.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity class for the time slots.
 *
 * @author Lorenz Huether
 */
@Entity
@Table(name = "TIMESLOTS")
public class TimeSlot {

    @Id
    @GeneratedValue
    private Long timeSlotId;

    @Column(nullable = false)
    private Date start = new Date();

    @Column
    private int duration;

    public Date getStart() {
        return new Date(start.getTime());
    }

    public int getDuration() {
        return duration;
    }

    public void setStart(Date start) {
        this.start = new Date(start.getTime());
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }
}
