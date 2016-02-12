package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.service.CourseService;

import java.util.ResourceBundle;
import java.util.Map;
import java.text.MessageFormat;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.convert.ConverterException;

/**
 * Converter for the PrivilegedUser class.
 *
 * @author Sören Tempel
 */
@ManagedBean
@RequestScoped
public class PrivilegedUserConverter implements Converter {
    /**
     * The course service for connection to the database.
     */
    @EJB
    private transient CourseService courseService;

    @Override
    public Object getAsObject(FacesContext context,
            UIComponent component, String value) {
        if (value == null) {
            return null;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());
        Map<String, Object> smap = facesContext.getExternalContext()
            .getSessionMap();

        Course course = (Course) smap.get("course");
        if (course == null) {
            String msg = bundle.getString("courses.fail");
            throw new ConverterException(new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
        }

        PrivilegedUser tutor = courseService.findTutor(course, value);
        if (tutor == null) {
            String fmt = bundle.getString("courses.studentDoesNotExist");
            String msg = new MessageFormat(fmt).format(new Object[]{value});
            throw new ConverterException(new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
        }

        return tutor;
    }

    @Override
    public String getAsString(FacesContext context,
            UIComponent component, Object value) {
        if (!(value instanceof PrivilegedUser)) {
            return "";
        }

        User user = ((PrivilegedUser) value).getUser();
        return (user == null) ? "" : user.getEmail();
    }
}
