package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.MailTemplate;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * Service class for managing mail templates.
 *
 * @author Lorenz Huether
 */
@Stateless
public class MailTemplateService extends GenericService<MailTemplate> {
    /**
     * Returns a list of MailTemplates for a specific course.
     *
     * @param course the course to get the templates from.
     *
     * @return a List of MailTemplates.
     */
    public List<MailTemplate> getMailTemplateList(Course course) {
        List<MailTemplate> mailTemplates = em.createQuery(
                "SELECT m From MailTemplate m"
                + " JOIN m.course AS c WITH c.courseId = :courseId",
                        MailTemplate.class)
                            .setParameter("courseId", course.getCourseId())
                            .getResultList();
        return mailTemplates;
    }

    /**
     * Returns a list of all MailTemplates.
     *
     * @return a List of all MailTemplates.
     */
    public List<MailTemplate> getMailTemplateList() {
        List<MailTemplate> mailTemplates = em.createQuery(
                "Select * From MailTemplate", MailTemplate.class)
                        .getResultList();

        return mailTemplates;
    }

    /**
     * Returns a single MailTemplate for a specific course or null.
     *
     * @param course the course to get the templates from.
     *
     * @param templateId the template to get.
     *
     * @return a MailTemplates.
     */
    public MailTemplate findMailTemplateById(Course course,
            String templateId) {
        List<MailTemplate> mailTemplates = em.createQuery(
                "SELECT m From MailTemplate m"
                + " JOIN m.course AS c"
                + " WITH c.courseId = :courseId"
                + " WHERE m.mailTemplateId = :templateId", MailTemplate.class)
                        .setParameter("courseId", course.getCourseId())
                        .setParameter("templateId", Long.parseLong(templateId))
                        .getResultList();

        return (mailTemplates.isEmpty()) ? null : mailTemplates.get(0);
    }

    /**
     * Returns the Default MailTemplate for a specific course or null.
     *
     * @param course the course to get the templates from.
     *
     * @return a MailTemplates.
     */
    public MailTemplate getDefaultTemplate(Course course) {
        List<MailTemplate> mailTemplates = em.createQuery(
                "SELECT m From MailTemplate m"
                + " JOIN m.course AS c"
                + " WITH c.courseId = :courseId"
                + " WHERE m.isDefault = true", MailTemplate.class)
                        .setParameter("courseId", course.getCourseId())
                        .getResultList();

        return (mailTemplates.isEmpty()) ? null : mailTemplates.get(0);
    }
}
