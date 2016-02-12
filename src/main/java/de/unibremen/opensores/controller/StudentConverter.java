package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Student;
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
 * Converter for the Student class.
 *
 * @author Sören Tempel
 */
@ManagedBean
@RequestScoped
public class StudentConverter implements Converter {
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

        Student student = courseService.findStudent(course, value);
        if (student == null) {
            String fmt = bundle.getString("courses.studentDoesNotExist");
            String msg = new MessageFormat(fmt).format(new Object[]{value});
            throw new ConverterException(new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
        }

        return student;
    }

    @Override
    public String getAsString(FacesContext context,
            UIComponent component, Object value) {
        if (!(value instanceof Student)) {
            return "";
        }

        User user = ((Student) value).getUser();
        return (user == null) ? "" : user.getEmail();
    }
}
