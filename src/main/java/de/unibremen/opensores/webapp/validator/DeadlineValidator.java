package de.unibremen.opensores.webapp.validator;

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
 * A custom validator for validating deadlines in the future.
 * @author Kevin scheck
 */
@FacesValidator(value = "deadlineValidator")
public class DeadlineValidator implements Validator {

    /**
     * Validates deadlines, which must be in the future.
     * @param ctx The FacesContext for which the validation occurs.
     * @param comp The corresponding ui component.
     * @param value The value of the deadline (the user input).
     */
    public void validate(FacesContext ctx, UIComponent comp, Object value) {
        List<FacesMessage> msgs = new ArrayList<>();

        ResourceBundle bundle = ResourceBundle.getBundle("messages",
                ctx.getViewRoot().getLocale());

        if (!(value instanceof Date)) {
            msgs.add(new FacesMessage(bundle.getString("examination.messageGiveDate")));
            throw new ValidatorException(msgs);
        }

        Date deadline = (Date) value;

        if (deadline.before(new Date())) {
            msgs.add(new FacesMessage(bundle.getString("examination.messageDatePassed")));
            throw new ValidatorException(msgs);
        }
    }
}
