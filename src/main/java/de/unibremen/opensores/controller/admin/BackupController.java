package de.unibremen.opensores.controller.admin;

import de.unibremen.opensores.controller.settings.CourseSettingsController;
import de.unibremen.opensores.model.Backup;
import de.unibremen.opensores.service.BackupService;
import de.unibremen.opensores.util.ServerProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.PersistenceException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.List;

/**
 * Controller for creating backups.
 *
 * @author Lorenz Huether
 */
@ManagedBean
@RequestScoped
public class BackupController {
    /**
     * The Backupservice.
     */
    @EJB
    private BackupService backupService;

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            CourseSettingsController.class);

    /**
     * The Dateformat as specified in config.properties.
     */
    private DateFormat dateForm;

    /**
     * The key for the DateFormat.
     */
    private static String dtePropertyKey = "exmatrikulator.backup.dateForm";

    /**
     * Executed after construction.
     */
    @PostConstruct
    public void init() {
        String format = null;

        try {
            Properties props = ServerProperties.getProperties();
            format = props.getProperty(dtePropertyKey);
        } catch (final IOException e) {
            log.debug(e);
            format = "yyyy-MM-dd_HH:mm:ss";
        } finally {
            dateForm = new SimpleDateFormat(format);
        }
    }

    /**
     * Creates a backup to the path specified in config.properties.
     */
    public void createBackup() {
        Backup backup = null;
        log.debug("createBackup called");
        try {
            backup = backupService.runBackup("ManualBackup");
        } catch (PersistenceException | IOException e) {
            log.fatal(e);
            return;
        }

        backupService.persist(backup);
    }

    /**
     * Deletes a backup from disk and database.
     *
     * @param backup to be deleted.
     */
    public void deleteBackup(Backup backup) {
        if (backup == null) {
            log.debug("Backup is null, nothing to delete");
            return;
        }

        String path = backup.getPath();
        backupService.remove(backup);

        try {
            File file = new File(path);
            backupService.deleteFolder(file);
            log.debug("Deleted Backup " + backup.getName());
        } catch (IOException | SecurityException e) {
            log.error(e);
        }
    }

    /**
     * Returns a Map of all backups.
     *
     * @return HashMap of all backups with name and id.
     */
    public List<Backup> listBackups() {
        return backupService.listBackups();
    }

    /**
     * Converts the backups date to a string.
     *
     * @param backup Backup of which the date is needed.
     *
     * @return the backups date as a string.
     */
    public String dateToString(Backup backup) {
        if (backup == null) {
            return "";
        }

        return dateForm.format(backup.getDate());
    }
}
