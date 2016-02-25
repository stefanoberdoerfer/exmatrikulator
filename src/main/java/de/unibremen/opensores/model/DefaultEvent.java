package de.unibremen.opensores.model;

import de.unibremen.opensores.util.Constants;
import org.primefaces.model.ScheduleEvent;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;

import java.io.Serializable;
import java.util.Date;

/**
 * The Default Event for events in the PrimeFaces scheduler used in the exmatrikulator.
 * Implements ScheduleEvent of PrimeFaces and acts as a persistable DefaultScheduleEvent.
 * http://grepcode.com/file/repository.primefaces.org/org.primefaces/primefaces/2.1/org/primefaces/model/DefaultScheduleEvent.java
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class DefaultEvent implements ScheduleEvent, Serializable {

    /**
     * The primary key of a default event in our database.
     */
    @Id
    @GeneratedValue
    private Long eventId;

    @Column(nullable = false)
    private Date startDate;

    @Column
    private Date endDate;

    @Column(length = Constants.MAX_TUT_EVENT_DESCR_LENGTH)
    private String description;

    @Column
    private String styleClass;

    @Column
    private Boolean isAllDay;

    @Column
    private Boolean isEditable;

    /**
     * The id used by the primefaces scheduler.
     * Not used as primary key in our database.
     */
    @Column
    private String id;

    @Column
    private String title;

    @Lob
    @Column
    private Object data;

    /**
     * Default constructor of a default event.
     */
    public DefaultEvent() {
        this.description = "";
        this.isAllDay = false;
        this.isEditable = true;
    }


    /**
     * Constructor for a default PrimeFaces schedule event.
     * @param title The title of the event.
     * @param start THe start date of the event.
     * @param end The end date of the event
     */
    public DefaultEvent(String title, Date start, Date end) {
        this();
        this.title = title;
        this.startDate = new Date(start.getTime());
        this.endDate = new Date(end.getTime());
    }

    /**
     * Constructor for a default PrimeFaces schedule event.
     * @param title The title of the event.
     * @param start THe start date of the event.
     * @param end The end date of the event
     */
    public DefaultEvent(String title, Date start, Date end, boolean allDay) {
        this(title, start, end);
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
        this(title, start, end);
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
        this(title, start, end);
        this.data = data;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String primeFacesId) {
        this.id = primeFacesId;
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

    public void setIsAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
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

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEndDate(Date endDate) {
        this.endDate = new Date(endDate.getTime());
    }

    public void setStartDate(Date startDate) {
        this.startDate = new Date(startDate.getTime());
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
