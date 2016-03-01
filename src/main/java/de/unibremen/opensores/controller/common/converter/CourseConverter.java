package de.unibremen.opensores.controller.common.converter;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.service.CourseService;

import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.convert.ConverterException;

/**
 * Converter for the Course class.
 *
 * @author Sören Tempel
 */
@ManagedBean
@RequestScoped
public class CourseConverter implements Converter {
    /**
     * The course service for connection to the database.
     */
    @EJB
    private CourseService courseService;

    @Override
    public Object getAsObject(FacesContext context,
            UIComponent component, String value) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        Course course;
        try {
            course = (value == null || value.trim().isEmpty()) ? null : courseService
                .find(Course.class, Integer.valueOf(value).longValue());
        } catch (NumberFormatException e) {
            course = null;
        }

        if (course == null) {
            String msg = bundle.getString("courses.fail");
            throw new ConverterException(new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
        } else {
            return course;
        }
    }

    @Override
    public String getAsString(FacesContext context,
            UIComponent component, Object value) {
        if (!(value instanceof Course)) {
            return "";
        }

        Course course = (Course) value;
        return course.getCourseId().toString();
    }
}
