package de.unibremen.opensores.model;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * The entity of a tutorial event.
 */
@Entity
public class TutorialEvent extends DefaultEvent {

    @ManyToOne
    private Tutorial tutorial;

    public TutorialEvent() {
        super();
    }

    public TutorialEvent(Tutorial tutorial, Date startDate, Date endDate) {
        this.tutorial = tutorial;
        
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
}
