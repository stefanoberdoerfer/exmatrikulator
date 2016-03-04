package de.unibremen.opensores.webapp.validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A validator which validates that an selected end date is after the
 * selected start date.
 * @author Kevin Scheck
 */
@FacesValidator(value = "dateRangeValidator")
public class DateRangeValidator implements Validator {

    /**
     * The log4j logger for debug, errors logs from log4j.
     * These logs are not related to actions in the exmatrikulator business domain.
     */
    private static Logger log = LogManager.getLogger(DateRangeValidator.class);

    /**
     * Validates that the end date of the selected event is after the start date.
     * @pre The start date is not null.
     * @param facesContext The FacesContext in which the validation is done.
     * @param uiComponent The UIComponent for which the validation is done.
     *                    Cant be null and should have an attribute map.
     * @param value The value of the end date
     * @throws ValidatorException If the end date is after the start date,
     *                            when the end date and the start date are valid dates.
     *                            When they are null or not Date objects,
     *                            the UIComponent should throw an ValidatorException
     *                            with required=true.
     */
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object value)
            throws ValidatorException {
        log.debug("validateEndDateAfterStartDate called with : " + value);
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        Object startDateObj = uiComponent.getAttributes().get("start-date");

        if (!(value instanceof Date) || !(startDateObj instanceof Date)) {
            log.debug("Returning.. value instance of Date? " + (value instanceof Date)
                    + " is start date null? " + startDateObj);
            return;
        }

        Date startDate = (Date) startDateObj;
        Date endDate = (Date) value;
        log.debug("End Date: " + endDate);
        log.debug("Start Date: " + startDate);
        if (!endDate.after(startDate)) {
            List<FacesMessage> msgs = new ArrayList<>();
            msgs.add(new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("common.error"),
                    bundle.getString("tutEvent.validatorMessageEndDate")));

            throw new ValidatorException(msgs);
        }
        log.debug("Date is valid");
    }
}