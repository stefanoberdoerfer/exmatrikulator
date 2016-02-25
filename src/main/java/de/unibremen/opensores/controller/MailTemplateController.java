package de.unibremen.opensores.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.model.MailTemplate;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.service.MailTemplateService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;

import javax.ejb.EJB;
import java.util.List;
import java.util.ArrayList;
import javax.faces.bean.ViewScoped;
import javax.faces.bean.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;

/**
 * Controller for creating and editing templates.
 *
 * @author Lorenz Hüther
 */
@ManagedBean
@ViewScoped
public class MailTemplateController {

    @EJB
    private MailTemplateService mailTemplateService;

    @EJB
    private GradeService gradeService;

    @EJB
    private CourseService courseService;

    @EJB
    private UserService userService;

    /**
     * The current MailTemplate.
     */
    private MailTemplate mailTemplate;

    /**
     * The current name.
     */
    private String templateName;

    /**
     * The current subject.
     */
    private String templateSubject;

    /**
     * The current text.
     */
    private String templateText;

    /**
     * The current locale.
     */
    private String templateLocale;

    /**
     * The current course.
     */
    private Course course;

    /**
     * The Logger.
     */
    private Logger log = LogManager.getLogger(MailTemplateController.class);

    /**
     * The FacesContext of this controller.
     */
    private FacesContext context;

    /**
     * Resource bundle.
     */
    private ResourceBundle bundle;

    /**
     * Executed on request, initializes the course.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");

        context = FacesContext.getCurrentInstance();

        bundle = ResourceBundle.getBundle("messages",
            context.getViewRoot().getLocale());

        HttpServletRequest request = (HttpServletRequest) context
                .getExternalContext()
                .getRequest();

        String courseId = request.getParameter(
                Constants.HTTP_PARAM_COURSE_ID);
        String mailTemplateId = request.getParameter(
                Constants.HTTP_PARAM_MAILTEMPLATE_ID);

        User user = (User) context.getExternalContext()
                .getSessionMap()
                .get(Constants.SESSION_MAP_KEY_USER);

        Course currentCourse = null;
        if (courseId != null) {
            currentCourse = courseService.findCourseById(courseId);
            if (currentCourse == null) {
                log.debug("Cannot find course" + courseId);
                return;
            }
            log.debug("Got courseId: " + courseId);
        }

        if (!userService.hasCourseRole(user, Role.LECTURER, currentCourse)
                || userService.getLecturer(user, currentCourse).isDeleted()) {
            log.warn("Unprivileged user " + user.getUserId()
                + " tried to modify mail templates.");
            return;
        } else {
            course = currentCourse;
        }

        MailTemplate template = null;
        if (mailTemplateId != null) {
            template = mailTemplateService.findMailTemplateById(course,
                    mailTemplateId);
            log.debug("Got MailTemplate with the name " + template.getName());
            mailTemplate = template;
            templateName = template.getName();
            templateSubject = template.getSubject();
            templateText = template.getText();
            templateLocale = template.getLocale();
        }
    }

    /**
     * Returns a list of templates for this course.
     *
     * @return a list of templates, might be empty.
     */
    public List<MailTemplate> getMailTemplateList() {
        if (course == null) {
            return new ArrayList<MailTemplate>();
        }

        return mailTemplateService.getMailTemplateList(course);
    }

    /**
     * Saves an already existing template to the database.
     */
    public void saveMailTemplate() {
        if (course == null) {
            return;
        }

        mailTemplate.setName(templateName);
        mailTemplate.setSubject(templateSubject);
        mailTemplate.setText(templateText);
        mailTemplate.setLocale(templateLocale);

        mailTemplateService.update(mailTemplate);
    }

    /**
     * Creates a new mailTemplate and persists it.
     */
    public void newMailTemplate() {
        if (course == null) {
            return;
        }

        mailTemplate = new MailTemplate();

        mailTemplate.setCourse(course);
        mailTemplate.setName(templateName);
        mailTemplate.setSubject(templateSubject);
        mailTemplate.setText(templateText);
        mailTemplate.setLocale(templateLocale);
        mailTemplate.setIsDefault(false);
        List<MailTemplate> templates = course.getEmailTemplates();
        templates.add(mailTemplate);
        course.setEmailTemplates(templates);

        courseService.update(course);
    }

    /**
     * Deletes a mailTemplate.
     *
     * @param template the template to be deleted.
     */
    public void remove(MailTemplate template) {
        if (course == null || template == null) {
            return;
        } else if (template.getIsDefault()) {
            return;
        }

        log.debug("Removed template");
        mailTemplateService.remove(template);
    }

    /**
     * Sets the default mail template for the final mails.
     *
     * @param template the mail template to be set.
     */
    public void setDefaultTemplate(MailTemplate template) {
        if (course == null) {
            return;
        }

        log.debug("New default template set.");

        MailTemplate defaultTemplate = mailTemplateService.getDefaultTemplate(course);
        defaultTemplate.setIsDefault(false);
        mailTemplateService.update(defaultTemplate);

        template.setIsDefault(true);

        mailTemplateService.update(template);
    }

    public String getCourseId() {
        return course.getCourseId().toString();
    }

    public MailTemplate getTemplate() {
        return mailTemplate;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getTemplateSubject() {
        return templateSubject;
    }

    public String getTemplateText() {
        return templateText;
    }

    public String getTemplateLocale() {
        return templateLocale;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setTemplateSubject(String templateSubject) {
        this.templateSubject = templateSubject;
    }

    public void setTemplateText(String templateText) {
        this.templateText = templateText;
    }

    public void setTemplateLocale(String templateLocale) {
        this.templateLocale = templateLocale;
    }
}
