package de.unibremen.opensores.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

/**
 * The entity of a tutorial event.
 */
@Entity
public class TutorialEvent extends DefaultEvent implements Serializable {

    private static final long serialVersionUID = -4409860309037617938L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @ManyToOne
    private Tutorial tutorial;

    /**
     * The user id of the creator of this tutorial event.
     */
    @Column
    private long creatorId;

    public TutorialEvent() {
        super();
    }


    /**
     * Constructor for creating a tutorial event with all necessary data.
     * @param tutorial The tutorial in which the event is located.
     * @param creatorId The user id of the user which created the event.
     * @param startDate The start date of the event.
     * @param endDate The end date of the event.
     */
    public TutorialEvent(Tutorial tutorial, long creatorId, Date startDate, Date endDate) {
        this.tutorial = tutorial;
        this.creatorId = creatorId;
        this.setStartDate(new Date(startDate.getTime()));
        this.setEndDate(new Date(endDate.getTime()));

    }

    public Tutorial getTutorial() {
        return tutorial;
    }

    public void setTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

    /**
     * Returns a standard title for the tutorial evenet using only the tutorial event.
     * @return The tutorial name as title.
     */
    @Override
    public String getTitle() {
        return (tutorial == null) ? "" : tutorial.getName();
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }
}
