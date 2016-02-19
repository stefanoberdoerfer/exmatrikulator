package de.unibremen.opensores.model;

import org.primefaces.model.ScheduleEvent;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * The Default Event for events in the PrimeFaces scheduler used in the exmatrikulator.
 * Implements ScheduleEvent of PrimeFaces and acts as a persistable DefaultScheduleEvent.
 * http://grepcode.com/file/repository.primefaces.org/org.primefaces/primefaces/2.1/org/primefaces/model/DefaultScheduleEvent.java
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class DefaultEvent implements ScheduleEvent, Serializable {

    @Id
    @GeneratedValue
    private long eventId;

    @Column(nullable = false)
    private Date startDate;

    @Column
    private Date endDate;

    @Column
    private String description;

    @Column
    private String styleClass;

    @Column
    private Boolean isAllDay;

    @Column
    private Boolean isEditable;

    @Column
    private String primeFacesId;

    @Column
    private String title;

    @Column
    private Object data;

    public DefaultEvent() {}


    /**
     * Constructor for a default PrimeFaces schedule event.
     * @param title The title of the event.
     * @param start THe start date of the event.
     * @param end The end date of the event
     */
    public DefaultEvent(String title, Date start, Date end) {
        this.title = title;
        this.startDate = start;
        this.endDate = end;
    }

    /**
     * Constructor for a default PrimeFaces schedule event.
     * @param title The title of the event.
     * @param start THe start date of the event.
     * @param end The end date of the event
     */
    public DefaultEvent(String title, Date start, Date end, boolean allDay) {
        this.title = title;
        this.startDate = start;
        this.endDate = end;
        this.isAllDay = allDay;
    }

    /**
     * Constructor for a default PrimeFaces schedule event.
     * @param title The title of the event.
     * @param start THe start date of the event.
     * @param end The end date of the event
     * @param styleClass The styleClass of the event.
     */
    public DefaultEvent(String title, Date start, Date end, String styleClass) {
        this.title = title;
        this.startDate = start;
        this.endDate = end;
        this.styleClass = styleClass;
    }

    /**
     * Constructor for a default PrimeFaces schedule event.
     * @param title The title of the event.
     * @param start THe start date of the event.
     * @param end The end date of the event.
     * @param data The extra data object of the event.
     */
    public DefaultEvent(String title, Date start, Date end, Object data) {
        this.title = title;
        this.startDate = start;
        this.endDate = end;
        this.data = data;
    }

    @Override
    public String getId() {
        return primeFacesId;
    }

    @Override
    public void setId(String primeFacesId) {
        this.primeFacesId = primeFacesId;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    @Override
    public Date getEndDate() {
        return new Date(endDate.getTime());
    }

    @Override
    public boolean isAllDay() {
        return isAllDay;
    }

    @Override
    public String getStyleClass() {
        return styleClass;
    }

    @Override
    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setEndDate(Date endDate) {
        this.endDate = new Date(endDate.getTime());
    }

    public void setStartDate(Date startDate) {
        this.startDate = new Date(startDate.getTime());
    }
}
